package com.koshelenkoa.securemessaging.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ScannerViewModel : ViewModel() {
    var chatId by mutableStateOf("")
        private set
    var step by mutableStateOf("")
        private set

    fun updateStep(step: String) {
        this.step = step

    }

    fun updateChatId(chatId: String) {
        this.chatId = chatId
    }
}