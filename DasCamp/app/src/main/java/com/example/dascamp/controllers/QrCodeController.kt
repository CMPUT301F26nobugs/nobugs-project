package com.example.dascamp.controllers

import com.example.dascamp.model.Campsite
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter


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

    /**
    * The text stored inside a campsite's QR code.
    *
    * @param campsite the campsite the code is for
    * @return text like "dascamp:campsite:abc123"
    */
    fun qrText(campsite: Campsite): String{
        return prefix + campsite.id
    }

    /**
     * Draws a QR code for some text.
     *
     * @param text what to store in the code
     * @param size width and height in pixels
     * @return a size x size grid where true = black square, false = white square
     */
    fun makeQr(text:String, size: Int): BitMatrix{
        return QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    }

    /**
     * Reads a scanned QR code's text and finds the campsite id in it.
     *
     * @param text the text the camera read
     * @return the campsite id, or null if it is not a DasCamp QR code
     */
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

