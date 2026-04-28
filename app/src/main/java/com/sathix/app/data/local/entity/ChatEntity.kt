package com.sathix.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sathix.app.domain.model.Chat
import com.sathix.app.domain.model.ChatType

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val type: String,
    val name: String,
    val photoUrl: String?,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val participantsCsv: String,
    val muted: Boolean,
    val pinned: Boolean,
    val typingUserId: String?
) {
    fun toDomain(): Chat = Chat(
        id = id,
        type = runCatching { ChatType.valueOf(type) }.getOrDefault(ChatType.ONE_TO_ONE),
        name = name, photoUrl = photoUrl,
        lastMessage = lastMessage, lastMessageTime = lastMessageTime,
        unreadCount = unreadCount,
        participants = participantsCsv.split(",").filter { it.isNotBlank() },
        muted = muted, pinned = pinned, typingUserId = typingUserId
    )

    companion object {
        fun fromDomain(c: Chat) = ChatEntity(
            id = c.id, type = c.type.name, name = c.name, photoUrl = c.photoUrl,
            lastMessage = c.lastMessage, lastMessageTime = c.lastMessageTime,
            unreadCount = c.unreadCount,
            participantsCsv = c.participants.joinToString(","),
            muted = c.muted, pinned = c.pinned, typingUserId = c.typingUserId
        )
    }
}
