package com.koshelenkoa.securemessaging.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {

    /**
     * create User in Firebase Auth
     * @param login
     * @param password
     * @param onSuccess passed FirebaseUser? called when task is successful
     * @param OnFailure passed String with a exception message from firebase and local code
     */
    fun createUser(
        login: String?,
        password: String?,
        onSuccess: (FirebaseUser?) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        when {
            login.isNullOrEmpty() -> {
                onFailure("Login can not be empty")
            }

            password.isNullOrEmpty() -> {
                onFailure("Password can not be empty")
            }

            else -> {
                val auth = FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword("${login.trim()}@email.com", password.trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            try {
                                onSuccess(task.result.user)
                            } catch (e: Exception) {
                                e.message?.let { onFailure(it) }
                                auth.currentUser?.delete()
                            }
                        } else {
                            task.exception?.message?.let { onFailure(it) }
                        }
                    }
            }
        }
    }

    /**
     * sign ins User in Firebase Auth
     * @param login
     * @param password
     * @param onSuccess passed FirebaseUser? called when task is successful
     * @param OnFailure passed String with a exception message from firebase and local code
     */
    fun signInUser(
        login: String?,
        password: String?,
        onSuccess: (FirebaseUser?) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        when {
            login.isNullOrEmpty() -> {
                onFailure("Login can not be empty")
            }

            password.isNullOrEmpty() -> {
                onFailure("Password can not be empty")
            }

            else -> {
                val auth = FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword("${login.trim()}@email.com", password.trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            try {
                                onSuccess(task.result.user)
                            } catch (e: Exception) {
                                e.message?.let { onFailure(it) }
                            }
                        } else {
                            task.exception?.message?.let { onFailure(it) }
                        }
                    }
            }
        }
    }
}