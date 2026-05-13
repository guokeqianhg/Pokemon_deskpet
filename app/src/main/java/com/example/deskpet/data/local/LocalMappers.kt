package com.example.deskpet.data.local

import com.example.deskpet.data.local.entity.ChatMessageEntity
import com.example.deskpet.data.local.entity.DiaryEntryEntity
import com.example.deskpet.data.local.entity.PetProfileEntity
import com.example.deskpet.data.local.entity.PetStatusEntity
import com.example.deskpet.model.ChatMessage
import com.example.deskpet.model.DiaryEntry
import com.example.deskpet.model.MessageRole
import com.example.deskpet.model.Personality
import com.example.deskpet.model.PetAction
import com.example.deskpet.model.PetProfile
import com.example.deskpet.model.PetStatus
import com.example.deskpet.util.generateRandomPet

fun PetProfile.toEntity(): PetProfileEntity = PetProfileEntity(
    id = id,
    name = name,
    imageUri = imageUri,
    personality = personality.name,
    action = action.name,
    expression = expression,
    decoration = decoration,
    favoriteFood = favoriteFood,
    moodText = moodText,
    companionStyle = companionStyle,
    stageTheme = stageTheme,
    accentEmoji = accentEmoji,
    actionHint = actionHint,
    seed = seed,
    createdAt = createdAt
)

fun PetStatus.toEntity(): PetStatusEntity = PetStatusEntity(
    mood = mood,
    hunger = hunger,
    energy = energy,
    intimacy = intimacy
)

fun DiaryEntry.toEntity(): DiaryEntryEntity = DiaryEntryEntity(
    id = id,
    petId = petId,
    petName = petName,
    petPersonality = petPersonality.name,
    userText = userText,
    petReply = petReply,
    summary = summary,
    moodTag = moodTag,
    createdAt = createdAt
)

fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    role = role.name,
    text = text,
    createdAt = createdAt
)

fun PetProfileEntity.toModel(): PetProfile {
    val fallback = generateRandomPet(imageUri)
    return PetProfile(
        id = id,
        name = name,
        imageUri = imageUri,
        personality = personality.toPersonalityOrNull() ?: fallback.personality,
        action = action.toPetActionOrNull() ?: PetAction.Idle,
        expression = expression,
        decoration = decoration,
        favoriteFood = favoriteFood,
        moodText = moodText,
        companionStyle = companionStyle,
        stageTheme = stageTheme.ifBlank { fallback.stageTheme },
        accentEmoji = accentEmoji.ifBlank { fallback.accentEmoji },
        actionHint = actionHint.ifBlank { fallback.actionHint },
        seed = seed,
        createdAt = createdAt
    )
}

fun PetStatusEntity.toModel(): PetStatus = PetStatus(
    mood = mood,
    hunger = hunger,
    energy = energy,
    intimacy = intimacy
).clamped()

fun DiaryEntryEntity.toModel(): DiaryEntry = DiaryEntry(
    id = id,
    petId = petId,
    petName = petName,
    petPersonality = petPersonality.toPersonalityOrNull() ?: Personality.Gentle,
    userText = userText,
    petReply = petReply,
    summary = summary,
    moodTag = moodTag,
    createdAt = createdAt
)

fun ChatMessageEntity.toModel(): ChatMessage = ChatMessage(
    id = id,
    role = role.toMessageRoleOrNull() ?: MessageRole.Pet,
    text = text,
    createdAt = createdAt
)

private fun String.toPersonalityOrNull(): Personality? {
    return runCatching { Personality.valueOf(this) }.getOrNull()
}

private fun String.toPetActionOrNull(): PetAction? {
    return runCatching { PetAction.valueOf(this) }.getOrNull()
}

private fun String.toMessageRoleOrNull(): MessageRole? {
    return runCatching { MessageRole.valueOf(this) }.getOrNull()
}
