package com.example.myapplication

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainScreenViewModel : ViewModel() {
    val TAG: String = "MainScreenViewModel"
    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()
    var textBox by mutableStateOf("")
    private val encryptor = MsgCipherDecipher()
   fun encryptDecryptText(text: String){
       var mtext : String?
       try {
           mtext = encryptor.returnFinalMessage(text)
       }catch (e: Exception){
           mtext = "Error"
           Log.d(TAG, " " + e.message)
       }
        _uiState.update{ currentState ->
            currentState.copy(
                outputText = mtext
            )
        }
   }

    fun updateEnteredText(text: String){
        textBox = text

    }
}

data class MainScreenState(
    val outputText: String? = null
)