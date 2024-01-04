package com.koshelenkoa.securemessaging.viewModels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GalleryViewModel: ViewModel(){
    var attachments: List<Uri> by mutableStateOf(emptyList())
        private set
    var index by mutableIntStateOf(0)
    fun updateAttachments(urisAsStrings: Array<String>?){
        if(urisAsStrings != null){
            for( uriString in urisAsStrings){
                val uri = Uri.parse(uriString)
                attachments = attachments.plus(uri)
            }
        }
    }

    fun updateIndex(intExtra: Int) {
        index = intExtra
    }
}