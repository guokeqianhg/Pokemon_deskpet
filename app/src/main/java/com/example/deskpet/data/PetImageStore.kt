package com.example.deskpet.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class PetImageStore(private val context: Context) {
    fun cacheImage(sourceUri: String): String? {
        return runCatching {
            val source = Uri.parse(sourceUri)
            val directory = File(context.filesDir, IMAGE_DIRECTORY).apply { mkdirs() }
            val target = File(directory, IMAGE_FILE_NAME)

            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

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

    private companion object {
        const val IMAGE_DIRECTORY = "pet_images"
        const val IMAGE_FILE_NAME = "current_pet_image.jpg"
    }
}
