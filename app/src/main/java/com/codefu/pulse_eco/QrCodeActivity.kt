package com.codefu.pulse_eco

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.codefu.pulse_eco.domain.repositories.UserActivityLogRepository
import com.codefu.pulse_eco.domain.repositories.impl.UserActivityLogRepositoryImpl

import com.codefu.pulse_eco.qrcode.QrCodeGenerator
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//TODO IF USER ADD SCAN AND CREATE ELSE ONLY SCAN


class QrCodeActivity : AppCompatActivity() {
    private lateinit   var qrCodeIV: ImageView
    private lateinit var dataEdt:EditText
    private lateinit var generateQrBtn:Button
    private lateinit var readQrBtn:Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.qr_code_layout)
        val qrCode = QrCodeGenerator()
        qrCodeIV = findViewById(R.id.idIVQrcode)
        dataEdt = findViewById(R.id.idEdt)
        generateQrBtn = findViewById(R.id.idBtnGenerateQR)
        readQrBtn = findViewById(R.id.idBtnScanQR)

        generateQrBtn.setOnClickListener {
            val inputText = dataEdt.text.toString()
            if (inputText.isEmpty()) {
                // Show a toast if the input is empty
                Toast.makeText(this, "Enter some text to generate QR Code", Toast.LENGTH_SHORT).show()
            } else {
                // Generate the QR code
                qrCode.generateQRCode(inputText, qrCodeIV)
            }
        }
        readQrBtn.setOnClickListener{
            qrCode.readQRCode(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val activityLogsRepository:UserActivityLogRepository=UserActivityLogRepositoryImpl(this)
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val scannedData = result.contents

            if (scannedData.isNotEmpty()) {
                Toast.makeText(this, "Scanned data: $scannedData", Toast.LENGTH_SHORT).show()

                CoroutineScope(Dispatchers.IO).launch {
                    activityLogsRepository.createLog(scannedData) { success ->
                        if (success) {
                            Log.i("QRLOG","Activity Log  created successfully!")

                        } else {
                            Log.i("QRLOG","Failed to create activity log.")
                        }
                    }
                }
            }
            else{
                Log.i("QRLOG","Invalid QR Code")
            }
            finish()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}