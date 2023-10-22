package com.example.myapplication.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.fonts.FontStyle
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.myapplication.util.PasswordStrengthChecker
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.ui.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.RegistrationViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


val TAG = "Registration Activity"

class RegistrationActivity : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "resumed")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val fontsize = sharedPrefs.getInt("fontsize", 20)
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d(TAG, "MainApplication can authenticate using biometrics.")

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e(TAG, "No biometric features available on this device.")

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                startActivity(enrollIntent)
            }

        }

            val auth = Firebase.auth
            setContent {
                MyApplicationTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {

                        RegistrationContent(auth = auth, fontsize = fontsize)
                    }
                }
            }
        }
    }


@Composable
fun RegistrationContent(viewModel: RegistrationViewModel = viewModel(), auth:FirebaseAuth, fontsize : Int) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier
        .padding(horizontal = 50.dp)
        .padding(top = 170.dp))  {
        UserLogin(fontsize = fontsize, login = viewModel.login, updateLogin = {viewModel.updateLogin(it)})
        PasswordTextField(fontsize = fontsize, password = viewModel.password,
            updatePassword = {viewModel.updatePassword(it)},
            passwordStrengthUpdate = {viewModel.passwordStrength(it)})
        LoginButton( context = context, fontsize = fontsize, login = viewModel.login,
            password = viewModel.password, passwordIsValid = uiState.passwordIsValid,
            updateUser ={ viewModel.updateUser(it)},
            auth = FirebaseAuth.getInstance())
    }
}

        @OptIn(ExperimentalComposeUiApi::class)
        @Composable
        fun UserLogin(fontsize: Int, login: String, updateLogin: (String) -> Unit) {

            val context = LocalContext.current
            val keyboardController = LocalSoftwareKeyboardController.current
            OutlinedTextField(
                value = login,
                onValueChange = updateLogin,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                placeholder = { Text(context.resources.getString(R.string.login_placeholder)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide()}),
                textStyle = TextStyle.Default.copy(fontSize = fontsize.sp)
                )

        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun PasswordTextField(
            password: String,
            modifier: Modifier = Modifier,
            semanticContentDescription: String = "",
            validateStrengthPassword: Boolean = false,
            hasError: Boolean = false,
            onHasStrongPassword: (isStrong: Boolean) -> Unit = {},
            fontsize: Int,
            updatePassword: (String) -> Unit,
            passwordStrengthUpdate: (Boolean) -> Unit
        ) {
            val focusManager = LocalFocusManager.current
            val showPassword = remember { mutableStateOf(false) }
            val color = remember { mutableStateOf(Color.Red) }
            val checker = PasswordStrengthChecker()
            Column(
                modifier = modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = semanticContentDescription },
                    value = password,
                    onValueChange = updatePassword,
                    textStyle = TextStyle.Default.copy(fontSize = fontsize.sp),
                    maxLines = 1,
                    placeholder = {

                        Text(
                            text = LocalContext.current.resources.getString(R.string.password_placeholder)
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true,
                    isError = hasError,
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
                    }

                )
                Spacer(modifier = Modifier.height(8.dp))
                if (password != "") {
                    if (strongPassword(password, passwordStrengthUpdate)) {
                        onHasStrongPassword(true)
                    } else {
                        onHasStrongPassword(false)
                    }
                    Text(
                        modifier = Modifier.semantics {
                            contentDescription = "StrengthPasswordMessage"
                        },
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            ) {
                                withStyle(style = SpanStyle(color = colorResource(id = R.color.orange200))) {
                                    if (!checker.hasLength(password)) {
                                        append(LocalContext.current.resources.getString(R.string.warning_password_too_short))
                                    } else if (!checker.hasUppercase(password)){
                                        append(LocalContext.current.resources.getString(R.string.warning_password_add_uppercase))
                                    } else if (!checker.hasDigit(password)) {
                                        append(LocalContext.current.resources.getString(R.string.warning_password_add_digit))
                                    } else if (!checker.hasSymbol(password)) {
                                        append(LocalContext.current.resources.getString(R.string.warning_password_add_symbol))
                                    } else if (!checker.hasLowercase(password)) {
                                        append(LocalContext.current.resources.getString(R.string.warning_password_add_lowercase))
                                    } else {
                                        append(LocalContext.current.resources.getString(R.string.password_okay))
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }


@Composable
fun LoginButton(
    auth: FirebaseAuth,
    context: Context,
    fontsize: Int,
    login: String?,
    password: String?,
    passwordIsValid: Boolean,
    updateUser: (FirebaseUser?) -> Unit
) {

    Button(
        onClick = {
            if (passwordIsValid) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }
                    // Get new FCM registration token
                    val token = task.result
                    createUser(
                        login = login, password = password,
                        auth = auth, context = context,
                        token = token, updateUser = updateUser
                    )
                })
            }else{
                Toast.makeText(
                    context,
                    "Password is invalid",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    {
        Text(text = "${LocalContext.current.resources.getString(R.string.register_button)}",
            fontSize = fontsize.sp)
    }
}

fun createUser(
    login: String?,
    password: String?,
    auth: FirebaseAuth,
    context: Context,
    token: String,
    updateUser: (FirebaseUser?) -> Unit
) {
    if(login!=null) {
        if(password!=null){
        auth.createUserWithEmailAndPassword("${login.trim()}@email.com", password.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = token }

                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User profile updated.")
                                updateUser(user)
                                context.startActivity(Intent(context, MainActivity::class.java))
                            } else {
                                Log.w(TAG, "error binding accaunt to token")
                            }
                        }
                } else{
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUser(null)
                }
            }
        }else{
            Toast.makeText(context, "Password can not be empty", Toast.LENGTH_SHORT)
        }
    }else{
        Toast.makeText(context, "Login can not be empty", Toast.LENGTH_SHORT)
    }
}

fun loginOkay(login: String): Boolean {
    return login matches "[A-Za-z0-9_]{6,}".toRegex()
}

fun strongPassword(text: String, passwordStrengthUpdate: (Boolean) -> Unit): Boolean {
    val checker = PasswordStrengthChecker()
    val isStrong = checker.hasLength(text) && checker.hasDigit(text)
            && checker.hasSymbol(text) && checker.hasUppercase(text) && checker.hasLowercase(text)
    Log.d("Registration Activity", "password strong : ${isStrong.toString()}")
    passwordStrengthUpdate(isStrong)
    return isStrong
}

