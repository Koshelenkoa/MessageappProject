package com.example.myapplication.ui.viewModels

import ChatApi
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapplication.Api.RequestSchedueler
import com.example.myapplication.QRcode.StringForQRGenerator
import com.example.myapplication.cryptography.KeyGeneratorForKeyStore
import com.example.myapplication.data.local.Chat
import com.example.myapplication.data.local.ChatDataHolder
import com.example.myapplication.data.local.ChatRepository
import com.example.myapplication.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.KeyStore
import javax.inject.Inject

val TAG = "Create QRCode"

@HiltViewModel
class QRCodeViewModel @Inject constructor(val chatRepository: ChatRepository): ViewModel() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var auth = AuthManager()
    private val requestSchedueler = RequestSchedueler()
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

    fun loadedSuccessfully() {
        loaded = true
        Log.d(TAG, "Loaded successfully")
    }

    fun onError(errorMessage: String?){
        this.errorMessage = errorMessage
        val chat = chatRepository.getChat(chatId)
        if(chat != null) {
            chatRepository.deleteChats(chat)
        }
    }

   fun updateContent(input: String?){
        if (input != null) {
            val keystore = KeyStore.getInstance("AndroidKeyStore")
            val keyGeneratorForKeyStore = KeyGeneratorForKeyStore(keystore)
            val stringGenerator = StringForQRGenerator(keyGeneratorForKeyStore)
            when(step){
                "step1" -> {
                    content = stringGenerator.step1(input)
                }
                "step3" -> {
                    content = stringGenerator.step3()
                }
            }
        }
   }

    fun createChat(){
        val service = ChatApi.create()
        scope.launch(Dispatchers.IO) {
            requestSchedueler.sendRequest(
                request = service::createChat,
                onSuccess = {response, timestamp ->
                    updateContent(chatId)
                    val chat = ChatDataHolder.chat!!
                    ChatDataHolder.update(timestamp = timestamp)
                    loadedSuccessfully()
                }
                ,
                onFailure = {response, t ->
                    if(response != null) {
                        response.errorBody()?.let {
                            Log.d(TAG, it.string())
                            onError(it.string())
                        }
                    }
                    if(t != null){
                        onError(t.message)
                    }},
                textToSend = mapOf("text" to chatId)
            )
        }
    }

    fun connectToChat(){
        val service = ChatApi.create()
        scope.launch(Dispatchers.IO) {
            requestSchedueler.sendRequest(
                request = service::connectToChat,
                onSuccess = {response, timestamp ->
                    ChatDataHolder.update(timestamp = timestamp)
                    updateContent(chatId)
                    loadedSuccessfully()
                },
                onFailure = {response, t ->
                        if(response != null) {
                            response.errorBody()?.let {
                                Log.d(TAG, it.string())
                                onError(it.string())
                            }
                        }
                        if(t != null){
                            onError(t.message)
                        }},
                textToSend = mapOf("text" to chatId))
                }
    }

    fun updateChatId(newChatId: String){
        chatId = newChatId
    }

    fun updateStep(newStep: String?){
            if(newStep != null){
                step = newStep
            }
        }
}
