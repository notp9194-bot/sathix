package com.sathix.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sathix.app.domain.model.Message
import com.sathix.app.domain.model.MessageStatus
import com.sathix.app.domain.model.MessageType

@Entity(
    tableName = "messages",
    indices = [Index("chatId"), Index("timestamp"), Index("status")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val type: String,
    val content: String,
    val mediaUrl: String?,
    val mediaThumbnail: String?,
    val mediaSize: Long,
    val mediaDuration: Long,
    val replyToId: String?,
    val status: String,
    val timestamp: Long,
    val editedAt: Long?,
    val deletedForEveryone: Boolean,
    val encrypted: Boolean,
    val deliveredToCsv: String,
    val seenByCsv: String
) {
    fun toDomain(): Message = Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        type = runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.TEXT),
        content = content,
        mediaUrl = mediaUrl,
        mediaThumbnail = mediaThumbnail,
        mediaSize = mediaSize,
        mediaDuration = mediaDuration,
        replyToId = replyToId,
        status = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT),
        timestamp = timestamp,
        editedAt = editedAt,
        deletedForEveryone = deletedForEveryone,
        encrypted = encrypted,
        deliveredTo = deliveredToCsv.split(",").filter { it.isNotBlank() },
        seenBy = seenByCsv.split(",").filter { it.isNotBlank() }
    )

    companion object {
        fun fromDomain(m: Message) = MessageEntity(
            id = m.id, chatId = m.chatId, senderId = m.senderId,
            type = m.type.name, content = m.content,
            mediaUrl = m.mediaUrl, mediaThumbnail = m.mediaThumbnail,
            mediaSize = m.mediaSize, mediaDuration = m.mediaDuration,
            replyToId = m.replyToId, status = m.status.name,
            timestamp = m.timestamp, editedAt = m.editedAt,
            deletedForEveryone = m.deletedForEveryone, encrypted = m.encrypted,
            deliveredToCsv = m.deliveredTo.joinToString(","),
            seenByCsv = m.seenBy.joinToString(",")
        )
    }
}
