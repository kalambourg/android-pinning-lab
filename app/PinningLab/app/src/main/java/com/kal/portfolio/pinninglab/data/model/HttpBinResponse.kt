package com.kal.portfolio.pinninglab.data.model

import com.google.gson.annotations.SerializedName

data class HttpBinResponse(
    @SerializedName("url") val url: String? = null,
    @SerializedName("headers") val headers: Map<String, String>? = null,
    @SerializedName("origin") val origin: String? = null,
    @SerializedName("args") val args: Map<String, String>? = null
)