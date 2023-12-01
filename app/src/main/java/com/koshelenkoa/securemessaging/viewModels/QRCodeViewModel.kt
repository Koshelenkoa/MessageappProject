package com.koshelenkoa.securemessaging.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.koshelenkoa.securemessaging.api.RequestSchedueler
import com.koshelenkoa.securemessaging.data.local.ChatDataHolder
import com.koshelenkoa.securemessaging.data.local.ChatRepository
import com.koshelenkoa.securemessaging.util.StringForQRGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.security.KeyStore
import javax.inject.Inject

val TAG = "Create QRCode"

@HiltViewModel
class QRCodeViewModel @Inject constructor(val chatRepository: ChatRepository) : ViewModel() {
    private val requestScheduler = RequestSchedueler()
    private val keystore = KeyStore.getInstance("AndroidKeyStore")
    private val keyGeneratorForKeyStore =
        com.koshelenkoa.securemessaging.cryptography.KeyGeneratorForKeyStore(keystore)
    private val stringGenerator = StringForQRGenerator(keyGeneratorForKeyStore)
    var loaded by mutableStateOf(false)
        private set
    var chatId by mutableStateOf("")
        private set
    var content by mutableStateOf("")
        private set
    var step by mutableStateOf("")
        private set
    var errorMessage: String? by mutableStateOf(null)
        private set

    private fun loadedSuccessfully() {
        loaded = true
        Log.d(TAG, "Loaded successfully")
    }

    private fun onError(errorMessage: String?) {
        this.errorMessage = errorMessage
        ChatDataHolder.setChat(null)
    }

    private fun updateContent(input: String?) {
        if (input != null) {
            when (step) {
                "step1" -> {
                    content = stringGenerator.step1(input)
                }

                "step3" -> {
                    content = stringGenerator.step3()
                }
            }
        }
    }

    fun updateChatId(newChatId: String) {
        chatId = newChatId
    }

    fun updateStep(newStep: String?) {
        if (newStep != null) {
            step = newStep
        }
    }

    fun createChat() {
        requestScheduler.sendRequest(
            functionName = "createChannel",
            onSuccess = { response ->
                try {
                    val timestamp = response.toLong()
                    updateContent(chatId)
                    ChatDataHolder.update(timestamp = timestamp)
                    loadedSuccessfully()
                } catch (e: Exception) {
                    e.message?.let { Log.d(TAG, it) }
                    onError("Response error")
                }
            },
            onFailure = { details ->
                onError(details)
            },
            textToSend = mapOf("chat" to chatId)
        )
    }

    fun connectToChat() {
        requestScheduler.sendRequest(
            functionName = "addUserToChannel",
            onSuccess = { response ->
                try {
                    val timestamp = response.toLong()
                    ChatDataHolder.update(timestamp = timestamp)
                    updateContent(chatId)
                    loadedSuccessfully()
                } catch (e: Exception) {
                    e.message?.let { Log.d(TAG, it) }
                    onError("Response error")
                }
            },
            onFailure = { details ->
                onError(details)
            },
            textToSend = mapOf("chat" to chatId)
        )
    }
}
