package com.koshelenkoa.securemessaging.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.koshelenkoa.securemessaging.util.AuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegistrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegistrationState())
    val uiState: StateFlow<RegistrationState> = _uiState.asStateFlow()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val auth = AuthManager()
    var login by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var loaded by mutableStateOf(false)
        private set


    fun updatePassword(newPassword: String) {
        password = newPassword
    }

    fun updateLogin(login: String) {
        this.login = login
    }

    fun loadedSuccessfully() {
        loaded = true
    }

    fun passwordStrength(isStrong: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                user = currentState.user,
                passwordIsValid = isStrong
            )

        }
    }
}

data class RegistrationState(
    val passwordIsValid: Boolean = false,
    val user: FirebaseUser? = null,
)