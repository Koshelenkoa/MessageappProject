package com.example.myapplication.util

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import kotlinx.coroutines.CompletableDeferred

class AuthManager {

    /**
     * called to get Authentication token for authorized http request
     */
    fun getAuthToken(): Task<GetTokenResult> {
        val mUser = FirebaseAuth.getInstance().currentUser!!
        return mUser.getIdToken(true)
    }

    fun getUid(): String?{
        val user = FirebaseAuth.getInstance().getCurrentUser()
        return user?.uid
    }

    /**
     * create User in Firebase Auth
     * If successful returns FirebaseUser
     * If failed returns null
     * @param login - userId
     * @param password
     */
    suspend fun createUser(login: String, password: String): FirebaseUser? {
        val def = CompletableDeferred<FirebaseUser?>()
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(login, password)
            .addOnCompleteListener { task ->
                def.complete(
                    if (task.isSuccessful)
                        task.result?.user
                    else
                        null
                )
            }
        return def.await()
    }

    /**
     *
     */
    suspend fun signInUser(login: String, password: String): FirebaseUser? {
        val def = CompletableDeferred<FirebaseUser?>()
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(login, password)
        return def.await()
    }

    /**
     * sending device token though the setUserData as displayname for easy retrival on the server
     */
    suspend fun sendToken() {

    }

}