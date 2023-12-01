package com.koshelenkoa.securemessaging.ui.activities

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
import com.koshelenkoa.securemessaging.util.StringForQRGenerator
import com.koshelenkoa.securemessaging.viewModels.ScannerViewModel
import com.google.zxing.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.security.KeyStore


@AndroidEntryPoint
class ScannerActivity : ComponentActivity(), ZXingScannerView.ResultHandler {
    val viewModel: ScannerViewModel by viewModels()
    private val keystore = KeyStore.getInstance("AndroidKeyStore")
    private val keyGeneratorForKeyStore =
        com.koshelenkoa.securemessaging.cryptography.KeyGeneratorForKeyStore(keystore)
    private val qrStringGenerator = StringForQRGenerator(keyGeneratorForKeyStore)
    private var mScannerView: ZXingScannerView? = null
    val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + job)


    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        Log.d("Scanner Activity", "Activity started")
        checkCameraPermissions(this)
        intent.getStringExtra("step")?.let { viewModel.updateStep(it) }
        intent.getStringExtra("chatId")?.let { viewModel.updateChatId(it) }
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
        val resString = rawResult.text
        val context = this
        when (viewModel.step) {
            "step2" -> {
                Log.d("ScannerActivity", "step2")
                val data = qrStringGenerator.step2(resString)
                val intent = Intent(context, CreateQRCodeActivity::class.java)
                intent.putExtra("chatId", data)
                intent.putExtra("step", "step3")
                startActivity(intent)
            }

            "step4" -> {
                Log.d("ScannerActivity", "step4")
                qrStringGenerator.step4(resString)
                val intent = Intent(this, SetNameActivity::class.java)
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