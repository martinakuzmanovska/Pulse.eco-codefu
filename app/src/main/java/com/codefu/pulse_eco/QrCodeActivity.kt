package com.codefu.pulse_eco

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
<<<<<<< HEAD:app/src/main/java/com/example/pulse_eco_codefu/QrCodeActivity.kt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codefu.pulse_eco.R
import com.example.pulse_eco_codefu.qrcode.QrCodeGenerator
=======
import com.codefu.pulse_eco.qrcode.QrCodeGenerator
>>>>>>> 357ce3371e8c1ef4373536a685f8609219c311b5:app/src/main/java/com/codefu/pulse_eco/QrCodeActivity.kt
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

//TODO IF USER ADD SCAN  AND CREATE ELSE ONLY SCAN


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
        val qrCode: QrCodeGenerator = QrCodeGenerator()
        qrCodeIV = findViewById(R.id.idIVQrcode);
        dataEdt = findViewById(R.id.idEdt);
        generateQrBtn = findViewById(R.id.idBtnGenerateQR);
        readQrBtn=findViewById(R.id.idBtnScanQR)

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
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val scannedData = result.contents
            Toast.makeText(this, "Scanned data: $scannedData", Toast.LENGTH_SHORT).show()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }
}