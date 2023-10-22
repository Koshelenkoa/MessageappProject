package com.example.myapplication

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.fonts.FontStyle
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.myapplication.cryptography.CipherHolder.Companion.setCipher
import com.example.myapplication.ui.activities.ChatList
import com.example.myapplication.ui.activities.RegistrationActivity
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.AuthenticationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.concurrent.Executor
import javax.crypto.Cipher


class MainActivity() : FragmentActivity() {

    private lateinit var executor: Executor
    //private var cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
    //private var crypto: BiometricPrompt.CryptoObject = BiometricPrompt.CryptoObject(cipher)
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val sharedPrefs = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val fontsize = sharedPrefs.getInt("fontsize", 20)

        val currentUser = auth.currentUser
        val viewModel: AuthenticationViewModel by viewModels()
        viewModel.updateUser(user = currentUser)

        executor = ContextCompat.getMainExecutor(this)
        retriveCredentials(this, {viewModel.updatePassword(it)}, {viewModel.updateLogin(it)})

        setContent {
            MyApplicationTheme {
                Surface {
                    AuthContent(
                        auth = auth,
                        activity = this@MainActivity,
                        fontsize = fontsize
                    )
                }
            }
        }

    }
}

@Composable
fun AuthContent(
    authenticationViewModel: AuthenticationViewModel = viewModel(),
    context: Context = LocalContext.current,
    auth: FirebaseAuth,
    activity: MainActivity,
    fontsize: Int
) {
    val biometricPrompt = buildBiometricPrompt(context)
    val viewModel: AuthenticationViewModel = authenticationViewModel

    Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.padding(horizontal = 50.dp)
        .padding(top = 170.dp)) {
        LoginTextBox(updateUI = {
            viewModel.updateLogin(it)},
            userLogin = viewModel.login,
            fontsize = fontsize)

        PasswordTextBox(updateUI = {
            viewModel.updatePassword(it)},
            password = viewModel.password,
            fontsize = fontsize)

        Buttons(
            context = context,
            auth = auth, biometricPrompt = biometricPrompt,
            activity = activity, packageContext = context,
            password = viewModel.password, login = viewModel.login,
            updateUI = {viewModel.updateUser(it)},
            fontsize = fontsize, user = viewModel.user
        )
    }


}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginTextBox(updateUI: (String)-> Unit, userLogin: String, fontsize: Int) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current.resources
    OutlinedTextField(
        value = userLogin,
        onValueChange = updateUI ,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 1,
        label  = { Text(text = context.getString(R.string.user_id_placeholder),
            fontSize = fontsize.sp) },
        textStyle = TextStyle.Default.copy(fontSize = fontsize.sp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide()})
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PasswordTextBox( updateUI: (String) -> Unit, password: String, fontsize: Int) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val showPassword = remember { mutableStateOf(false) }
        OutlinedTextField(
        value = password,
            textStyle = TextStyle.Default.copy(fontSize = fontsize.sp),
        onValueChange = updateUI,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide()}),
        label = { Text(LocalContext.current.resources.getString(R.string.password_placeholder),
                fontSize = fontsize.sp )},
        visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
        val (icon, iconColor) = if (showPassword.value) {
            Pair(
                Icons.Filled.Visibility,
                colorResource(id = R.color.orange200)
            )
        } else {
            Pair(Icons.Filled.VisibilityOff, colorResource(id = R.color.orange200))
        }

        IconButton(onClick = { showPassword.value = !showPassword.value }) {
            Icon(
                icon,
                contentDescription = "Visibility",
                tint = iconColor
            )
        }
    })
}


fun signIn(
    user: FirebaseUser?,
    userLogin: String?,
    password: String?,
    context: Context,
    auth: FirebaseAuth,
    biometricPrompt: BiometricPrompt,
    activity: MainActivity,
    updateUI: (FirebaseUser?) -> Unit
    ) {
    val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.resources.getString(R.string.biometric_title))
        .setSubtitle(context.resources.getString(R.string.biometric_subtitle))
        .setNegativeButtonText(context.resources.getString(R.string.biometric_prompt_negative_button))
        .build()

    val TAG = "Authentication Sign In"
    if (userLogin != null && userLogin != "") {
        if(password != null && password != "") {
            if(user == null){
            auth.signInWithEmailAndPassword("$userLogin@email.com", password)
                .addOnCompleteListener(
                    activity
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signIn:success")
                        //save login and password
                        saveCredentials(login = userLogin, password = password, context = context)
                        val user: FirebaseUser? = auth.currentUser
                        biometricPrompt.authenticate(
                            biometricPromptInfo
                        )
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmailAndPassword:failure", task.exception)
                        Toast.makeText(
                            activity, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI(null)
                    }
                }
            }else{
                biometricPrompt.authenticate(biometricPromptInfo)
            }
        }
    }
}


fun buildBiometricPrompt(context: Context): BiometricPrompt{
    val fragmentContext = context as FragmentActivity
    val biometricPrompt = BiometricPrompt(fragmentContext,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence,
            ) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(
                    context,
                    "Authentication error: $errString", Toast.LENGTH_SHORT
                )
                    .show()
            }

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult,
            ) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(
                    context,
                    "Authentication succeeded!", Toast.LENGTH_SHORT
                )
                    .show()
                val bioCipher = result.cryptoObject?.cipher
                if (bioCipher != null) {
                    setCipher(bioCipher)
                }

                val intent = Intent(
                    context,
                    ChatList::class.java
                )
                context.startActivity(intent)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(context, "Authentication failed",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    )
    return biometricPrompt

}
@Composable
fun Buttons(
    packageContext: Context,
    context: Context,
    auth: FirebaseAuth,
    biometricPrompt: BiometricPrompt,
    activity: MainActivity,
    password: String,
    login: String,
    updateUI: (FirebaseUser?) -> Unit,
    fontsize: Int,
    user: FirebaseUser?
) {
    var userLogin = login.trim()
    var password = password.trim()
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Button(
            onClick = {
                signIn(
                    user,
                    userLogin, password,  context, auth,
                    biometricPrompt, activity, updateUI
                )
            },
            modifier = Modifier.fillMaxWidth()

        ) {
            Text(text = context.resources.getString(R.string.login_button),
                fontSize = fontsize.sp)
        }

        Button(
            onClick = {
                val intent = Intent(
                    packageContext,
                    RegistrationActivity::class.java
                )
                packageContext.startActivity(
                    intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(context.resources.getString(R.string.register_button),
                fontSize = fontsize.sp )
        }
    }
}


fun saveCredentials(login: String, password: String, context: Context) {
    val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    val sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE)
    val encyptedSharedPrefs = EncryptedSharedPreferences.create(
        "secret_shared_prefs",
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

    encyptedSharedPrefs
        .edit()
        .putString("password", password)
        .commit()

    sharedPreferences
        .edit()
        .putString("login", login)
        .commit()
}

fun retriveCredentials(context: Context, updatePasword: (String)-> Unit,
    updateLogin: (String) -> Unit){
    val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    val sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE)
    val encryptedSharedPrefs = EncryptedSharedPreferences.create(
        "secret_shared_prefs",
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

    val login = sharedPreferences.getString("login", "")?: ""
    val password = encryptedSharedPrefs.getString("password", "")?: ""
    if (!login.equals("") && !password.equals("")) {
        updatePasword(password)
        updateLogin(login)
        Log.d("MainActivity", "Credential retrived")
    }
}
