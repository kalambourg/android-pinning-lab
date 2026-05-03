Java.perform(() => {
    try {
        const TrustManagerImpl = Java.use("com.android.org.conscrypt.TrustManagerImpl");
        TrustManagerImpl.verifyChain.implementation = function(untrustedChain, trustAnchorChain, host, clientAuth, ocspData, tlsSctData) {
            console.log("[*] TrustManagerImpl.verifyChain hooked → bypass pour : " + host);
            return untrustedChain;
        };
        console.log("[*] TrustManager bypass actif");
    } catch(e) {
        console.log("[!] TrustManagerImpl non disponible : " + e);
    }
});