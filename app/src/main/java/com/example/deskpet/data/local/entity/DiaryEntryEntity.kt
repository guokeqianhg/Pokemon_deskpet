package com.example.deskpet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val petName: String,
    val petPersonality: String,
    val userText: String,
    val petReply: String,
    val summary: String,
    val moodTag: String,
    val createdAt: Long
)
