package com.example.myapplication.ui.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegistrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegistrationState())
    val uiState: StateFlow<RegistrationState> = _uiState.asStateFlow()

    var login by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set


    fun updatePassword(newPassword: String){
        password = newPassword
    }

    fun updateLogin(login: String){
        this.login = login
    }

    fun updateUser(user: FirebaseUser?){
        Log.d("Registration ViewModel", "User update" )
        _uiState.update {currentState ->
            currentState.copy(
                user = currentState.user,
                passwordIsValid = currentState.passwordIsValid)
        }
    }

    fun passwordStrength(isStrong: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                user =  currentState.user,
                passwordIsValid = isStrong
            )

        }
    }
}

data class RegistrationState(
    val passwordIsValid: Boolean = false,
    val user: FirebaseUser? = null
)