package com.koshelenkoa.securemessaging.compose

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.koshelenkoa.securemessaging.MainActivity
import com.koshelenkoa.securemessaging.R
import com.koshelenkoa.securemessaging.services.MyFirebaseMessagingService
import com.koshelenkoa.securemessaging.ui.activities.RegistrationActivity
import com.koshelenkoa.securemessaging.ui.activities.TAG
import com.koshelenkoa.securemessaging.util.AuthManager
import com.koshelenkoa.securemessaging.util.PasswordStrengthChecker
import com.koshelenkoa.securemessaging.viewModels.RegistrationViewModel
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun launchNextActivity(loaded: Boolean) {
    val context = LocalContext.current
    if (loaded) {
        context.startActivity(Intent(context, MainActivity::class.java))
    }
}


@Composable
fun RegistrationContent(
    viewModel: RegistrationViewModel,
    fontsize: Int,
    activity: RegistrationActivity,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier
            .padding(horizontal = 50.dp)
            .padding(top = 170.dp)
    ) {
        UserLogin(
            fontsize = fontsize,
            login = viewModel.login,
            updateLogin = { viewModel.updateLogin(it) })
        PasswordTextField(fontsize = fontsize, password = viewModel.password,
            updatePassword = { viewModel.updatePassword(it) },
            passwordStrengthUpdate = { viewModel.passwordStrength(it) })
        LoginButton(
            context = context, fontsize = fontsize, login = viewModel.login,
            password = viewModel.password, passwordIsValid = uiState.passwordIsValid,
            activity = activity
        )
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
        label = {
            Text(
                text = context.resources.getString(R.string.login_placeholder),
                fontSize = fontsize.sp
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() }),
        textStyle = TextStyle.Default.copy(fontSize = fontsize.sp)
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    password: String,
    modifier: Modifier = Modifier,
    semanticContentDescription: String = "",
    hasError: Boolean = false,
    onHasStrongPassword: (isStrong: Boolean) -> Unit = {},
    fontsize: Int,
    updatePassword: (String) -> Unit,
    passwordStrengthUpdate: (Boolean) -> Unit,
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
            label = {
                Text(
                    text = LocalContext.current.resources.getString(R.string.password_placeholder),
                    fontSize = fontsize.sp
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
                        withStyle(style = SpanStyle(color = color.value, fontSize = 15.sp)) {
                            if (!checker.hasLength(password)) {
                                append(LocalContext.current.resources.getString(R.string.warning_password_too_short))
                            } else if (!checker.hasUppercase(password)) {
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
    context: Context,
    fontsize: Int,
    login: String?,
    password: String?,
    passwordIsValid: Boolean,
    activity: RegistrationActivity,
) {

    Button(
        onClick = {
            if (passwordIsValid) {
                createUser(
                    login = login, password = password,
                    context = context, activity = activity
                )
            } else {
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
        Text(
            text = "${LocalContext.current.resources.getString(R.string.register_button)}",
            fontSize = fontsize.sp
        )
    }
}

fun createUser(
    login: String?,
    password: String?,
    context: Context,
    activity: RegistrationActivity,
) {
    val auth = AuthManager()
    auth.createUser(
        login = login,
        password = password,
        onSuccess = { user ->
            val messagingService = MyFirebaseMessagingService()
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                messagingService.sendRegistrationToServer(it.result)
            }
            Log.d(TAG, "createUserWithEmail:success")
            context.startActivity(Intent(activity, MainActivity::class.java))
        },
        onFailure = { exceptionMessage ->
            Log.w(TAG, "createUserWithEmail:failure $exceptionMessage")
            Toast.makeText(
                context,
                exceptionMessage,
                Toast.LENGTH_SHORT,
            ).show()
        },
    )
}

fun strongPassword(text: String, passwordStrengthUpdate: (Boolean) -> Unit): Boolean {
    val checker = PasswordStrengthChecker()
    val isStrong = checker.hasLength(text) && checker.hasDigit(text)
            && checker.hasSymbol(text) && checker.hasUppercase(text) && checker.hasLowercase(text)
    Log.d("Registration Activity", "password strong : $isStrong")
    passwordStrengthUpdate(isStrong)
    return isStrong
}