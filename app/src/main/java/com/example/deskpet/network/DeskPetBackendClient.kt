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
                val uri = Uri.parse(uriString)
                val boundary = "DeskPetBoundary${System.currentTimeMillis()}"
                val connection = openConnection("/api/pet/from-image").apply {
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

                val json = JSONObject(readResponse(connection))
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
