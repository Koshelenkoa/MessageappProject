package com.example.myapplication.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.cryptography.MessageEncryption
import com.example.myapplication.data.local.ChatForUI
import com.example.myapplication.data.local.ChatRepository
import com.example.myapplication.data.local.Message
import com.example.myapplication.data.local.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val messagesRepository: MessagesRepository) : ViewModel() {
    private val messageEncryption: MessageEncryption = MessageEncryption(chatRepository)
    private val _uiState = MutableStateFlow(ChatListState())
    val uiState: StateFlow<ChatListState> = _uiState.asStateFlow()
    fun loadChats() {
        CoroutineScope(Dispatchers.IO).launch {
            chatRepository.loadChats().collect { chats ->
                val chatItems: MutableList<ChatForUI> = mutableListOf()
                for (chat in chats) {
                    val lastMessage = messagesRepository.loadLastMessage(chat.chat_id)
                    var lastText: Message? = null

                    if (lastMessage != null) {
                        lastText = messageEncryption.decryptMessage(lastMessage)
                    }
                    var chatItem: ChatForUI = ChatForUI.createChatForUi(chat, lastText)
                    chatItems.add(chatItem)
                }

                val chatItemList = chatItems.toList()
                    .sortedBy { it.timestamp }.asReversed()
                _uiState.update { currentState ->
                    currentState.copy(
                        chatList = chatItemList
                    )
                }
            }
        }

    }
}

data class ChatListState(val chatList: List<ChatForUI> = emptyList())