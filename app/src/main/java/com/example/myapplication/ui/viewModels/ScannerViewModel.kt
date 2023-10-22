package com.example.myapplication.ui.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ScannerViewModel: ViewModel() {
    var chatId by mutableStateOf("")
        private set

    fun updateChatId(chatId: String?){
        if(chatId != null) {
            this.chatId = chatId
        }
    }
}