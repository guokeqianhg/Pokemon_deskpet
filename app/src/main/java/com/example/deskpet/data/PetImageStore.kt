package com.example.deskpet.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class PetImageStore(private val context: Context) {
    fun cacheImage(sourceUri: String): String? {
        return runCatching {
            val source = Uri.parse(sourceUri)
            val directory = File(context.filesDir, IMAGE_DIRECTORY).apply { mkdirs() }
            val target = File(directory, IMAGE_FILE_NAME)

            if (!writeScaledImage(source, target)) {
                context.contentResolver.openInputStream(source)?.use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return null
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                target
            ).toString()
        }.getOrNull()
    }

    fun canRead(uriString: String): Boolean {
        return runCatching {
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use { true } == true
        }.getOrDefault(false)
    }

    fun clearCache() {
        runCatching {
            File(context.filesDir, IMAGE_DIRECTORY).deleteRecursively()
        }
    }

    private fun writeScaledImage(source: Uri, target: File): Boolean {
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(source)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
            }
            val bitmap = context.contentResolver.openInputStream(source)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            } ?: return false

            target.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
            }
            bitmap.recycle()
            true
        }.getOrDefault(false)
    }

    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        var scaledWidth = width
        var scaledHeight = height
        while (scaledWidth > MAX_IMAGE_SIDE || scaledHeight > MAX_IMAGE_SIDE) {
            sample *= 2
            scaledWidth /= 2
            scaledHeight /= 2
        }
        return sample.coerceAtLeast(1)
    }

    private companion object {
        const val IMAGE_DIRECTORY = "pet_images"
        const val IMAGE_FILE_NAME = "current_pet_image.jpg"
        const val MAX_IMAGE_SIDE = 1280
    }
}
