package com.example.dascamp.controllers

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Copies a picture the user picked into this app's private files folder.
 *
 * The photo picker only lends access for a moment. Saving a copy means the
 * profile image is still there the next time the app opens. US 01.08.02
 *
 * @return the absolute path of the saved file, or null if the copy failed
 */
fun copyPickedImage(context: Context, source: Uri, fileName: String): String? {
    return try {
        val dest = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(source)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        dest.absolutePath
    } catch (_: Exception) {
        null
    }
}
