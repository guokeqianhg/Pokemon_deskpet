package com.example.deskpet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_status")
data class PetStatusEntity(
    @PrimaryKey val id: String = "current_status",
    val mood: Int,
    val hunger: Int,
    val energy: Int,
    val intimacy: Int
)
