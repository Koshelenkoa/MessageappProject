package com.example.myapplication.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.ui.text.font.font
import com.example.myapplication.Buttons
import com.example.myapplication.R
import com.example.myapplication.ui.activities.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.RegistrationViewModel
import com.example.myapplication.ui.viewModels.SetNameViewModel
import java.io.IOException

class SetNameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chat = intent.getStringExtra("chat")
        val viewModel: SetNameViewModel by viewModels()
        if (chat != null) {
            viewModel.setChat(chat)
        }else {
            Log.d("SetNameActivity", "chat variable is null")
            throw IOException()
        }
        val sharedPrefs = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val fontSize = sharedPrefs.getInt("fontsize", 20)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TextBox(viewModel = viewModel, fontsize = fontSize)
                }
            }
        }
    }
}

@Composable
fun TextBox(viewModel: SetNameViewModel, fontsize: Int ){
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val name = uiState.name?:""
    var text by remember { mutableStateOf(name) }
    Column(modifier = Modifier.padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(
            text = LocalContext.current.getString(R.string.set_name),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = fontsize.sp
        )
        OutlinedTextField(value = text, onValueChange =
        {text = it
        viewModel.update(text)},
            maxLines = 1,
            textStyle = TextStyle.Default.copy(fontSize = fontsize.sp))


        Button(onClick = {val name = uiState.name?:"".trim()
            if(name!=""){
                if(name.length>3){
                    viewModel.setName(name)
                    val intent = Intent(context, ChatList::class.java)
                    context.startActivity(intent)
                }else {
                    Toast.makeText(context, context.getText(R.string.name_short),
                        Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context, context.getText(R.string.name_blank),
                    Toast.LENGTH_SHORT).show()
            }}) {
            Text("Done", fontSize = fontsize.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
}