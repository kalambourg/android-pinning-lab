package com.kal.portfolio.pinninglab.data.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProxyDetector @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (isProxySet()) {
            throw ProxyDetectedException("Proxy détecté — requête bloquée")
        }
        return chain.proceed(chain.request())
    }

    private fun isProxySet(): Boolean {
        val host = System.getProperty("http.proxyHost")
        val port = System.getProperty("http.proxyPort")
        return !host.isNullOrEmpty() && !port.isNullOrEmpty()
    }
}

class ProxyDetectedException(message: String) : Exception(message)