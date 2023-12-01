package com.koshelenkoa.securemessaging.ui.activities

import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE
import com.koshelenkoa.securemessaging.compose.RegistrationContent
import com.koshelenkoa.securemessaging.compose.launchNextActivity
import com.koshelenkoa.securemessaging.ui.ui.theme.MyApplicationTheme
import com.koshelenkoa.securemessaging.util.SharedPrefsHelper.Companion.getFontSize
import com.koshelenkoa.securemessaging.viewModels.RegistrationViewModel


val TAG = "Registration Activity"

class RegistrationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fontsize = getFontSize(this)
        val viewModel: RegistrationViewModel by viewModels()

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.ACTION_BIOMETRIC_ENROLL,
                        BIOMETRIC_STRONG )
                }
                startActivityForResult(enrollIntent, REQUEST_CODE)
            }
        }
        setContent {
            launchNextActivity(loaded = viewModel.loaded)
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    RegistrationContent(
                        viewModel = viewModel,
                        fontsize = fontsize,
                        activity = this
                    )
                }
            }
        }
    }
}

