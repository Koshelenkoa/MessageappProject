package com.example.myapplication.QRcode

import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import com.google.zxing.WriterException


class QRcodeUtil {
    private val TAG = "QRCode util"

    /**
     * Generates QR bitmap from given string and dimentions
     */
    fun generateQRCode(string: String, dimen: Int): Bitmap? {
        val qrgEncoder = QRGEncoder(string, null, QRGContents.Type.TEXT, dimen)
        lateinit var qrImage: ImageView
        return try {
            qrgEncoder.bitmap
        } catch (e: WriterException) {
            Log.e(TAG, e.toString())
            null
        }
    }

}