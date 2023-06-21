package com.example.myapplication


import android.content.Context
import androidx.lifecycle.viewmodel.compose.viewModel
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.tasks.OnCompleteListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.material.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    val mContext = baseContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainContent()
                }
            }
        }

    }

fun getContext(): Context? {
    return mContext
}
}


@Composable
fun MainContent(
    viewModel: MainScreenViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    val mContext: Context = context
    val uiState by viewModel.uiState.collectAsState()
    val paddingValue = 15.dp
    var mText : String = viewModel.textBox
    Column(
        verticalArrangement = Arrangement.spacedBy(space = 0.dp, alignment = Alignment.CenterVertically)
    ){

        TextField(
            value = mText,
            onValueChange = {mText = it
                             viewModel.updateEnteredText(it)},
            modifier = Modifier
                .padding(paddingValue)
                .background(MaterialTheme.colors.primary)
                .align(Alignment.CenterHorizontally)
                .height(180.dp)
                .fillMaxWidth(),
            placeholder = { Text(text = "Enter something...", color = Color.LightGray, fontSize = 20.sp) })

        Button(
            onClick = { if(!viewModel.textBox.equals("")){
                    viewModel.encryptDecryptText(viewModel.textBox)} } ,
            modifier = Modifier.padding(paddingValue)
                .background(MaterialTheme.colors.secondary)
                .align(Alignment.CenterHorizontally)
                .height(45.dp)
                .width(100.dp)) {
            Text("Enter", fontSize = 20.sp)
        }
        Button(
            onClick = {
                getToken(mContext)} ,
            modifier = Modifier.padding(paddingValue)
                .background(MaterialTheme.colors.secondary)
                .align(Alignment.CenterHorizontally)
                .height(45.dp)
                ) {
            Text("Show Toast", fontSize = 20.sp)
        }


        Text("$uiState.outputText",
             color = Color.White, modifier = Modifier
            .padding(paddingValue)
            .background(MaterialTheme.colors.primary)
            )
        }


}

fun getToken(context : Context){
    val TAG = "Main activity"
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
        if (!task.isSuccessful) {
            Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            return@OnCompleteListener
        }

        // Get new FCM registration token
        val token = task.result

        // Log and toast
        val msg = token
        Log.d(TAG, msg)
        Toast.makeText( context, msg, Toast.LENGTH_SHORT).show()
    })
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
            MainContent()
        }
    }

