package com.kal.portfolio.pinninglab.data.repository

import com.kal.portfolio.pinninglab.data.model.HttpBinResponse
import com.kal.portfolio.pinninglab.data.network.HttpBinApi
import com.kal.portfolio.pinninglab.data.network.ProxyDetectedException
import javax.inject.Inject
import javax.inject.Singleton

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}

@Singleton
class NetworkRepository @Inject constructor(
    private val api: HttpBinApi
) {

    suspend fun get(): NetworkResult<HttpBinResponse> {
        return try {
            NetworkResult.Success(api.get())
        } catch (e: ProxyDetectedException) {
            NetworkResult.Error("Proxy détecté — intercept bloqué")
        } catch (e: javax.net.ssl.SSLPeerUnverifiedException) {
            NetworkResult.Error("Certificate pinning : certificat rejeté")
        } catch (e: Exception) {
            NetworkResult.Error("Erreur réseau : ${e.message}")
        }
    }

    suspend fun headers(): NetworkResult<HttpBinResponse> {
        return try {
            NetworkResult.Success(api.headers())
        } catch (e: ProxyDetectedException) {
            NetworkResult.Error("Proxy détecté — intercept bloqué")
        } catch (e: javax.net.ssl.SSLPeerUnverifiedException) {
            NetworkResult.Error("Certificate pinning : certificat rejeté")
        } catch (e: Exception) {
            NetworkResult.Error("Erreur réseau : ${e.message}")
        }
    }
}