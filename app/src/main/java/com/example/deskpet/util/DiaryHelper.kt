package com.example.deskpet.util

import com.example.deskpet.model.DiaryEntry
import com.example.deskpet.model.PetProfile

object DiaryHelper {
    fun createEntry(
        userText: String,
        petReply: String,
        petProfile: PetProfile,
        summaryOverride: String? = null,
        moodTagOverride: String? = null
    ): DiaryEntry {
        val now = System.currentTimeMillis()
        return DiaryEntry(
            id = "diary-$now",
            petId = petProfile.id,
            petName = petProfile.name,
            petPersonality = petProfile.personality,
            userText = userText,
            petReply = petReply,
            summary = summaryOverride?.takeIf { it.isNotBlank() } ?: summarize(userText),
            moodTag = moodTagOverride?.takeIf { it.isNotBlank() } ?: moodTag(userText),
            createdAt = now
        )
    }

    private fun summarize(text: String): String {
        val trimmed = text.trim()
        if (trimmed.length <= 28) return trimmed
        return trimmed.take(28) + "..."
    }

    private fun moodTag(text: String): String = when {
        text.hasAny("累", "疲惫", "困") -> "疲惫"
        text.hasAny("烦", "压力", "焦虑") -> "压力"
        text.hasAny("难过", "伤心") -> "难过"
        text.hasAny("生气", "气") -> "生气"
        text.hasAny("不想活", "自杀", "伤害自己", "结束生命") -> "需要支持"
        else -> "普通"
    }
}

private fun String.hasAny(vararg keywords: String): Boolean {
    return keywords.any { contains(it, ignoreCase = true) }
}
