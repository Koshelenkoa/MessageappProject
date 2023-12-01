package com.koshelenkoa.securemessaging.viewModels

import androidx.lifecycle.ViewModel
import com.koshelenkoa.securemessaging.cryptography.MessageEncryption
import com.koshelenkoa.securemessaging.data.local.ChatForUI
import com.koshelenkoa.securemessaging.data.local.ChatRepository
import com.koshelenkoa.securemessaging.data.local.Message
import com.koshelenkoa.securemessaging.data.local.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val messagesRepository: MessagesRepository,
) : ViewModel() {
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
                        val messageEncryption = MessageEncryption(
                            chatRepository,
                            chat.chat_id
                        )
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