package com.koshelenkoa.securemessaging

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.koshelenkoa.securemessaging.compose.AuthenticationScreen
import com.koshelenkoa.securemessaging.compose.buildBiometricPrompt
import com.koshelenkoa.securemessaging.ui.theme.MyApplicationTheme
import com.koshelenkoa.securemessaging.util.SharedPrefsHelper.Companion.retriveCredentials
import com.koshelenkoa.securemessaging.viewModels.AuthenticationViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor


class MainActivity : FragmentActivity() {
    private lateinit var executor: Executor
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val sharedPrefs = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val fontsize = sharedPrefs.getInt("fontsize", 20)

        val viewModel: AuthenticationViewModel by viewModels()
        val biometricPrompt = buildBiometricPrompt(this)


        executor = ContextCompat.getMainExecutor(this)
        retriveCredentials(this, { viewModel.updatePassword(it) }, { viewModel.updateLogin(it) })

        setContent {
            MyApplicationTheme {
                Surface {
                    AuthenticationScreen(
                        auth = auth,
                        activity = this@MainActivity,
                        fontsize = fontsize,
                        biometricPrompt = biometricPrompt
                    )
                }
            }
        }
    }
}


