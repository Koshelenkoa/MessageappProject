package com.example.myapplication.services

import android.content.Intent
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.myapplication.Api.TokenApi
import com.example.myapplication.data.local.Message
import com.example.myapplication.data.local.MessagesRepository
import com.example.myapplication.util.AuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var repository: MessagesRepository

    private val auth = FirebaseAuth.getInstance()
    private val TAG = "MyFirebaseMessagingService"


    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) MainApplication is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    //token is sent as a display name for the sake of convenience and easy retrieval
    /**
     *
     */
    private fun sendRegistrationToServer(token: String) {
        val job = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.IO + job)

        scope.launch {
            try {
                val authManager = AuthManager()
                val user = auth.currentUser
                authManager.getAuthToken().addOnSuccessListener {
                    val authToken = it.token
                    if(authToken != null){
                        val uid = user?.uid
                        if (uid != null) {
                            val service = TokenApi.create(token)
                            val call: Call<Void> =
                                service.updateToken(
                                    mapOf(
                                        "token" to token,
                                        "uid" to uid
                                    ), authToken
                                )
                            call.enqueue(object : Callback<Void> {

                                override fun onResponse(
                                    call: Call<Void>,
                                    response: Response<Void>
                                ) {
                                    if (response.isSuccessful) {
                                        Log.d("FireBaseMessaging", "device Token updated")
                                    } else {
                                        Log.d("FireBaseMessaging", "device Token failed to update")
                                    }
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Log.d(TAG, t.toString())
                                }
                            })
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Can not send token")
            }
        }
    }

    /**
     * Writes message to the db
     * @param message
     */
    @WorkerThread
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "message received")
        val messageMap = message.data
        val message = Message(messageMap)
        repository.createMessage(message)
        sendBroadcast(Intent("UPDATE_CHAT"))
    }

}