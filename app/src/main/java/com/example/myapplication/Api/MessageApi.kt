package com.example.myapplication.Api
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface MessageApi {

    /**
     * Sends message as JSON Http POST request
     * @param message - HashMap of the message
     * @return call<timestamp>
     */
    @POST("/writeMessageToServer")
    fun sendMessage(
        @Body message: Map<String, Any?>,
        @Header("Authorization Bearer ") bearerToken: String,
    ): Call<ResponseBody>

    companion object {

            fun create(url: String): MessageApi {
                val client = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val request = original.newBuilder()
                            .method(original.method, original.body)
                            .build()
                        chain.proceed(request)
                    }
                    .build()

                return Retrofit.Builder()
                    .baseUrl(url)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(MessageApi::class.java)
            }


            fun create(): MessageApi {
                val url = URLpath.BASE
                return create(url)
            }

    }
}