package com.example.deskpet.data

import android.content.Context
import com.example.deskpet.model.ChatMessage
import com.example.deskpet.model.DiaryEntry
import com.example.deskpet.model.MessageRole
import com.example.deskpet.model.Personality
import com.example.deskpet.model.PetAction
import com.example.deskpet.model.PetProfile
import com.example.deskpet.model.PetStatus
import com.example.deskpet.util.defaultStatus
import com.example.deskpet.util.generateRandomPet
import org.json.JSONArray
import org.json.JSONObject

data class DeskPetSnapshot(
    val petProfile: PetProfile,
    val petStatus: PetStatus,
    val diaryEntries: List<DiaryEntry>,
    val chatMessages: List<ChatMessage>
)

class DeskPetRepository(context: Context) {
    private val preferences = context.getSharedPreferences("deskpet_store", Context.MODE_PRIVATE)

    fun loadSnapshot(): DeskPetSnapshot {
        val profile = preferences.getString(KEY_PROFILE, null)
            ?.let { runCatching { parsePetProfile(JSONObject(it)) }.getOrNull() }
            ?: generateRandomPet(currentImageUri = null)

        val status = preferences.getString(KEY_STATUS, null)
            ?.let { runCatching { parsePetStatus(JSONObject(it)) }.getOrNull() }
            ?: defaultStatus()

        val diaries = preferences.getString(KEY_DIARIES, null)
            ?.let { runCatching { parseDiaryEntries(JSONArray(it)) }.getOrNull() }
            ?: emptyList()

        val messages = preferences.getString(KEY_MESSAGES, null)
            ?.let { runCatching { parseChatMessages(JSONArray(it)) }.getOrNull() }
            ?: emptyList()

        return DeskPetSnapshot(profile, status.clamped(), diaries, messages)
    }

    fun savePet(profile: PetProfile, status: PetStatus) {
        runCatching {
            preferences.edit()
                .putString(KEY_PROFILE, profile.toJson().toString())
                .putString(KEY_STATUS, status.clamped().toJson().toString())
                .apply()
        }
    }

    fun saveDiaryEntries(entries: List<DiaryEntry>) {
        runCatching {
            preferences.edit()
                .putString(KEY_DIARIES, entries.toDiaryJson().toString())
                .apply()
        }
    }

    fun saveChatMessages(messages: List<ChatMessage>) {
        runCatching {
            preferences.edit()
                .putString(KEY_MESSAGES, messages.takeLast(MAX_MESSAGES).toChatJson().toString())
                .apply()
        }
    }

    fun saveAll(snapshot: DeskPetSnapshot) {
        runCatching {
            preferences.edit()
                .putString(KEY_PROFILE, snapshot.petProfile.toJson().toString())
                .putString(KEY_STATUS, snapshot.petStatus.clamped().toJson().toString())
                .putString(KEY_DIARIES, snapshot.diaryEntries.toDiaryJson().toString())
                .putString(KEY_MESSAGES, snapshot.chatMessages.takeLast(MAX_MESSAGES).toChatJson().toString())
                .apply()
        }
    }

    private fun PetProfile.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("imageUri", imageUri)
        .put("personality", personality.name)
        .put("action", action.name)
        .put("expression", expression)
        .put("decoration", decoration)
        .put("favoriteFood", favoriteFood)
        .put("moodText", moodText)
        .put("companionStyle", companionStyle)
        .put("seed", seed)
        .put("createdAt", createdAt)

    private fun PetStatus.toJson(): JSONObject = JSONObject()
        .put("mood", mood)
        .put("hunger", hunger)
        .put("energy", energy)
        .put("intimacy", intimacy)

    private fun DiaryEntry.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("petId", petId)
        .put("petName", petName)
        .put("petPersonality", petPersonality.name)
        .put("userText", userText)
        .put("petReply", petReply)
        .put("summary", summary)
        .put("moodTag", moodTag)
        .put("createdAt", createdAt)

    private fun ChatMessage.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("role", role.name)
        .put("text", text)
        .put("createdAt", createdAt)

    private fun List<DiaryEntry>.toDiaryJson(): JSONArray {
        val array = JSONArray()
        forEach { array.put(it.toJson()) }
        return array
    }

    private fun List<ChatMessage>.toChatJson(): JSONArray {
        val array = JSONArray()
        forEach { array.put(it.toJson()) }
        return array
    }

    private fun parsePetProfile(json: JSONObject): PetProfile {
        val fallback = generateRandomPet(json.optString("imageUri").takeIf { it.isNotBlank() })
        return PetProfile(
            id = json.optString("id", fallback.id),
            name = json.optString("name", fallback.name),
            imageUri = json.optString("imageUri").takeIf { it.isNotBlank() && it != "null" },
            personality = json.optString("personality").toPersonalityOrNull() ?: fallback.personality,
            action = json.optString("action").toPetActionOrNull() ?: PetAction.Idle,
            expression = json.optString("expression", fallback.expression),
            decoration = json.optString("decoration", fallback.decoration),
            favoriteFood = json.optString("favoriteFood", fallback.favoriteFood),
            moodText = json.optString("moodText", fallback.moodText),
            companionStyle = json.optString("companionStyle", fallback.companionStyle),
            seed = json.optLong("seed", fallback.seed),
            createdAt = json.optLong("createdAt", fallback.createdAt)
        )
    }

    private fun parsePetStatus(json: JSONObject): PetStatus = PetStatus(
        mood = json.optInt("mood", 82),
        hunger = json.optInt("hunger", 28),
        energy = json.optInt("energy", 72),
        intimacy = json.optInt("intimacy", 8)
    )

    private fun parseDiaryEntries(array: JSONArray): List<DiaryEntry> {
        return buildList {
            for (index in 0 until array.length()) {
                runCatching {
                    val json = array.getJSONObject(index)
                    add(
                        DiaryEntry(
                            id = json.optString("id", "diary-${json.optLong("createdAt", index.toLong())}"),
                            petId = json.optString("petId", ""),
                            petName = json.optString("petName", "宠物"),
                            petPersonality = json.optString("petPersonality").toPersonalityOrNull() ?: Personality.Gentle,
                            userText = json.optString("userText", ""),
                            petReply = json.optString("petReply", ""),
                            summary = json.optString("summary", ""),
                            moodTag = json.optString("moodTag", "普通"),
                            createdAt = json.optLong("createdAt", 0L)
                        )
                    )
                }
            }
        }
    }

    private fun parseChatMessages(array: JSONArray): List<ChatMessage> {
        return buildList {
            for (index in 0 until array.length()) {
                runCatching {
                    val json = array.getJSONObject(index)
                    add(
                        ChatMessage(
                            id = json.optString("id", "message-$index"),
                            role = json.optString("role").toMessageRoleOrNull() ?: MessageRole.Pet,
                            text = json.optString("text", ""),
                            createdAt = json.optLong("createdAt", 0L)
                        )
                    )
                }
            }
        }
    }

    private fun String.toPersonalityOrNull(): Personality? {
        return runCatching { Personality.valueOf(this) }.getOrNull()
    }

    private fun String.toPetActionOrNull(): PetAction? {
        return runCatching { PetAction.valueOf(this) }.getOrNull()
    }

    private fun String.toMessageRoleOrNull(): MessageRole? {
        return runCatching { MessageRole.valueOf(this) }.getOrNull()
    }

    private companion object {
        const val KEY_PROFILE = "pet_profile"
        const val KEY_STATUS = "pet_status"
        const val KEY_DIARIES = "diary_entries"
        const val KEY_MESSAGES = "chat_messages"
        const val MAX_MESSAGES = 100
    }
}
