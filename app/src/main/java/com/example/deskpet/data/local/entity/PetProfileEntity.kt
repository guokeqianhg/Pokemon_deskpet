package com.example.deskpet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_profile")
data class PetProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUri: String?,
    val personality: String,
    val action: String,
    val expression: String,
    val decoration: String,
    val favoriteFood: String,
    val moodText: String,
    val companionStyle: String,
    val stageTheme: String,
    val accentEmoji: String,
    val actionHint: String,
    val imageScale: Float,
    val imageOffsetX: Float,
    val imageOffsetY: Float,
    val seed: Long,
    val createdAt: Long
)
