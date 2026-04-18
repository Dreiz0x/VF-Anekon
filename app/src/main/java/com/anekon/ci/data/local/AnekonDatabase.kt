package com.anekon.ci.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.anekon.ci.data.local.dao.*
import com.anekon.ci.data.local.entity.*

/**
 * Database de Anekon - Room
 */
@Database(
    entities = [
        ProjectEntity::class,
        BuildEntity::class,
        ChatMessageEntity::class,
        GeneratedAppEntity::class,
        GitHubAccountEntity::class,
        AISettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AnekonDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun buildDao(): BuildDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun generatedAppDao(): GeneratedAppDao
    abstract fun githubAccountDao(): GitHubAccountDao
    abstract fun aiSettingsDao(): AISettingsDao
}