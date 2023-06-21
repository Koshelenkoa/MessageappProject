package com.example.myapplication

import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxScopeInstance.align
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.concurrent.Executor

class AuthenticationActivity : ComponentActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var callback: BiometricPrompt.AuthenticationCallback
    private val cancellationSignal: CancellationSignal = CancellationSignal()
    private lateinit var crypto: BiometricPrompt.CryptoObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        executor = ContextCompat.getMainExecutor(this)
        crypto = BiometricPrompt.CryptoObject.ini

        biometricPrompt = BiometricPrompt.Builder(baseContext)
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .build()

        callback = object: BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int,
                                               errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext,
                    "Authentication error: $errString", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext,
                    "Authentication succeeded!", Toast.LENGTH_SHORT)
                    .show()
                val cipher = result.cryptoObject?.cipher
                val i: Intent = Intent(this@AuthenticationActivity,
                    MainActivity.class)
                startActivity(i)
            }

            ''
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Authentication failed",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                androidx.compose.material.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material.MaterialTheme.colors.background
                ) {
                    Column(){

                    Button(onClick = {
                        biometricPrompt.authenticate(crypto, cancellationSignal, executor, callback)}
                    , modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.2f)
                    ) {
                        Text("Log in")
                    }
                }
            }}
        }

    }


}




@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {

    }
}

