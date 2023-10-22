package com.example.myapplication

import ChatApi
import android.util.Log
import com.example.myapplication.util.AuthManager
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Callback

import retrofit2.Call
import retrofit2.Response
import kotlin.properties.Delegates

class ChatApiTest {

    private lateinit var chatApi: ChatApi
    private var token: String? = null
    private var uid: String? = null

    @Before
    suspend fun setup() {
        val auth = AuthManager()
        val user = auth.signInUser("userTest@email.com", "P4ssword?")
        uid = user?.uid
        user?.getIdToken(false)?.addOnCompleteListener {
            if (it.isSuccessful) {
                token = it.result?.token
            }
        }
        if(token != null) {
            chatApi = ChatApi.create(token!!)
        }
    }

    @Test
    suspend fun testCreateChat_Success() {
        var responseCode by Delegates.notNull<Int>()
        if(uid != null) {
            if (token != null) {

                mapOf(
                    "text" to "chatId0000",
                    "uid" to uid
                )?.let {
                    token?.let { it1 ->
                        chatApi.createChat(
                            it, it1
                        )
                            .enqueue(object : Callback<ResponseBody> {
                                override fun onResponse(
                                    call: Call<ResponseBody>,
                                    response: Response<ResponseBody>,
                                ) {
                                    if (response.isSuccessful) {
                                        Log.e(
                                            "Chat call test",
                                            "Connection to chat success: ${response.code()}"
                                        )
                                        responseCode = response.code()
                                    } else {
                                        Log.e(
                                            "Chat call test",
                                            "Connection to chat failed: ${response.code()}"
                                        )
                                        responseCode = response.code()
                                    }
                                }

                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Log.e("Chat call test", "Connection to chat failed")
                                    responseCode = 500
                                }

                            })
                    }
                }
            }
        }
        assertEquals(200, responseCode)
    }
}
