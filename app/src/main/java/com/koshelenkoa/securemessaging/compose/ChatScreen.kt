package com.koshelenkoa.securemessaging.compose

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.koshelenkoa.securemessaging.R
import com.koshelenkoa.securemessaging.data.local.Message
import com.koshelenkoa.securemessaging.data.local.room.MessageItem
import com.koshelenkoa.securemessaging.ui.activities.ChatList
import com.koshelenkoa.securemessaging.ui.activities.GalleryActivity
import com.koshelenkoa.securemessaging.util.SharedPrefsHelper
import com.koshelenkoa.securemessaging.viewModels.ChatViewModel
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    context: Context = LocalContext.current,
    viewModel: ChatViewModel,
    pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
) {
    val uiState by viewModel.uiState.collectAsState()
    val nick = uiState.username

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary
                ),
                title = { Text("$nick") },
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, ChatList::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
                                contentDescription = "Delete"
                            )
                        }
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                modifier = Modifier.shadow(
                    elevation = 10.dp,
                    spotColor = Color.DarkGray
                )
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
            ) {
                ShowMessages(
                    viewModel.messagePagingDataFlow.collectAsLazyPagingItems(),
                    { viewModel.getSelectionScreen() },
                    { viewModel.selectMessages(it) },
                    { viewModel.selectionScreenOn() },
                    { viewModel.resend(it) }
                )
            }
        },
        bottomBar = {
            TextBox(
                text = viewModel.mText,
                updateTextBox = { viewModel.updateTextBox(it) },
                encryptMessage = { viewModel.sendMessage() },
                pickMedia,
                viewModel.attachments,
                { viewModel.unattach(it) },
                context
            )
        }
    )
}


@ExperimentalComposeUiApi
@Composable
fun TextBox(
    text: String, updateTextBox: (String) -> Unit,
    encryptMessage: () -> Unit, pickMedia: ActivityResultLauncher<PickVisualMediaRequest>,
    attachments: List<Uri>, unattach: (Uri) -> Unit, context: Context
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column {
        if(attachments.isNotEmpty()) {
            ImageTiles(attachments, unattach, context)
        }
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }),
                leadingIcon = {
                    Button(
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = stringResource(R.string.button_attach),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingIcon = {
                    Button(
                        onClick = {
                            encryptMessage.invoke()
                        },

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        )
                    )
                    {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = stringResource(R.string.cd_button_send),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ImageTiles(attachmentList: List<Uri>, unattach: (Uri) -> Unit, context: Context ){
    LazyRow(
        userScrollEnabled = true
    ){
        items(attachmentList.size) {index ->
            ImageTile(attachmentList[index], unattach, context)
        }
    }
}

@Composable
fun ImageTile(uri: Uri,
              unattach: (Uri) -> Unit, context: Context){
    val bitmap = context.contentResolver.loadThumbnail(uri, Size(160,160), null)
    Log.d("Chat", uri.path.toString())
    Box(contentAlignment = Alignment.Center) {
        Image(
            bitmap = bitmap.asImageBitmap(), "ImageTile",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .padding(3.dp)

        )
        Button(onClick = { unattach(uri) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Delete Image",
                modifier = Modifier.shadow(5.dp),
                tint = Color.White
            )

        }
    }
}

@Composable
fun ShowMessage(
    messageItem: MessageItem, selectionScreen: () -> Boolean,
    selectMessages: (String) -> Unit, selectionScreenOn: () -> Unit,
    resend: (MessageItem) -> Unit,
    context: Context
) {
    val picHeight = 300
    val picWidth = 300
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        DialogOnResend(
            onDismissRequest = { showDialog = false },
            onConfirmation = {
                    resend(messageItem)
                    showDialog = false
                } )
    }
    val paddingValues = PaddingValues(end = 6.dp, bottom = 6.dp, top = 2.dp, start = 6.dp)
    val iconSize = 15.dp
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
                        onClick = {
                            if (selectionScreen()) {
                                selectMessages(messageItem.messageId)
                            }
                            if (messageItem.sent == Message.FAILED) {
                                showDialog = true
                            }
                        }
                    ),
                shape = cardShapeFor(messageItem),
                backgroundColor = when (messageItem.isMine) {
                    true -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                },
                content = {
                    val content = messageItem.messageData
                    val uris = content.attachments?.map{string -> Uri.parse(string)}
                    Column {
                        if(content.text != null) {
                            if (content.text.isNotBlank()) {
                                Text(
                                    text = content.text,
                                    modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 0.dp),
                                    fontSize = SharedPrefsHelper.getFontSize(LocalContext.current).sp,
                                    color = when (messageItem.isMine) {
                                        true -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSecondary
                                    }
                                )
                            }
                        }
                        if(uris != null){
                            if(uris.isNotEmpty()) {
                                val size = content.attachments.size
                                if (size == 1) {
                                    val bitmap = context.contentResolver.loadThumbnail(
                                        uris[0],
                                        Size(picWidth*3, picHeight*3),
                                        null)
                                    Image(
                                        bitmap = bitmap.asImageBitmap(), "ImageTile",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .height(picHeight.dp)
                                            .width((picWidth*0.9).dp)
                                            .padding(3.dp)

                                    )
                                }else{
                                    val second = size/2
                                    val first = second + size%2

                                    Row {
                                        Column {
                                            for (i in 0..<size step 2) {
                                                ImageView(
                                                    index = i, height = picHeight / first,
                                                    width = picWidth / 2, context = context,
                                                    uris = uris.toTypedArray()
                                                )
                                            }
                                        }
                                        Column {
                                            for (i in 1..<size step 2) {
                                                ImageView(
                                                    index = i, height = picHeight / second,
                                                    width = picWidth / 2, context = context,
                                                    uris = uris.toTypedArray()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // status icon
                        when (messageItem.sent) {
                            Message.SENT -> {
                                if (messageItem.time != null) {
                                    Text(
                                        text = messageItem.time.toString(),
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .padding(paddingValues),
                                        color = when {
                                            messageItem.isMine -> MaterialTheme.colorScheme.onPrimary.copy(
                                                alpha = 0.5f
                                            )

                                            else -> MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
                                        }
                                    )
                                }
                            }

                            Message.PENDING -> {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "Waiting for message to be sent",
                                    modifier = Modifier
                                        .align(alignment = Alignment.End)
                                        .padding(paddingValues)
                                        .size(iconSize),
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                                )
                            }

                            Message.FAILED -> {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = "Failed to send message",
                                    modifier = Modifier
                                        .align(alignment = Alignment.End)
                                        .padding(paddingValues)
                                        .size(iconSize),
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogOnResend(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit) {
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.dialog_resend_title))
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text(stringResource(R.string.dialog_resend_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
}

@Composable
fun ShowMessages(
    messages: LazyPagingItems<MessageItem>, selectionScreen: () -> Boolean,
    selectMessages: (String) -> Unit, selectionScreenOn: () -> Unit,
    resend: (MessageItem) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            userScrollEnabled = true
        ) {
            items(
                count = messages.itemCount,
                key = messages.itemKey { it.messageId }
            ) { index ->
                val message = messages[index]
                if (message != null) {
                    ShowMessage(
                        message,
                        selectionScreen,
                        selectMessages,
                        selectionScreenOn,
                        resend,
                        LocalContext.current
                    )
                }
            }
        }
    }
}

@Composable
fun ImageView(uris:Array<Uri>, index:Int, height: Int, width: Int, context: Context){
    val urisAsStrings = uris.map{ uri -> uri.toString()}.toTypedArray()
    val uri  = uris[index]
    Log.d("Chat Screen", uri.path.toString())
    var bitmap: Bitmap? = null
    try {
       bitmap = context.contentResolver.loadThumbnail(
            uri.normalizeScheme(),
            Size(width * 3, height * 3),
            null
        )
    }catch (e: Exception){
        Log.d("ChatScreen", e.message.toString())
    }
    if (bitmap != null) {
        Image(
            modifier = Modifier
                .clickable {
                    val intent = Intent(context, GalleryActivity::class.java)
                    intent.putExtra("index", index)
                    intent.putExtra("uris", urisAsStrings)
                    context.startActivity(intent)
                }
                .height(height.dp)
                .width(width.dp)
                .padding(3.dp),
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "ImageTile",
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun cardShapeFor(message: MessageItem): Shape {
    val roundedCorners = RoundedCornerShape(16.dp)
    return when (message.isMine) {
        true -> roundedCorners.copy(bottomEnd = CornerSize(0))
        else -> roundedCorners.copy(bottomStart = CornerSize(0))
    }

}