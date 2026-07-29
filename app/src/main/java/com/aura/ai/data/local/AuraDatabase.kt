package com.aura.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.ai.data.local.dao.ChatDao
import com.aura.ai.data.local.entity.ChatEntity
import com.aura.ai.data.local.entity.MessageEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        const val NAME = "aura.db"
    }
}
