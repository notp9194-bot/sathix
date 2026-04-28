package com.sathix.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class MessageType { TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT, LOCATION, CONTACT, SYSTEM }
enum class MessageStatus { PENDING, SENT, DELIVERED, SEEN, FAILED }

@Parcelize
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val type: MessageType = MessageType.TEXT,
    val content: String = "",
    val mediaUrl: String? = null,
    val mediaThumbnail: String? = null,
    val mediaSize: Long = 0L,
    val mediaDuration: Long = 0L,
    val replyToId: String? = null,
    val status: MessageStatus = MessageStatus.PENDING,
    val timestamp: Long = 0L,
    val editedAt: Long? = null,
    val deletedForEveryone: Boolean = false,
    val encrypted: Boolean = true,
    val deliveredTo: List<String> = emptyList(),
    val seenBy: List<String> = emptyList()
) : Parcelable
