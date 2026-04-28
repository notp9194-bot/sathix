package com.sathix.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sathix.app.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun observeForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun pageForChat(chatId: String, limit: Int, offset: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE status IN ('PENDING','FAILED') ORDER BY timestamp ASC")
    suspend fun pendingMessages(): List<MessageEntity>

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE messages SET deletedForEveryone = 1, content = '' WHERE id = :id")
    suspend fun deleteForEveryone(id: String)

    @Query("UPDATE messages SET content = :newContent, editedAt = :editedAt WHERE id = :id")
    suspend fun edit(id: String, newContent: String, editedAt: Long)
}
