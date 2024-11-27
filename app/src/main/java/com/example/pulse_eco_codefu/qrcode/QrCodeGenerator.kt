package com.example.pulse_eco_codefu.qrcode

import android.app.Activity
import android.graphics.Bitmap
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder


class QrCodeGenerator {

        public fun generateQRCode(text: String,qrCodeIV:ImageView) : Bitmap ?{
        val barcodeEncoder = BarcodeEncoder()
        try {
            // This method returns a Bitmap image of the
            // encoded text with a height and width of 400
            // pixels.
            val bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400)
            qrCodeIV.setImageBitmap(bitmap) // Sets the Bitmap to ImageView
            return bitmap;

        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }

    }

    public fun readQRCode(activity:Activity)
    {
        val integrator=IntentIntegrator(activity)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setBeepEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()

    }
}
