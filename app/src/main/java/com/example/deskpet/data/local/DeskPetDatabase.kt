package com.example.deskpet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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
                    ).addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration(false)
                        .build()
                        .also { INSTANCE = it }
                }
            }.getOrNull()
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pet_profile ADD COLUMN stageTheme TEXT NOT NULL DEFAULT 'warm'")
                db.execSQL("ALTER TABLE pet_profile ADD COLUMN accentEmoji TEXT NOT NULL DEFAULT '✨'")
                db.execSQL("ALTER TABLE pet_profile ADD COLUMN actionHint TEXT NOT NULL DEFAULT 'calm'")
            }
        }
    }
}
