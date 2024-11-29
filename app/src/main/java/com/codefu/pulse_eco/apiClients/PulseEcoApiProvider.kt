package com.codefu.pulse_eco.apiClients

import com.google.gson.GsonBuilder
import okhttp3.Response
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class PulseEcoApiProvider{

    companion object{
        @Volatile
        private var INSTANCE: PulseEcoApi? = null

        @JvmStatic
        fun getPulseEcoApi(): PulseEcoApi{
            return INSTANCE ?: synchronized(this){
                val instance = create()
                INSTANCE = instance
                instance
            }
        }

        private fun create(): PulseEcoApi{
            class QueryParamInterceptor : Interceptor {
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request().newBuilder()
                        //.addHeader("Authorization", "Basic QmFzaWMgYXJpeml6aXppOnN3YWdneTEyMw==")
                        .build()
                    return chain.proceed(request)
                }
            }

            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val okhttpClient = OkHttpClient.Builder()
                .addInterceptor(QueryParamInterceptor())
                .addInterceptor(httpLoggingInterceptor)
                .build()
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val gsonConverterFactory = GsonConverterFactory.create(gson)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://skopje.pulse.eco/rest/")
                .client(okhttpClient)
                .addConverterFactory(gsonConverterFactory)
                .build()

            return retrofit.create(PulseEcoApi::class.java)
        }

    }

}