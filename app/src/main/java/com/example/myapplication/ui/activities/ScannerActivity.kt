package com.example.myapplication.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.QRcode.StringForQRGenerator
import com.example.myapplication.cryptography.KeyGeneratorForKeyStore
import com.example.myapplication.ui.viewModels.QRCodeViewModel
import com.example.myapplication.ui.viewModels.ScannerViewModel
import com.google.zxing.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.security.KeyStore


@AndroidEntryPoint
class ScannerActivity : ComponentActivity(), ZXingScannerView.ResultHandler {
    val viewModel: ScannerViewModel by viewModels()
    var step : String? = null
    private val keystore = KeyStore.getInstance("AndroidKeyStore")
    private val keyGeneratorForKeyStore = KeyGeneratorForKeyStore(keystore)
    private val qrStringGenerator = StringForQRGenerator(keyGeneratorForKeyStore)
    private var mScannerView: ZXingScannerView? = null
    val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + job)


    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        Log.d("Scanner Activity", "Activity started")
        checkCameraPermissions(this)
        step = intent.getStringExtra("step")
        viewModel.updateChatId(intent.getStringExtra("chatId"))
        // Programmatically initialize the scanner view
        mScannerView = ZXingScannerView(this)
        // Set the scanner view as the content view
        setContentView(mScannerView)
    }

    override fun onResume() {
        super.onResume()
        mScannerView?.setResultHandler(this)
        mScannerView?.startCamera()
    }

    override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        Log.d("Scanner Activity", "Result called")
        val resString = rawResult.barcodeFormat.toString()
        val context = this
        when (step) {
            "step2" -> {
                val data = qrStringGenerator.step2(resString)
                val intent = Intent(context, CreateQRCodeActivity::class.java)
                intent.putExtra("chatId", data)
                intent.putExtra("step", "step3")
                startActivity(intent)
                    }


            "step4" -> {
                qrStringGenerator.step4(resString)
                val intent  = Intent(this, SetNameActivity::class.java)
                intent.putExtra("chat", viewModel.chatId)
                startActivity(intent)
            }
        }
        finish()
    }
}

fun checkCameraPermissions(context: Context) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED
    ) {
        // Permission is not granted
        Log.d("checkCameraPermissions", "No Camera Permissions")
        ActivityCompat.requestPermissions(
            (context as Activity?)!!, arrayOf<String>(Manifest.permission.CAMERA),
            100
        )
    }
}