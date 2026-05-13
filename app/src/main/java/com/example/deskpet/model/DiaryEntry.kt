package com.example.deskpet.model

data class DiaryEntry(
    val id: String,
    val petId: String,
    val petName: String,
    val petPersonality: Personality,
    val userText: String,
    val petReply: String,
    val summary: String,
    val moodTag: String,
    val createdAt: Long
)
