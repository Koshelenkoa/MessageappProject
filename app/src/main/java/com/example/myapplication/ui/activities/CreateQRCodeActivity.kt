package com.example.myapplication.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.activities.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModels.QRCodeViewModel
import com.example.myapplication.util.AuthManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.Executors

@AndroidEntryPoint
class CreateQRCodeActivity : ComponentActivity() {

    val viewModel: QRCodeViewModel by viewModels()
    lateinit var content: String
    val auth = AuthManager()
    val listeningExecutor = Executors.newSingleThreadExecutor()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.updateStep(intent.getStringExtra("step"))
        when(viewModel.step) {
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
                    if(viewModel.loaded) {
                        QRcodeContent(this, viewModel.step, viewModel.content, viewModel.chatId)
                    }else {
                        LoadingScreen(viewModel.errorMessage)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(errorMessage: String?) {

    if(errorMessage != null){
        Toast.makeText(LocalContext.current, errorMessage, Toast.LENGTH_SHORT)
            .show()
        LocalContext.current.startActivity(Intent(LocalContext.current ,ChatList::class.java))
    }
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(50.dp)
        )
    }
}

@Composable
fun RememberQrBitmapPainter(
    content: String,
    size: Dp = 350.dp,
    padding: Dp = 0.dp,
): BitmapPainter {

    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }
    val paddingPx = with(density) { padding.roundToPx() }


    var bitmap by remember(content) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(bitmap) {
        if (bitmap != null) return@LaunchedEffect

        launch(Dispatchers.IO) {
            val qrCodeWriter = QRCodeWriter()

            val encodeHints = mutableMapOf<EncodeHintType, Any?>()
                .apply {
                    this[EncodeHintType.MARGIN] = paddingPx
                }

            val bitmapMatrix = try {
                qrCodeWriter.encode(
                    content, BarcodeFormat.QR_CODE,
                    sizePx, sizePx, encodeHints
                )
            } catch (ex: WriterException) {
                null
            }

            val matrixWidth = bitmapMatrix?.width ?: sizePx
            val matrixHeight = bitmapMatrix?.height ?: sizePx

            val newBitmap = Bitmap.createBitmap(
                bitmapMatrix?.width ?: sizePx,
                bitmapMatrix?.height ?: sizePx,
                Bitmap.Config.ARGB_8888,
            )

            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) {
                    val shouldColorPixel = bitmapMatrix?.get(x, y) ?: false
                    val pixelColor = if (shouldColorPixel) Color.BLACK else Color.WHITE

                    newBitmap.setPixel(x, y, pixelColor)
                }
            }

            bitmap = newBitmap
        }
    }

    return remember(bitmap) {
        val currentBitmap = bitmap ?: Bitmap.createBitmap(
            sizePx, sizePx,
            Bitmap.Config.ARGB_8888,
        ).apply { eraseColor(Color.TRANSPARENT) }

        BitmapPainter(currentBitmap.asImageBitmap())
    }
}

@Composable
fun DoneButton(context: Context, step: String, chat: String?) {
    Button(onClick = {
        if (step.equals("step1")) {
            val intent = Intent(context, ScannerActivity::class.java)
            intent.putExtra("step", "step4")
            intent.putExtra("chat", chat)
            context.startActivity(intent)

        } else if (step.equals("step3")) {
            val intent = Intent(context, SetNameActivity::class.java)
            intent.putExtra("chat", chat)
            context.startActivity(intent)
        }
    })
    {
        Text("Done", fontSize = 30.sp)
    }
}


@Composable
fun QRcodeContent(context: Context, step: String, content: String, chat: String?) {
    Box(contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = RememberQrBitmapPainter(content), contentDescription = "QRCode")
            DoneButton(context = context, step = step, chat = chat)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        RememberQrBitmapPainter(content = "sbfhjeblebrgbeebfbrgbh")
    }
}

