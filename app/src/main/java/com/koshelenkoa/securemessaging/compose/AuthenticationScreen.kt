package com.koshelenkoa.securemessaging.compose

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.koshelenkoa.securemessaging.MainActivity
import com.koshelenkoa.securemessaging.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.koshelenkoa.securemessaging.ui.activities.ChatList
import com.koshelenkoa.securemessaging.ui.activities.RegistrationActivity
import com.koshelenkoa.securemessaging.util.AuthManager
import com.koshelenkoa.securemessaging.util.SharedPrefsHelper.Companion.saveCredentials
import com.koshelenkoa.securemessaging.viewModels.AuthenticationViewModel

@Composable
    fun AuthenticationScreen(
    authenticationViewModel: AuthenticationViewModel = viewModel(),
    context: Context = LocalContext.current,
    auth: FirebaseAuth,
    activity: MainActivity,
    fontsize: Int,
    biometricPrompt: BiometricPrompt,
    ) {
        val viewModel: AuthenticationViewModel = authenticationViewModel

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(horizontal = 50.dp)
                .padding(top = 170.dp)
        ) {
            LoginTextBox(
                updateUI = {
                    viewModel.updateLogin(it)
                },
                userLogin = viewModel.login,
                fontsize = fontsize
            )

            PasswordTextBox(
                updateUI = {
                    viewModel.updatePassword(it)
                },
                password = viewModel.password,
                fontsize = fontsize
            )

            Buttons(
                context = context,
                auth = auth, biometricPrompt = biometricPrompt,
                activity = activity, packageContext = context,
                password = viewModel.password, login = viewModel.login,
                updateUI = { viewModel.updateUser(it) },
                fontsize = fontsize, user = auth.currentUser
            )
        }


    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun LoginTextBox(updateUI: (String) -> Unit, userLogin: String, fontsize: Int) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val context = LocalContext.current.resources
        OutlinedTextField(
            value = userLogin,
            onValueChange = updateUI,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            label = {
                Text(
                    text = context.getString(R.string.user_id_placeholder),
                    fontSize = fontsize.sp
                )
            },
            textStyle = TextStyle.Default.copy(fontSize = fontsize.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() })
        )

    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun PasswordTextBox(updateUI: (String) -> Unit, password: String, fontsize: Int) {
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
                onDone = { keyboardController?.hide() }),
            label = {
                Text(
                    LocalContext.current.resources.getString(R.string.password_placeholder),
                    fontSize = fontsize.sp
                )
            },
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
        updateUI: (FirebaseUser?) -> Unit,
    ) {
        val TAG = "Authentication Sign In"
        val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.resources.getString(R.string.biometric_title))
            .setSubtitle(context.resources.getString(R.string.biometric_subtitle))
            .setNegativeButtonText(context.resources.getString(R.string.biometric_prompt_negative_button))
            .build()

        try{
            val authManager = AuthManager()
            authManager.signInUser(
                login = userLogin,
                password = password,
                onSuccess = { user ->
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signIn:success")
                    //save login and password
                    saveCredentials(
                        login = userLogin!!,
                        password = password!!, //nullability checked inside the method
                        context = context
                    )
                    val user: FirebaseUser? = auth.currentUser
                    biometricPrompt.authenticate(
                        biometricPromptInfo
                    )
                    updateUI(user)
                },
                onFailure = { exceptionMessage ->
                    // If sign in fails, display a message to the user.
                    if(user != null && user.email.equals("$userLogin@email.com") && password!!.isNotBlank()) {
                        biometricPrompt.authenticate(biometricPromptInfo)
                    }else {
                        Log.w(TAG, "signInWithEmailAndPassword:failure $exceptionMessage")
                        Toast.makeText(
                            activity, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI(null)
                    }
                }
            )
        } catch (e: Exception){
            if(user != null) {
                biometricPrompt.authenticate(biometricPromptInfo)
            }
        }
    }


    fun buildBiometricPrompt(context: Context): BiometricPrompt {
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
                    ).show()

                    val intent = Intent(
                        context,
                        ChatList::class.java
                    )
                    context.startActivity(intent)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        context, "Authentication failed",
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
        user: FirebaseUser?,
    ) {
        val userLogin = login.trim()
        val password = password.trim()

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(
                onClick = {
                    signIn(
                        user,
                        userLogin, password, context, auth,
                        biometricPrompt, activity, updateUI
                    )
                },
                modifier = Modifier.fillMaxWidth()

            ) {
                Text(
                    text = context.resources.getString(R.string.login_button),
                    fontSize = fontsize.sp
                )
            }

            Button(
                onClick = {
                    val intent = Intent(
                        packageContext,
                        RegistrationActivity::class.java
                    )
                    packageContext.startActivity(
                        intent
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    context.resources.getString(R.string.register_button),
                    fontSize = fontsize.sp
                )
            }
        }
    }
