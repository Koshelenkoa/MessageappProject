package com.example.myapplication.Api

import android.util.Log
import com.example.myapplication.ui.viewModels.TAG
import com.example.myapplication.util.AuthManager
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestSchedueler {

    fun sendRequest(
        request: (Map<String, Any?>, String) -> Call<ResponseBody>,
        onSuccess: (Response<ResponseBody>, Long) -> Unit,
        onFailure: (Response<ResponseBody>?, t: Throwable?) -> Unit,
        textToSend: Map<String, Any?>
    ){
        val auth = AuthManager()
        val uid = auth.getUid()
        val token = auth.getAuthToken().result.token
        if (token != null) {
            val data = textToSend.toMutableMap()
            data["uid"] = uid
            val call: Call<ResponseBody> =
                request(data, token)

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val timestamp = response.raw().receivedResponseAtMillis
                        onSuccess(response, timestamp)
                    } else
                        response.errorBody()?.let {
                            Log.d(TAG, it.string())
                        }
                    onFailure(response, null)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d(TAG, t.toString())
                    onFailure(null, t)
                }
            })
        }
    }
}