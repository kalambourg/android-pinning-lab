package com.kal.portfolio.pinninglab.data.network

import com.kal.portfolio.pinninglab.data.model.HttpBinResponse
import retrofit2.http.GET

interface HttpBinApi {

    @GET("/get")
    suspend fun get(): HttpBinResponse

    @GET("/headers")
    suspend fun headers(): HttpBinResponse
}