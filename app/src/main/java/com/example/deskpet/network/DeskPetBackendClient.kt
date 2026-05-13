package com.example.deskpet.network

import android.content.Context
import android.net.Uri
import com.example.deskpet.model.PetProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class BackendChatResponse(
    val reply: String,
    val moodTag: String,
    val diarySummary: String
)

data class BackendImageResponse(
    val petName: String,
    val personality: String,
    val expression: String,
    val decoration: String,
    val favoriteFood: String,
    val companionStyle: String,
    val stageTheme: String,
    val accentEmoji: String,
    val actionHint: String,
    val description: String
)

data class BackendCutoutResponse(
    val success: Boolean,
    val mode: String,
    val message: String,
    val imageUrl: String?,
    val processedImagePath: String?,
    val suggestedCrop: String,
    val confidence: Float
)

class DeskPetBackendClient(
    private val baseUrl: String = "http://10.0.2.2:8000"
) {
    fun backendUrl(): String = baseUrl

    suspend fun checkHealth(): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                val response = openConnection("/health").apply {
                    requestMethod = "GET"
                    doOutput = false
                }
                response.responseCode in 200..299
            }.getOrDefault(false)
        }
    }

    suspend fun sendChat(message: String, petProfile: PetProfile): BackendChatResponse? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val payload = JSONObject()
                    .put("message", message)
                    .put(
                        "pet",
                        JSONObject()
                            .put("pet_id", petProfile.id)
                            .put("pet_name", petProfile.name)
                            .put("personality", petProfile.personality.name)
                    )

                val json = JSONObject(postJson("/api/chat", payload.toString()))
                BackendChatResponse(
                    reply = json.getString("reply"),
                    moodTag = json.optString("mood_tag", "普通"),
                    diarySummary = json.optString("diary_summary", message)
                )
            }.getOrNull()
        }
    }

    suspend fun uploadPetImage(context: Context, uriString: String): BackendImageResponse? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val json = JSONObject(postMultipartImage(context, uriString, "/api/pet/from-image"))
                BackendImageResponse(
                    petName = json.optString("pet_name", ""),
                    personality = json.optString("personality", ""),
                    expression = json.optString("expression", ""),
                    decoration = json.optString("decoration", ""),
                    favoriteFood = json.optString("favorite_food", ""),
                    companionStyle = json.optString("companion_style", ""),
                    stageTheme = json.optString("stage_theme", ""),
                    accentEmoji = json.optString("accent_emoji", ""),
                    actionHint = json.optString("action_hint", ""),
                    description = json.optString("description", "")
                )
            }.getOrNull()
        }
    }

    suspend fun requestPetCutout(context: Context, uriString: String): BackendCutoutResponse? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val json = JSONObject(postMultipartImage(context, uriString, "/api/pet/cutout"))
                BackendCutoutResponse(
                    success = json.optBoolean("success", false),
                    mode = json.optString("mode", "soft_cutout"),
                    message = json.optString("message", ""),
                    imageUrl = json.optString("image_url", "").takeIf { it.isNotBlank() },
                    processedImagePath = json.optString("processed_image_path", "").takeIf { it.isNotBlank() },
                    suggestedCrop = json.optString("suggested_crop", "center"),
                    confidence = json.optDouble("confidence", 0.5).toFloat()
                )
            }.getOrNull()
        }
    }

    private fun postJson(path: String, body: String): String {
        val connection = openConnection(path).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        connection.outputStream.use { outputStream ->
            outputStream.write(body.toByteArray(Charsets.UTF_8))
        }
        return readResponse(connection)
    }

    private fun postMultipartImage(context: Context, uriString: String, path: String): String {
        val uri = Uri.parse(uriString)
        val boundary = "DeskPetBoundary${System.currentTimeMillis()}"
        val connection = openConnection(path).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        connection.outputStream.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8)).use { writer ->
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"pet-image.jpg\"\r\n")
                writer.append("Content-Type: image/jpeg\r\n\r\n")
                writer.flush()

                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(outputStream)
                }
                outputStream.flush()

                writer.append("\r\n--$boundary--\r\n")
                writer.flush()
            }
        }

        return readResponse(connection)
    }

    private fun openConnection(path: String): HttpURLConnection {
        return (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            connectTimeout = 2500
            readTimeout = 4500
            doInput = true
            doOutput = true
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val statusCode = connection.responseCode
        val stream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        val response = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        if (statusCode !in 200..299) {
            error("Backend returned $statusCode: $response")
        }
        return response
    }
}
