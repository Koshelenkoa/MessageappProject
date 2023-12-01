package com.koshelenkoa.securemessaging.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthenticationViewModel : ViewModel() {
    val auth = FirebaseAuth.getInstance()
    var login by mutableStateOf("")
        private set
    var user: FirebaseUser? by mutableStateOf(auth.currentUser)
        private set

    var password by mutableStateOf("")
        private set
    var loginAutomatically by mutableStateOf(true)
        private set

    fun updatePassword(password: String) {
        this.password = password
    }

    fun updateLogin(login: String) {
        this.login = login
    }

    fun updateUser(user: FirebaseUser?) {
        this.user = user
    }

    fun declineLogin() {
        loginAutomatically = false
    }
}
