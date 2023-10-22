import com.example.myapplication.Api.URLpath
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.Header
import retrofit2.http.POST


interface ChatApi {

    @POST("/createChannel")
    fun createChat(
        @Body chat: Map<String, Any?>,
        @Header("Authorization") bearerToken: String,
    ): Call<ResponseBody>

    @POST("/addUserToChannel")
    fun connectToChat(
        @Body chat: Map<String, Any?>,
        @Header("Authorization Bearer ") bearerToken: String,
    ): Call<ResponseBody>

    companion object {

        fun create(url: String): ChatApi {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ChatApi::class.java)
        }

        fun create(): ChatApi {
            val url = URLpath.BASE
            return create(url)
        }
    }
}
