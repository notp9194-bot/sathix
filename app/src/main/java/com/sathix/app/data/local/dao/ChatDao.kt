package com.sathix.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sathix.app.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(chats: List<ChatEntity>)

    @Query("SELECT * FROM chats ORDER BY pinned DESC, lastMessageTime DESC")
    fun observeAll(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ChatEntity?

    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :id")
    suspend fun clearUnread(id: String)

    @Query("UPDATE chats SET muted = :muted WHERE id = :id")
    suspend fun setMuted(id: String, muted: Boolean)

    @Query("UPDATE chats SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun delete(id: String)
}
