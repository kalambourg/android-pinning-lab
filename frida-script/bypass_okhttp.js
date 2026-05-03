Java.perform(() => {
    try {
        const CertificatePinner = Java.use("okhttp3.CertificatePinner");
        
        CertificatePinner["check$okhttp"].implementation = function(hostname, peerCertificates) {
            console.log("[*] CertificatePinner.check$okhttp hooked pour : " + hostname);
        };
        console.log("[*] OkHttp pinning bypass actif via check$okhttp");
    } catch(e) {
        console.log("[!] check$okhttp non disponible : " + e);
        
        const CertificatePinner = Java.use("okhttp3.CertificatePinner");
        CertificatePinner.check.overload("java.lang.String", "java.util.List").implementation = function(h, c) {
            console.log("[*] CertificatePinner.check(List) hooked : " + h);
        };
    }
});