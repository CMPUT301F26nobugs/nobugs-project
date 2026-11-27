package com.example.dascamp.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.example.dascamp.controllers.CampImages
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Draws a picture saved in app storage. [fallback] is shown when there is no file.
 * Used for the entrant profile image. US 01.08.02
 * * @param contentScale how the picture fits the box: Crop fills it (cuts the edges),
 *  * Fit shows the whole picture
 */
@Composable
fun StoredImage(
    path: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable () -> Unit
) {
    if (!path.isNullOrBlank() && path.startsWith("firestore:")) {
        var bitmap by remember(path) { mutableStateOf<android.graphics.Bitmap?>(null) }
        LaunchedEffect(path) {
            val bytes = withContext(Dispatchers.IO) { CampImages.load(path) }
            bitmap = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        }
        val loaded = bitmap
        if (loaded == null) {
            fallback()
        } else {
            Image(
                bitmap = loaded.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        return
    }
    if (!path.isNullOrBlank() && (path.startsWith("http") || path.startsWith("content:"))) {
        SubcomposeAsyncImage(
            model = path,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            loading = { fallback() },
            error = { fallback() }
        )
        return
    }
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
            contentScale = contentScale
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
