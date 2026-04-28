package com.sathix.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sathix.app.data.local.dao.ChatDao
import com.sathix.app.data.local.dao.MessageDao
import com.sathix.app.data.local.dao.UserDao
import com.sathix.app.data.local.entity.ChatEntity
import com.sathix.app.data.local.entity.MessageEntity
import com.sathix.app.data.local.entity.UserEntity

@Database(
    entities = [MessageEntity::class, ChatEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    abstract fun userDao(): UserDao
}
