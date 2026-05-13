package com.example.deskpet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.deskpet.data.local.dao.ChatDao
import com.example.deskpet.data.local.dao.DiaryDao
import com.example.deskpet.data.local.dao.PetDao
import com.example.deskpet.data.local.entity.ChatMessageEntity
import com.example.deskpet.data.local.entity.DiaryEntryEntity
import com.example.deskpet.data.local.entity.PetProfileEntity
import com.example.deskpet.data.local.entity.PetStatusEntity

@Database(
    entities = [
        PetProfileEntity::class,
        PetStatusEntity::class,
        DiaryEntryEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DeskPetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun diaryDao(): DiaryDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: DeskPetDatabase? = null

        fun createOrNull(context: Context): DeskPetDatabase? {
            return runCatching {
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Room.databaseBuilder(
                        context.applicationContext,
                        DeskPetDatabase::class.java,
                        "deskpet.db"
                    ).fallbackToDestructiveMigration(false)
                        .build()
                        .also { INSTANCE = it }
                }
            }.getOrNull()
        }
    }
}
