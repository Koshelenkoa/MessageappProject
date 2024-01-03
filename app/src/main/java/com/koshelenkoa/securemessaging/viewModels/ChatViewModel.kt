package com.koshelenkoa.securemessaging.viewModels

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import androidx.paging.map
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.koshelenkoa.securemessaging.MainApplication
import com.koshelenkoa.securemessaging.api.RequestSchedueler
import com.koshelenkoa.securemessaging.cryptography.MessageEncryption
import com.koshelenkoa.securemessaging.data.local.ChatRepository
import com.koshelenkoa.securemessaging.data.local.Message
import com.koshelenkoa.securemessaging.data.local.MessageData
import com.koshelenkoa.securemessaging.data.local.MessagesRepository
import com.koshelenkoa.securemessaging.data.local.room.MessageItem
import com.koshelenkoa.securemessaging.util.UploadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {
    val TAG = "ChatViewModel"
    private val _uiState = MutableStateFlow(ChatState())
    val uiState: StateFlow<ChatState> = _uiState.asStateFlow()
    private var selectedMessages: MutableList<String?> = ArrayList()
    var mText by mutableStateOf("")
    var attachments : List<Uri> by mutableStateOf(emptyList())
    lateinit var messagePagingDataFlow: Flow<PagingData<MessageItem>>
    private val uid = FirebaseAuth.getInstance().uid
    private lateinit var messageEncryptor: MessageEncryption
    private val requestSchedueler = RequestSchedueler()

    fun updateTextBox(text: String) {
        mText = text
    }

    fun setChat(chatId: String?) {
        _uiState.update { currentState ->
            currentState.copy(chatId = chatId)
        }
        CoroutineScope(Dispatchers.IO).launch {
            messageEncryptor = MessageEncryption(
                chatRepository,
                uiState.value.chatId!!
            )
        }

    }

    fun setNickName() {
        CoroutineScope(Dispatchers.IO).launch {
            _uiState.update { currentState ->
                currentState.copy(username = chatRepository.getChat(currentState.chatId!!)?.nick)
            }
        }
    }

    fun loadMessages() {
        messagePagingDataFlow = messagesRepository.getMessagesStream(uiState.value.chatId!!)
            .map {
                it.map { message ->
                    MessageItem(
                        messageEncryptor.decryptMessage(message),
                        uid!!
                    )
                }
            }
    }

    fun selectMessages(messages: String?) {
        if (messages != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    selectedMessages = currentState.selectedMessages.plus(messages)
                )
            }
        }
    }

    fun deleteMessages(vararg messageIds: String = uiState.value.selectedMessages.toTypedArray()) {
        CoroutineScope(Dispatchers.IO).launch {
            val messages = messagesRepository
                .getMessageById(*messageIds)
            messages.collect {
                messagesRepository.deleteMessages(*it.toTypedArray())
            }
        }
        for (message in selectedMessages) {
            _uiState.update { currentState ->
                currentState.copy(
                    messages = currentState.messages.filter { item ->
                        item.messageId !in selectedMessages
                    },
                )
            }
        }
    }

    fun selectionScreenOn() {
        _uiState.update { currentState ->
            currentState.copy(selectionScreen = true)
        }
    }

    fun selectionScreenOff() {
        _uiState.update { currentState ->
            currentState.copy(selectionScreen = false)
        }
    }

    fun getSelectionScreen(): Boolean {
        return uiState.value.selectionScreen
    }

    fun attach(uris: List<Uri>){
        for(uri in uris) {
            attachments = attachments.plus(uri)
        }
    }

    fun unattach(uri: Uri){
        attachments = attachments.minus(uri)
    }

    fun sendMessage(messageText: String? = mText, messageAttachments: List<String>? = attachments.map{uri -> uri.toString()}){
        if (!messageText.isNullOrEmpty() || !messageAttachments.isNullOrEmpty()) {
            mText = ""
            attachments = emptyList()
            val messageData = MessageData(messageText!!.trim(), messageAttachments?.toTypedArray())
            val gson = Gson()
            val dataJson = gson.toJson(messageData)
            val message = Message(
                messageId = UUID.randomUUID().toString(),
                chat = uiState.value.chatId,
                isSent = Message.PENDING,
                data = dataJson.toByteArray(),
                sender = uid!!,
                timestamp = System.currentTimeMillis()
            )
            CoroutineScope(Dispatchers.IO).launch {
            val encryptedMessage = messageEncryptor.encryptMessage(message)
            val messageId = message.messageId
            messagesRepository.createMessage(encryptedMessage)
            Log.d(TAG, "message saved")
            try {
                var urls = emptyList<String>()
                if(messageAttachments != null && messageAttachments.isNotEmpty()) {
                    urls = uploadImages(messageAttachments, messageEncryptor)
                    Log.d(TAG, urls[0])
                }
            val transitMessage = message.toTransitMessage(urls)
            val encryptedTransitMessage = messageEncryptor.encryptMessage(transitMessage)
            requestSchedueler.sendRequest(
                functionName = "addMessageToServer",
                    textToSend = encryptedTransitMessage.toMap(),
                    onSuccess = { response ->
                        try {
                            val timestamp = response.toLong()
                            messagesRepository.updateTime(messageId, timestamp)
                        } catch (e: Exception) {
                            messagesRepository.updateStatusFailed(messageId)
                        }
                    },
                    onFailure = {
                        messagesRepository.updateStatusFailed(messageId)
                    },
                )
                }catch (e: Exception){
                messagesRepository.updateStatusFailed(messageId)
            }
            }
        }
    }

    fun resend(message: MessageItem) {
        Log.d(TAG, "resend")
        CoroutineScope(Dispatchers.IO).launch {
            val content = message.messageData
            val messageId = message.messageId
            val messages = messagesRepository.getMessageById(messageId)
            messages.collect {
                messagesRepository.deleteMessages(*it.toTypedArray())
            }
            Log.d(TAG, "message deleted")
            sendMessage(content.text, content.attachments?.toList())
        }
    }

    suspend fun uploadImages(attachments: List<String>, encryption: MessageEncryption): List<String>{
        var attachmentsUrls = emptyList<String>()
        for (attachment in attachments){
            val uri = Uri.parse(attachment)
            val srs = ImageDecoder.createSource(MainApplication.getApplication().baseContext.contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(srs)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val bitmapBytes = stream.toByteArray()
            bitmap.recycle()

            val encryptedBytes = encryption.encryptBytes(bitmapBytes)
            val uploadManager = UploadManager()
            val url = uploadManager.uploadImage(encryptedBytes)
            if (url != null){
                attachmentsUrls = attachmentsUrls.plus(url)
            }
        }
        return attachmentsUrls
    }
}

data class ChatState(
    val chatId: String? = null,
    val username: String? = null,
    val messages: List<MessageItem> = emptyList(),
    val selectedMessages: List<String> = emptyList(),
    val selectionScreen: Boolean = false,
)
