package com.koshelenkoa.securemessaging.api

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RequestSchedueler {
    private val functions = FirebaseFunctions.getInstance()
    val TAG = "RequestSchedueler"

    /**
     * Method to send http requests to the server
     * Never should be used on a main thread
     * @param functionName name of the function on the backend
     * @param textToSend map containing request body will be converted to json
     * @param onSuccess Unit will be executed on response code 200
     * @param onFailure Unit will be executed in a response code that is not 200
     * @return response code
     */
    fun sendRequest(
        functionName: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
        textToSend: Map<String, String?>,
    ) {
        call(functionName, textToSend)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    if (e is FirebaseFunctionsException) {
                        val code = e.code
                        val details = e.details
                        Log.d(TAG, "$code: $details")
                    }
                    e?.message?.let { Log.d(TAG, it) }
                    CoroutineScope(Dispatchers.IO).launch {
                        onFailure(e.toString())
                    }
                } else {
                    val handler = CoroutineExceptionHandler { context, throwable ->
                        throwable.message?.let { Log.d(TAG, it) }
                        Log.d(TAG, task.result)
                        onFailure(task.result)
                    }
                    CoroutineScope(Dispatchers.IO).launch(handler) {
                        onSuccess(task.result)
                    }
                }
            }

    }

    private fun call(functionName: String, data: Map<String, Any?>): Task<String> {
        return functions
            .getHttpsCallable(functionName)
            .call(data)
            .continueWith { task ->
                val result = task.result?.data as String
                result
            }
    }
}