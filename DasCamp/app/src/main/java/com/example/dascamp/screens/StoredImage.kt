package com.example.dascamp.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File

/**
 * Draws a picture saved in app storage. [fallback] is shown when there is no file.
 * Used for the entrant profile image. US 01.08.02
 */
@Composable
fun StoredImage(
    path: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit
) {
    val stamp = if (path.isNullOrBlank()) 0L else File(path).lastModified()
    val bitmap = remember(path, stamp) {
        if (path.isNullOrBlank() || !File(path).exists()) null else decodeSampled(path, 512)
    }
    if (bitmap == null) {
        fallback()
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

/** Shrinks a large photo before decoding so a camera picture does not run out of memory. */
private fun decodeSampled(path: String, maxEdge: Int): android.graphics.Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (bounds.outWidth / sample > maxEdge || bounds.outHeight / sample > maxEdge) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample.coerceAtLeast(1) }
    return BitmapFactory.decodeFile(path, options)
}
