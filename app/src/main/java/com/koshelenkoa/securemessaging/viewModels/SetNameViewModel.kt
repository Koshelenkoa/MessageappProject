package com.koshelenkoa.securemessaging.viewModels

import androidx.lifecycle.ViewModel
import com.koshelenkoa.securemessaging.data.local.Chat
import com.koshelenkoa.securemessaging.data.local.ChatDataHolder
import com.koshelenkoa.securemessaging.data.local.ChatRepository
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
class SetNameViewModel @Inject constructor(private val chatRepository: ChatRepository) :
    ViewModel() {
    lateinit var _uiState: MutableStateFlow<SetNameState>
    lateinit var uiState: StateFlow<SetNameState>

    fun setChat(chat: String) {
        _uiState = MutableStateFlow(SetNameState(chat))
        uiState = _uiState.asStateFlow()
    }

    fun setName(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val chat = ChatDataHolder.chat!!
            chatRepository.createChat(
                Chat(
                    chat_id = chat.chat_id,
                    nick = name,
                    alias = chat.alias,
                    publicKey = chat.publicKey,
                    timestamp = chat.timestamp,
                )
            )
            ChatDataHolder.setChat(null)
        }
    }

    fun update(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                chat = currentState.chat,
                name = name
            )
        }
    }
}

data class SetNameState(
    val chat: String,
    val name: String? = null,
)