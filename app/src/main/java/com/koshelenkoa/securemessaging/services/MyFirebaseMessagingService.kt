package com.koshelenkoa.securemessaging.services

import android.util.Log
import androidx.annotation.WorkerThread
import com.koshelenkoa.securemessaging.api.RequestSchedueler
import com.koshelenkoa.securemessaging.data.local.Message
import com.koshelenkoa.securemessaging.data.local.MessagesRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var repository: MessagesRepository


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
        val job = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.IO + job)
        RequestSchedueler()

        scope.launch { sendRegistrationToServer(token) }
    }


    /**
     * Updates userclaim token
     */
    fun sendRegistrationToServer(token: String) {
        val requestSchedueler = RequestSchedueler()
        requestSchedueler.sendRequest(
            functionName = "updateToken",
            onSuccess = { response ->
                if (response == "200") {
                    Log.d("FireBaseMessaging", "device Token updated")
                } else {
                    Log.d("FireBaseMessaging", "device Token failed to update")
                    throw Exception()
                }
            },
            onFailure = {
                Log.d("FireBaseMessaging", "device Token failed to update")
                throw Exception()
            },
            textToSend = mapOf("token" to token)
        )
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
    }

}