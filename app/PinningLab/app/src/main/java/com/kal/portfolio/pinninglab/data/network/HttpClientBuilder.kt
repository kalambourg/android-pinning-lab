package com.kal.portfolio.pinninglab.data.network

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpClientBuilder @Inject constructor(
    private val proxyDetector: ProxyDetector
) {

    companion object {
        private const val HOSTNAME = "httpbin.org"
        private const val PIN_PRIMARY = "sha256/5BWYNtPxvjsl+qhQLxo3jz3ZaK74xyHT/QdOhBB07i0="
        private const val PIN_BACKUP = "sha256/vxRon/El5KuI4vx5ey1DgmsYmRY0nDd5Cg4GfJ8S+bg="
    }

    fun build(): OkHttpClient {
        val certificatePinner = CertificatePinner.Builder()
            .add(HOSTNAME, PIN_PRIMARY)
            .add(HOSTNAME, PIN_BACKUP)
            .build()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(proxyDetector)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}