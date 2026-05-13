package com.example.deskpet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.deskpet.data.local.entity.PetProfileEntity
import com.example.deskpet.data.local.entity.PetStatusEntity

@Dao
interface PetDao {
    @Query("SELECT * FROM pet_profile LIMIT 1")
    suspend fun getPetProfile(): PetProfileEntity?

    @Query("SELECT * FROM pet_status LIMIT 1")
    suspend fun getPetStatus(): PetStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPetProfile(profile: PetProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPetStatus(status: PetStatusEntity)

    @Query("DELETE FROM pet_profile")
    suspend fun clearPetProfile()

    @Query("DELETE FROM pet_status")
    suspend fun clearPetStatus()
}
