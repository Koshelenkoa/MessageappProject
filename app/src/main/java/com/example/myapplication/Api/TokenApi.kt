package com.example.myapplication.Api

import com.example.myapplication.Api.URLpath
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TokenApi {

    @POST("/updateToken")
    fun updateToken(
        @Body chat: Map<String, String?>,
        @Header("Authorization") bearerToken: String
    ): Call<Void>

    companion object {

        fun create(url: String, token: String): TokenApi {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .build()


            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TokenApi::class.java)
        }

        fun create(token: String): TokenApi {
            val url = URLpath.BASE
            return create(url, token)
        }
    }
}
