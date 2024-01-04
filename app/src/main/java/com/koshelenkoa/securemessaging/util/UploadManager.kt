package com.koshelenkoa.securemessaging.util

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.koshelenkoa.securemessaging.MainApplication
import com.koshelenkoa.securemessaging.cryptography.MessageEncryption
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID


class UploadManager {
    fun downloadImage(url: String, encryptor: MessageEncryption): Uri? {
        var `in`: InputStream? = null
        var bytes: ByteArray? = null
        var responseCode = -1
        try {
            val url = URL(url)
            val con = url.openConnection() as HttpURLConnection
            con.doInput = true
            con.connect()
            responseCode = con.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //download
                `in` = con.inputStream
                bytes = `in`.readBytes()
                `in`.close()

            val decryptedBytes = encryptor.decryptBytes(bytes)
            val name = UUID.randomUUID().toString()+ ".png"
            val file_path = Environment.getExternalStorageDirectory().absolutePath +
                    "/secureMessaging"
            val dir = File(file_path)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, name)
            val fOut = FileOutputStream(file)

            val bitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
                val uri  = Uri.parse(file.toString())
                return uri
            }
            return null
        } catch (ex: Exception) {
            Log.e("Exception", ex.toString())
            return null
        }
    }

    suspend fun uploadImage(data: ByteArray): String? {
        val name = UUID.randomUUID().toString()
        val storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference

        val upldImagesRef = storageRef.child("images/$name.bin")

        var uploadTask = upldImagesRef.putBytes(data)

        val url =  uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            upldImagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UploadManager" , task.result.toString())
            }
        }.await()
        return url.toString()
    }

    fun downloadIfmage(url: String, encryptor: MessageEncryption): Uri? {
        var `in`: InputStream? = null
        var bytes: ByteArray? = null
        var responseCode = -1
        try {
            val url = URL(url)
            val con = url.openConnection() as HttpURLConnection
            con.doInput = true
            con.connect()
            responseCode = con.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //download
                `in` = con.inputStream
                bytes = `in`.readBytes()
                `in`.close()

                val applicationContext = MainApplication.getApplication().applicationContext
                val resolver = applicationContext.contentResolver

                val name = "IMG_${System.currentTimeMillis()}.jpg"

                val mediaCollection =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Images.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY
                        )
                    } else {
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                val imageDetails = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, name)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val uri = resolver.insert(mediaCollection, imageDetails)

                if (uri != null) {
                    resolver.openFileDescriptor(uri, "w", null).use { pfd ->
                        val fos = resolver.openOutputStream(uri)
                        fos?.write(bytes)

                    }
                    imageDetails.clear()
                    imageDetails.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(uri, imageDetails, null, null)
                }
                return uri
            }
        } catch (ex: Exception) {
            Log.e("Exception", ex.toString())
            return null
        }
        return null
    }

}
