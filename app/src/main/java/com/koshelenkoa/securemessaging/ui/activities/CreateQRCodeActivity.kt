package com.koshelenkoa.securemessaging.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.koshelenkoa.securemessaging.compose.LoadingScreen
import com.koshelenkoa.securemessaging.compose.QRcodeContent
import com.koshelenkoa.securemessaging.ui.activities.ui.theme.MyApplicationTheme
import com.koshelenkoa.securemessaging.viewModels.QRCodeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class CreateQRCodeActivity : ComponentActivity() {

    val viewModel: QRCodeViewModel by viewModels()
    lateinit var content: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.updateStep(intent.getStringExtra("step"))
        viewModel.updateChatId(intent.getStringExtra("chatId")?: "")
        when (viewModel.step) {
            "step1" -> {
                val chatId = UUID.randomUUID().toString()
                viewModel.updateChatId(chatId)
                viewModel.createChat()
            }

            "step3" -> {
                val chatId = intent.getStringExtra("chatId")
                viewModel.updateChatId(chatId!!)
                viewModel.connectToChat()
            }
        }

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background

                ) {
                    if (viewModel.loaded) {
                        QRcodeContent(this, viewModel.step, viewModel.content, viewModel.chatId)
                    } else {
                        LoadingScreen(viewModel.errorMessage)
                    }
                }
            }
        }
    }
}
