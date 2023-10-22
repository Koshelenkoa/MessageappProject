package com.example.myapplication.ui.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.MainApplication
import com.example.myapplication.cryptography.MessageEncryption
import com.example.myapplication.data.local.ChatRepository
import com.example.myapplication.data.local.Message
import com.example.myapplication.data.local.MessagesRepository
import com.example.myapplication.data.local.room.MessageDao
import com.example.myapplication.data.local.room.MessageItem
import com.example.myapplication.workers.SendMessageWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val chatRepository: ChatRepository,
    private val messageDao: MessageDao
) : ViewModel() {
    private val messageEncryptor: MessageEncryption = MessageEncryption(chatRepository)
    private val _uiState = MutableStateFlow(ChatState())
    val uiState: StateFlow<ChatState> = _uiState.asStateFlow()
    private var selectedMessages: MutableList<String?> = ArrayList()
    var mText by mutableStateOf("")
    private lateinit var login: String
    var messageToBeSent: Message by mutableStateOf(Message(""))
        private set
    val messages: LiveData<List<Message>> = messageDao
        .loadAllMessages(uiState.value.chatId).asLiveData()

    fun setLogin(login: String){
        this.login = login
    }
    fun setChat(chatId: String?) {
        _uiState.update { currentState ->
            currentState.copy(
                chatId = chatId,
                username = currentState.username,
                messages = currentState.messages
            )
        }
    }

    fun selectMessages(messages: String?) {
        if (messages!=null) {
            _uiState.update { currentState ->
                currentState.copy(
                    messages = currentState.messages,
                    chatId = currentState.chatId,
                    username = currentState.username,
                    selectedMessages = currentState.selectedMessages.plus(messages)
                )
            }
        }
    }

    fun deleteMessages() {
        CoroutineScope(Dispatchers.IO).launch{
            val messages = messagesRepository
                .getMessageById(*uiState.value.selectedMessages.toTypedArray())
            messages.collect() {
                messagesRepository.deleteMessages(*it.toTypedArray())
            }
        }
        for (message in selectedMessages) {
            _uiState.update { currentState ->
                currentState.copy(
                    username = currentState.username,
                    messages = currentState.messages.filter { item ->
                        item.messageId !in selectedMessages
                    },
                )
            }
        }
    }
        fun selectionScreenOn() {
            _uiState.update { currentState ->
                currentState.copy(
                    selectionScreen = true,
                    messages = currentState.messages,
                    chatId = currentState.chatId,
                    username = currentState.username,
                    selectedMessages = currentState.selectedMessages
                )
            }
        }

        fun selectionScreenOff() {
            _uiState.update { currentState ->
                currentState.copy(
                    selectionScreen = false,
                    messages = currentState.messages,
                    chatId = currentState.chatId,
                    username = currentState.username,
                    selectedMessages = currentState.selectedMessages
                )
            }
        }

        fun getSelectionScreen(): Boolean {
            return uiState.value.selectionScreen
        }

        fun loadMessages() {
            CoroutineScope(Dispatchers.IO).launch {
                messagesRepository.loadMessages(uiState.value.chatId).collect { messages ->
                    var messageItems: MutableList<MessageItem> = mutableListOf()
                    val decryptedMessages = messageEncryptor.decryptMessages(messages)
                    for (message in decryptedMessages) {
                        val messageItem = MessageItem(message, login)
                        messageItems.add(messageItem)
                    }
                    val messageItemList = messageItems.toList()
                    _uiState.update { currentState ->
                        currentState.copy(
                            messages = messageItemList,
                            chatId = currentState.chatId,
                            username = currentState.username,
                            selectedMessages = currentState.selectedMessages
                        )
                    }
                }
            }
        }

        fun encryptMessage() {
            val messageText = mText
            var message = Message(
                messageId = UUID.randomUUID().toString(),
                chat = uiState.value.chatId,
                isSent = false,
                data = messageText,
                sender = login
            )
            //encrypt message
            CoroutineScope(Dispatchers.IO).launch {
                if (messageText != null && messageText.trim() == "") {
                    message = messageEncryptor.encryptMessage(message, uiState.value.chatId!!)
                    //save encypted message to db with flag is_sent = false
                    messagesRepository.createMessage(message)
                    messageToBeSent = message

                }
            }
        }




    fun updateTextBox(text: String) {
        mText = text
    }

}

data class ChatState(
    val chatId: String? = null,
    val username: String? = null,
    val messages: List<MessageItem> = emptyList(),
    val selectedMessages: List<String> = emptyList(),
    val selectionScreen: Boolean = false
)
