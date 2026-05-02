package com.kal.portfolio.pinninglab.di

import com.kal.portfolio.pinninglab.data.network.HttpBinApi
import com.kal.portfolio.pinninglab.data.network.HttpClientBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpBinApi(httpClientBuilder: HttpClientBuilder): HttpBinApi {
        return Retrofit.Builder()
            .baseUrl("https://httpbin.org")
            .client(httpClientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HttpBinApi::class.java)
    }
}