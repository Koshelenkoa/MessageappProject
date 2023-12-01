package com.koshelenkoa.securemessaging.util

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SharedPrefsHelper {
    companion object {
        fun getFontSize(context: Context): Int {
            val sharedPrefs =
                context.getSharedPreferences("sharedPrefs", ComponentActivity.MODE_PRIVATE)
            return sharedPrefs.getInt("fontsize", 20)
        }

        fun saveCredentials(login: String, password: String, context: Context) {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPreferences = context.getSharedPreferences("sharedPrefs",
                Context.MODE_PRIVATE
            )
            val encyptedSharedPrefs = EncryptedSharedPreferences.create(
                "secret_shared_prefs",
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            encyptedSharedPrefs
                .edit()
                .putString("password", password)
                .commit()

            sharedPreferences
                .edit()
                .putString("login", login)
                .commit()
        }

        fun retriveCredentials(
            context: Context, updatePasword: (String) -> Unit?,
            updateLogin: (String) -> Unit?,
        ) {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPreferences = context.getSharedPreferences("sharedPrefs",
                Context.MODE_PRIVATE
            )
            val encryptedSharedPrefs = EncryptedSharedPreferences.create(
                "secret_shared_prefs",
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val login = sharedPreferences.getString("login", "") ?: ""
            val password = encryptedSharedPrefs.getString("password", "") ?: ""

            updatePasword(password)
            updateLogin(login)
            Log.d("MainActivity", "Credential retrived")

        }
    }
}
