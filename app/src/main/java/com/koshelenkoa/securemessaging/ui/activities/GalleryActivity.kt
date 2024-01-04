package com.koshelenkoa.securemessaging.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.koshelenkoa.securemessaging.compose.GalleryScreen
import com.koshelenkoa.securemessaging.ui.activities.ui.theme.MyApplicationTheme
import com.koshelenkoa.securemessaging.viewModels.GalleryViewModel

class GalleryActivity : ComponentActivity() {
    val viewModel: GalleryViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.updateAttachments(intent.getStringArrayExtra("uris"))
        viewModel.updateIndex(intent.getIntExtra("index", 0 ))
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black

                ) {
                    GalleryScreen(viewModel.attachments, viewModel.index)
                }
            }
        }
    }
}
