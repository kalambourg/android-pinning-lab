Java.perform(() => {
    const ProxyDetector = Java.use("com.kal.portfolio.pinninglab.data.network.ProxyDetector");
    
    ProxyDetector.isProxySet.implementation = function() {
        console.log("[*] ProxyDetector.isProxySet() hooked → returning false");
        return false;
    };
    
    console.log("[*] Proxy detection bypass active");
});