# PinningLab — Android Certificate Pinning + Frida Bypass

A two-part Android security project demonstrating certificate pinning implementation and dynamic bypass using Frida. Built as a portfolio project exploring both defensive and offensive mobile security.

---

## Project Structure

```
android-pinning-lab/
├── app/                        # Android app with certificate pinning
└── frida-scripts/
    ├── bypass_proxy_detection.js   # Bypass ProxyDetector
    ├── bypass_trustmanager.js      # Bypass TLS validation layer
    └── bypass_okhttp.js            # Bypass OkHttp CertificatePinner
```

---

## Part A — Certificate Pinning Implementation

### What it does

An Android app that calls `https://httpbin.org` with three layers of network security:

```
Layer 1 — ProxyDetector        Blocks requests if a system proxy is configured
Layer 2 — X509TrustManager     TLS validation via Android trust chain
Layer 3 — CertificatePinner    SHA-256 SPKI pinning via OkHttp
```

### Certificate Pinning

Implemented via OkHttp `CertificatePinner` with multi-pin (primary + backup):

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("httpbin.org", "sha256/5BWYNtPxvjsl+qhQLxo3jz3ZaK74xyHT/QdOhBB07i0=") // primary
    .add("httpbin.org", "sha256/vxRon/El5KuI4vx5ey1DgmsYmRY0nDd5Cg4GfJ8S+bg=") // backup — Amazon RSA 2048 M03
    .build()
```

**Primary pin** — SHA-256 of httpbin.org's public key (SPKI).  
**Backup pin** — SHA-256 of the intermediate CA (Amazon RSA 2048 M03). Survives certificate renewal as long as the CA doesn't change.

### Proxy Detection

```kotlin
private fun isProxySet(): Boolean {
    val host = System.getProperty("http.proxyHost")
    val port = System.getProperty("http.proxyPort")
    return !host.isNullOrEmpty() && !port.isNullOrEmpty()
}
```

If a system proxy is detected (e.g. Burp Suite configured on the device), the request is blocked before it even reaches the network layer.

### Network Security Config

```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="system"/>
            <certificates src="user"/>
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

User CAs (including Burp's) are rejected in production. `debug-overrides` allows them only in debug builds.

### Test Results

| Scenario | Result |
|---|---|
| No proxy, direct connection | ✅ Request succeeds |
| Proxy configured on device | ❌ Blocked by ProxyDetector |
| Burp active, no Frida | ❌ Blocked by CertificatePinner |
| Burp active + Frida bypass | ✅ Traffic visible in Burp |

---

## Part B — Frida Bypass Scripts

### Prerequisites

```bash
# Rooted device with frida-server deployed
adb push frida-server /data/local/tmp/frida-server
adb shell chmod +x /data/local/tmp/frida-server
adb shell su -c "/data/local/tmp/frida-server &"

# Install frida-tools
pip install frida frida-tools
```

### Running the scripts

```bash
# Bypass proxy detection only
python -m frida_tools.repl -U -f com.kal.portfolio.pinninglab \
    -l frida-scripts/bypass_proxy_detection.js

# Full bypass — proxy detection + TLS + pinning
python -m frida_tools.repl -U -f com.kal.portfolio.pinninglab \
    -l frida-scripts/bypass_proxy_detection.js \
    -l frida-scripts/bypass_trustmanager.js \
    -l frida-scripts/bypass_okhttp.js
```

**`-f`** spawns the app — hooks are injected before any application code runs. Using `-n` (attach to running process) causes hooks to miss because OkHttp is already initialized via Hilt.

---

### bypass_proxy_detection.js

**What it targets:** `ProxyDetector.isProxySet()` — the method that checks system proxy properties.

**How it works:** Replaces the implementation to always return `false`, so the app believes no proxy is configured regardless of the actual system state.

```javascript
ProxyDetector.isProxySet.implementation = function() {
    return false;
};
```

**Why it works:** Proxy detection is a client-side check. Any client-side boolean check can be hooked and overridden at runtime via Frida.

---

### bypass_trustmanager.js

**What it targets:** `com.android.org.conscrypt.TrustManagerImpl.verifyChain()` — the internal Android method that validates the TLS certificate chain.

**How it works:** Replaces `verifyChain` to return the untrusted chain as-is without validation, so any certificate (including Burp's self-signed one) is accepted.

```javascript
TrustManagerImpl.verifyChain.implementation = function(untrustedChain, ...) {
    return untrustedChain; // skip validation, accept everything
};
```

**Why it works:** Even with a `CertificatePinner`, TLS handshake validation happens first. If the TrustManager accepts Burp's certificate, the connection proceeds to the pinning check.

---

### bypass_okhttp.js

**What it targets:** `okhttp3.CertificatePinner.check$okhttp()` — the internal OkHttp method that compares certificate pins.

**How it works:** Replaces the check method with an empty function that never throws, so OkHttp treats every certificate as pinned regardless of its actual SHA-256 hash.

```javascript
CertificatePinner["check$okhttp"].implementation = function(hostname, peerCertificates) {
    // no exception thrown → pinning "passes"
};
```

**Why `check$okhttp` instead of `check`:** OkHttp 4.x internally calls `check$okhttp` — a Kotlin-generated internal method. Hooking the public `check()` overloads doesn't intercept the actual execution path in recent versions.

---

## What I Intentionally Didn't Do (And Why)

**No SSL Kill Switch script** — Generic SSL unpinning scripts (like ssl-kill-switch) exist and would bypass this app in one line. The point of this project is to understand *why* each bypass works, not just to unpin. Writing targeted scripts demonstrates deeper understanding.

**No native layer pinning** — Implementing pinning in C++ via NDK would make Frida bypass significantly harder (requires Ghidra/IDA for analysis, no direct Java method hooks). Out of scope for this project but the natural next step for hardened apps.

**No obfuscation** — R8 obfuscation would rename `isProxySet`, `ProxyDetector`, etc., making Frida scripts non-portable. Obfuscation + pinning together is the real production defense — deliberately kept separate here for clarity.

---

## Key Takeaways

**On the defensive side:** Multi-layer defense is meaningful. An attacker needs to bypass proxy detection, TLS validation, *and* certificate pinning independently. Each layer adds friction.

**On the offensive side:** All three layers are bypassable from the runtime on a rooted device. Client-side security controls can always be circumvented given enough access. The real protection is the key attestation and server-side validation (see Project 1 — Secure Storage Vault).

**The asymmetry:** Writing the defense took hours. Writing the bypass took minutes once you understand the internals. This asymmetry is the core lesson of mobile AppSec.

---

## Stack

```
Language        Kotlin
UI              Jetpack Compose + Material3
Architecture    MVVM + Clean Architecture
DI              Hilt
HTTP            OkHttp + Retrofit
Instrumentation Frida 17.x
Min SDK         30 (Android 11)
```