package com.example.myapplication.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.Api.MessageApi
import com.example.myapplication.Api.RequestSchedueler
import com.example.myapplication.data.local.MessagesRepository
import com.example.myapplication.util.AuthManager
import javax.inject.Inject

/**
 * calls sendMessage from MessageApi
 * input data: all parameters of Message as String: Any?
 */
class SendMessageWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private lateinit var service: MessageApi
    private val requestSchedueler = RequestSchedueler()

    @Inject
    lateinit var repository: MessagesRepository
    val TAG = "SendMessageWorker"
    val auth = AuthManager()

    override suspend fun doWork(): Result {
        lateinit var result: Result
        val service = MessageApi.create()
        requestSchedueler.sendRequest(
            request = service::sendMessage,
            onSuccess = {response, timestamp ->
                repository.updateTime(inputData.getString("mesageId"), timestamp)
                result = Result.success()
            },
            onFailure = {response, t ->
                result = Result.failure()
            },
            textToSend = inputData.keyValueMap
        )
            return result

    }
}