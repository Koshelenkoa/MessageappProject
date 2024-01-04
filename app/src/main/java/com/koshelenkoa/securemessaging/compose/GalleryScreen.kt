package com.koshelenkoa.securemessaging.compose

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.Contacts.Photo
import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.snapping.SnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.koshelenkoa.securemessaging.ui.activities.ChatList

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun GalleryScreen(uris: List<Uri>, index: Int){
        val context  = LocalContext.current
        val pagerState = rememberPagerState(pageCount = { uris.size},
            initialPage = index)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { when (uris.size){
                        1 -> "Photo"
                        else -> "${pagerState.currentPage+1} of ${uris.size}"
                    } },
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.DarkGray,
                        titleContentColor = Color.White))
            },
            content = { padding ->
                    padding.toString()
                    HorizontalPager(state = pagerState, userScrollEnabled = true) { page ->
                        val bitmap =
                            MediaStore.Images.Media.getBitmap(context.contentResolver, uris[page])
                        ZoomableImage(bitmap = bitmap.asImageBitmap())
                    }

            }
        )
    }

    @Composable
    fun ZoomableImage(bitmap: ImageBitmap) {
        val scale = remember { mutableFloatStateOf(1f) }
        val rotationState = remember { mutableFloatStateOf(0f) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        scale.value *= zoom
                        rotationState.value += rotation
                    }
                }
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.Center) // keep the image centralized into the Box
                    .graphicsLayer(
                        // adding some zoom limits (min 50%, max 200%)
                        scaleX = maxOf(.7f, minOf(3f, scale.value)),
                        scaleY = maxOf(.7f, minOf(3f, scale.value)),
                        rotationZ = rotationState.value
                    ),
                contentDescription = null,
                bitmap = bitmap
            )
        }
    }
