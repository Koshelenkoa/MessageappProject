package com.example.myapplication.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.MainApplication
import com.example.myapplication.R
import com.example.myapplication.data.local.room.MessageItem
import com.example.myapplication.ui.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.ChatViewModel
import com.example.myapplication.workers.SendMessageWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
@AndroidEntryPoint
class ChatActivity : ComponentActivity() {

    val chatId = intent.extras?.getString("chatId")
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var login: String
    private val serviceUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "UPDATE_CHAT") {
                viewModel.loadMessages()
            }
        }
    }

    fun sendMessage(){
        val sendMessageWorker = OneTimeWorkRequestBuilder<SendMessageWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .putAll(viewModel.messageToBeSent.toMap())
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueue(sendMessageWorker).result
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentFilter = IntentFilter("UPDATE_CHAT")
        registerReceiver(serviceUpdateReceiver, intentFilter)
        viewModel.setChat(chatId)
        val sharedPrefs = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        login = sharedPrefs.getString("login", "")?: ""
        viewModel.setLogin(login)
        viewModel.loadMessages()
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background

                ) {
                    MainContent(viewModel = viewModel, sendMessage = {sendMessage()})
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MainApplication.activityResumed()
    }

    override fun onPause() {
        super.onPause()
        MainApplication.activityPaused()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainContent(context: Context = LocalContext.current, viewModel: ChatViewModel, sendMessage: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val nick = uiState.username
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(),
                title = { Text("$nick")},
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, ChatList::class.java)
                        context.startActivity(intent) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go back")
                    }
                },
                actions = {
                    if (viewModel.getSelectionScreen()) {
                        IconButton(onClick = {
                            viewModel.deleteMessages()
                            viewModel.selectionScreenOff()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete")
                        }
                    }
                }
            )
        },
    ) { padding ->
        Column(modifier = Modifier
                .padding(padding)
        ) {
            ShowMessages(uiState.messages, {viewModel.getSelectionScreen()},
                {viewModel.selectMessages(it)}, {viewModel.selectionScreenOn()})
        }
    }

    TextBox(viewModel.mText, {viewModel.updateTextBox(it)}, {viewModel.encryptMessage()}, sendMessage)
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
fun TextBox(text: String, updateTextBox:(String) -> Unit, encryptMessage: () -> Unit ,
            sendMessage: () -> Unit){
    val keyboardController = LocalSoftwareKeyboardController.current

    Row {

        TextField(
            value = text,
            onValueChange = {
                updateTextBox(it)
            },
            modifier = Modifier
                .weight(1f),
            placeholder = {
                Text(
                    text = stringResource(R.string.textBox_placeholder)
                )
            },
            maxLines = 6,
            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),
        )
        Button(
            modifier = Modifier.height(56.dp),
            onClick = { encryptMessage()
            sendMessage()}
        ) {

            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = stringResource(R.string.cd_button_send)
            )
        }
    }


}

@Composable
fun ShowMessage(messageItem: MessageItem, selectionScreen: () -> Boolean,
          selectMessages: (String) -> Unit, selectionScreenOn: () -> Unit) {

    Row {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = when {
                messageItem.isMine -> Alignment.End
                else -> Alignment.Start
            },
        )
        {
            Card(
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(),
                    ) {
                        if (selectionScreen()) {
                            selectMessages(messageItem.messageId)

                        }
                    }
                    .timedClick(1000) { passed: Boolean ->
                        if (selectionScreen()&& passed) {
                            selectionScreenOn()
                            selectMessages(messageItem.messageId)

                        }
                    },
                shape = cardShapeFor(messageItem),
                backgroundColor = when (messageItem.isMine) {
                    true -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                },
                content = {
                    Text(
                        text = messageItem.content,
                        modifier = Modifier.padding(8.dp),
                        color = when (messageItem.isMine) {
                            true -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSecondary
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun ShowMessages(messages: List<MessageItem>,  selectionScreen: () -> Boolean,
                 selectMessages: (String) -> Unit, selectionScreenOn: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true
        ) {

            items(messages.size) { messageNum ->
                ShowMessage(messages[messageNum], selectionScreen, selectMessages,
                    selectionScreenOn)
            }

        }

    }
}

@Composable
fun Modifier.timedClick(
    timeInMillis: Long,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: (Boolean) -> Unit,
) = composed {

    var timeOfTouch = -1L
    LaunchedEffect(key1 = timeInMillis, key2 = interactionSource) {
        interactionSource.interactions
            .onEach { interaction: Interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        timeOfTouch = System.currentTimeMillis()
                    }

                    is PressInteraction.Release -> {
                        val currentTime = System.currentTimeMillis()
                        onClick(currentTime - timeOfTouch > timeInMillis)
                    }

                    is PressInteraction.Cancel -> {
                        onClick(false)
                    }
                }

            }
            .launchIn(this)
    }

    Modifier.clickable(
        interactionSource = interactionSource,
        indication = rememberRipple(),
        onClick = {}
    )
}



@Composable
fun cardShapeFor(message: MessageItem): Shape {
    val roundedCorners = RoundedCornerShape(16.dp)
    return when (message.isMine) {
        true -> roundedCorners.copy(bottomEnd = CornerSize(0))
        else -> roundedCorners.copy(bottomStart = CornerSize(0))
    }

}

