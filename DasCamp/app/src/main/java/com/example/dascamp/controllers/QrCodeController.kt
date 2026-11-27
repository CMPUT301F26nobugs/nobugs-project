package com.example.dascamp.controllers

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.dascamp.model.Campsite
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.util.concurrent.Executors
import androidx.camera.core.Preview as CameraPreview


/**
 * QR CODE CONTROLLER
 *
 * Responsibilities:
 * - Decide what text goes inside a campsite's QR code
 * - Draw the QR code as a grid of black and white squares
 * - Read a scanned QR code's text back into a campsite id
 *
 * Collaborators: Campsite, ZXing (QRCodeWriter)
 *
 * Used by screens: OwnerQrCode it , EntrantQrCodeScreen
 */
class QrCodeController {
    private val prefix = "dascamp:campsite:"

    fun qrText(campsite: Campsite): String{
        return prefix + campsite.id
    }

    fun makeQr(text:String, size: Int): BitMatrix{
        return QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    }

    fun campsiteIdFrom(text: String): String?{
        if(!text.startsWith(prefix)){
            return null
        }

        val id = text.substring(prefix.length)
        if (id.isBlank()){
            return null
        }
        return id
    }
}

//camera for qr code scanning
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun QrCameraPreview(
    modifier: Modifier = Modifier,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentScanned by rememberUpdatedState(onQrScanned)

    val previewView = remember{ PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
        var scanned = false
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null

        providerFuture.addListener({
            cameraProvider = providerFuture.get()

            val preview = CameraPreview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(executor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val value = barcodes.firstNotNullOfOrNull { it.rawValue }
                        if (value != null && !scanned) {
                            scanned = true
                            currentScanned(value)
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            }

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Log.e("QrCameraPreview", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProvider?.unbindAll()
            scanner.close()
            executor.shutdown()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}