package com.koshelenkoa.securemessaging.compose

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.koshelenkoa.securemessaging.R
import com.koshelenkoa.securemessaging.data.local.ChatForUI
import com.koshelenkoa.securemessaging.ui.activities.ChatActivity
import com.koshelenkoa.securemessaging.ui.activities.CreateQRCodeActivity
import com.koshelenkoa.securemessaging.ui.activities.ScannerActivity
import com.koshelenkoa.securemessaging.util.SharedPrefsHelper


@Composable
fun ChannelListItem(chat: ChatForUI) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable(enabled = true,
                onClick = {
                    Log.d("Click in list", "Clicked")
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("chatId", chat.chatId)
                    context.startActivity(intent)
                })
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.padding(start = 8.dp)) { // 3
            Text(
                text = chat.nick ?: "",
                style = TextStyle(fontWeight = FontWeight.Bold),
                fontSize = SharedPrefsHelper.getFontSize(LocalContext.current).sp,
            )
            val lastMessageText = chat.lastText ?: ". . . "

            Text(
                text = lastMessageText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = SharedPrefsHelper.getFontSize(LocalContext.current).sp
            )
        }
    }
}


@Composable
fun ChatListScreen(chatList: List<ChatForUI>) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (chatList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) {
                items(chatList.size) { channelNum ->
                    ChannelListItem(chatList[channelNum])
                    Divider()
                }
            }
        } else {
            Text(
                context.getString(R.string.no_chats),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

}

@Composable
fun MultiFab(
    multiFloatingState: MultiFloatingState,
    onMultiFloatingStateChange: (MultiFloatingState) -> Unit,
    context: Context,
    items: List<MinFabItem>,
) {
    Column(horizontalAlignment = Alignment.End) {
        if (multiFloatingState == MultiFloatingState.Expanded) {
            items.forEach { item ->
                MinFab(
                    item = item,
                    onMinFabButtonClick = { minFabItem ->
                        when (minFabItem.identifier) {
                            Identifier.Scan.name -> {
                                val intent = Intent(context, ScannerActivity::class.java)
                                intent.putExtra("step", "step2")
                                context.startActivity(intent)
                            }

                            Identifier.Create.name -> {
                                val intent = Intent(context, CreateQRCodeActivity::class.java)
                                intent.putExtra("step", "step1")
                                context.startActivity(intent)
                            }
                        }
                    },
                    fabScale = if (multiFloatingState == MultiFloatingState.Expanded) 70f else 0f,
                    alpha = if (multiFloatingState == MultiFloatingState.Expanded) 1f else 0f
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
        }

        FloatingActionButton(
            onClick = {
                onMultiFloatingStateChange(
                    if (multiFloatingState == MultiFloatingState.Expanded) {
                        MultiFloatingState.Collapsed
                    } else {
                        MultiFloatingState.Expanded
                    }
                )
            },
            modifier = Modifier.rotate(if (multiFloatingState == MultiFloatingState.Expanded) 315f else 0f),
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.Add,
                null,
                Modifier.rotate(if (multiFloatingState == MultiFloatingState.Expanded) 315f else 0f)
            )
        }
    }
}


@Composable
fun MinFab(
    item: MinFabItem,
    onMinFabButtonClick: (MinFabItem) -> Unit,
    fabScale: Float,
    alpha: Float,
) {
    val color = MaterialTheme.colorScheme.primary
    Log.d("ChatList", "${item.identifier} called")
    Text(
        text = item.label,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .alpha(
                animateFloatAsState(
                    targetValue = alpha,
                    animationSpec = tween(50), label = ""
                ).value
            )
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 6.dp, end = 6.dp, top = 4.dp)
    )
    Spacer(modifier = Modifier.size(16.dp))
    Canvas(
        modifier = Modifier
            .size(32.dp)
            .clickable(
                interactionSource = MutableInteractionSource(),
                onClick = {
                    onMinFabButtonClick.invoke(item)
                },
                indication = rememberRipple(
                    bounded = false,
                    radius = 20.dp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
    ) {
        drawCircle(
            color = color,
            radius = fabScale
        )
        drawImage(
            image = item.icon,
            topLeft = Offset(
                center.x - (item.icon.width / 2),
                center.y - (item.icon.width / 2)
            ),
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
}

enum class Identifier {
    Create,
    Scan
}

enum class MultiFloatingState {
    Expanded,
    Collapsed
}

class MinFabItem(
    val icon: ImageBitmap,
    val label: String,
    val identifier: String,
)