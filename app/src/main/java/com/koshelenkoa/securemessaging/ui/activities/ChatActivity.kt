package com.koshelenkoa.securemessaging.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.koshelenkoa.securemessaging.compose.ChatScreen
import com.koshelenkoa.securemessaging.ui.activities.ui.theme.MyApplicationTheme
import com.koshelenkoa.securemessaging.viewModels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            for(uri in uris) {
                this.contentResolver.takePersistableUriPermission(uri, flag)
            }
            viewModel.attach(uris)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatId = intent.extras?.getString("chatId")
        viewModel.setChat(chatId)
        viewModel.setNickName()
        viewModel.loadMessages()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(viewModel = viewModel, pickMedia = pickMedia)
                }
            }
        }
    }
}
