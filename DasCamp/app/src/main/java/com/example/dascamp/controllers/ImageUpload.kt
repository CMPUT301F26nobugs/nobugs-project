package com.example.dascamp.controllers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.firestore
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Shrinks a picked photo and stores one JPEG in the Firestore `images` collection.
 *
 * Each picture is its own document, kept under the 1 MB document limit. The user
 * or campsite only stores a short marker. Replacing a photo overwrites that document.
 */
object CampImages {
    private const val MAX_EDGE = 1024
    private const val MAX_BYTES = 400 * 1024
    private val cache = java.util.concurrent.ConcurrentHashMap<String, ByteArray>()

    fun profilePath(userId: String) = "profiles_$userId"

    fun campsitePath(campsiteId: String) = "campsites_$campsiteId"

    /** @return a marker stored on the user or campsite, or null if the picture could not be saved */
    fun upload(context: Context, source: Uri, documentId: String): String? {
        val bytes = shrink(context, source) ?: return null
        return try {
            Tasks.await(
                Firebase.firestore.collection("images").document(documentId)
                    .set(mapOf("data" to Blob.fromBytes(bytes)))
            )
            val marker = "firestore:$documentId?v=${System.currentTimeMillis()}"
            cache[marker] = bytes
            marker
        } catch (_: Exception) {
            null
        }
    }

    fun delete(documentId: String) {
        cache.keys.removeAll { it.substringAfter("firestore:").substringBefore("?") == documentId }
        try {
            Tasks.await(Firebase.firestore.collection("images").document(documentId).delete())
        } catch (_: Exception) {
            // Already gone, or Firestore rules rejected the delete.
        }
    }

    /** Reads a marker written by [upload]. Returns the JPEG bytes, or null. */
    fun load(marker: String): ByteArray? {
        cache[marker]?.let { return it }
        val documentId = marker.removePrefix("firestore:").substringBefore("?")
        if (documentId.isBlank()) return null
        return try {
            val snap = Tasks.await(Firebase.firestore.collection("images").document(documentId).get())
            val bytes = snap.getBlob("data")?.toBytes() ?: return null
            cache[marker] = bytes
            bytes
        } catch (_: Exception) {
            null
        }
    }

    private fun shrink(context: Context, source: Uri): ByteArray? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / sample > MAX_EDGE || bounds.outHeight / sample > MAX_EDGE) {
            sample *= 2
        }
        val decoded = resolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val longEdge = max(decoded.width, decoded.height).coerceAtLeast(1)
        val scale = MAX_EDGE.toFloat() / longEdge
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).toInt().coerceAtLeast(1),
                (decoded.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            decoded
        }
        if (scaled != decoded) decoded.recycle()
        var quality = 70
        var bytes: ByteArray
        do {
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            bytes = out.toByteArray()
            quality -= 10
        } while (bytes.size > MAX_BYTES && quality >= 40)
        scaled.recycle()
        return if (bytes.size > MAX_BYTES) null else bytes
    }
}
