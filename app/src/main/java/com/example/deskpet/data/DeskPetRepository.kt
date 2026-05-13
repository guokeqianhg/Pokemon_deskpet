package com.example.deskpet.data

import android.content.Context
import com.example.deskpet.data.local.DeskPetDatabase
import com.example.deskpet.data.local.toEntity
import com.example.deskpet.data.local.toModel
import com.example.deskpet.model.ChatMessage
import com.example.deskpet.model.DiaryEntry
import com.example.deskpet.model.MessageRole
import com.example.deskpet.model.Personality
import com.example.deskpet.model.PetAction
import com.example.deskpet.model.PetProfile
import com.example.deskpet.model.PetStatus
import com.example.deskpet.util.defaultStatus
import com.example.deskpet.util.generateRandomPet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    private val database = DeskPetDatabase.createOrNull(context)

    suspend fun loadSnapshot(): DeskPetSnapshot {
        return withContext(Dispatchers.IO) {
            val roomSnapshot = loadFromRoom()
            if (roomSnapshot != null) {
                roomSnapshot
            } else {
                migrateLegacyIfNeeded() ?: loadLegacySnapshot()
            }
        }
    }

    suspend fun savePet(profile: PetProfile, status: PetStatus) {
        withContext(Dispatchers.IO) {
            database?.petDao()?.runCatching {
                upsertPetProfile(profile.toEntity())
                upsertPetStatus(status.clamped().toEntity())
            }
        }
    }

    suspend fun saveDiaryEntries(entries: List<DiaryEntry>) {
        withContext(Dispatchers.IO) {
            database?.diaryDao()?.runCatching {
                clearAllEntries()
                upsertEntries(entries.map { it.toEntity() })
            }
        }
    }

    suspend fun saveChatMessages(messages: List<ChatMessage>) {
        withContext(Dispatchers.IO) {
            database?.chatDao()?.runCatching {
                clearMessages()
                upsertMessages(messages.takeLast(MAX_MESSAGES).map { it.toEntity() })
            }
        }
    }

    suspend fun saveAll(snapshot: DeskPetSnapshot) {
        withContext(Dispatchers.IO) {
            database?.runCatching {
                petDao().upsertPetProfile(snapshot.petProfile.toEntity())
                petDao().upsertPetStatus(snapshot.petStatus.clamped().toEntity())
                diaryDao().clearAllEntries()
                diaryDao().upsertEntries(snapshot.diaryEntries.map { it.toEntity() })
                chatDao().clearMessages()
                chatDao().upsertMessages(snapshot.chatMessages.takeLast(MAX_MESSAGES).map { it.toEntity() })
            }
        }
    }

    suspend fun getDiaryEntry(entryId: String): DiaryEntry? {
        return withContext(Dispatchers.IO) {
            database?.diaryDao()?.runCatching { getEntryById(entryId)?.toModel() }?.getOrNull()
        }
    }

    suspend fun clearPetData() {
        withContext(Dispatchers.IO) {
            database?.petDao()?.runCatching {
                clearPetProfile()
                clearPetStatus()
            }
        }
    }

    private suspend fun loadFromRoom(): DeskPetSnapshot? {
        return withContext(Dispatchers.IO) {
            database?.runCatching {
                val profile = petDao().getPetProfile()
                val status = petDao().getPetStatus()
                val diaries = diaryDao().getAllEntries()
                val chats = chatDao().getAllMessages()

                if (profile == null && status == null && diaries.isEmpty() && chats.isEmpty()) {
                    null
                } else {
                    DeskPetSnapshot(
                        petProfile = profile?.toModel() ?: generateRandomPet(currentImageUri = null),
                        petStatus = status?.toModel() ?: defaultStatus(),
                        diaryEntries = diaries.map { it.toModel() }.sortedByDescending { it.createdAt },
                        chatMessages = chats.map { it.toModel() }
                    )
                }
            }?.getOrNull()
        }
    }

    private suspend fun migrateLegacyIfNeeded(): DeskPetSnapshot? {
        if (database == null) return null
        if (preferences.getBoolean(KEY_MIGRATION_DONE, false)) {
            return loadLegacySnapshot()
        }

        val legacy = loadLegacySnapshot()
        val hasLegacyData = preferences.contains(KEY_PROFILE) ||
            preferences.contains(KEY_STATUS) ||
            preferences.contains(KEY_DIARIES) ||
            preferences.contains(KEY_MESSAGES)

        if (!hasLegacyData) {
            preferences.edit().putBoolean(KEY_MIGRATION_DONE, true).apply()
            return null
        }

        return runCatching {
            saveAll(legacy)
            preferences.edit().putBoolean(KEY_MIGRATION_DONE, true).apply()
            loadFromRoom() ?: legacy
        }.getOrElse {
            legacy
        }
    }

    private fun loadLegacySnapshot(): DeskPetSnapshot {
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

        return DeskPetSnapshot(profile, status.clamped(), diaries.sortedByDescending { it.createdAt }, messages)
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
        const val KEY_MIGRATION_DONE = "room_migration_done"
        const val MAX_MESSAGES = 100
    }
}
