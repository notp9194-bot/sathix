package com.sathix.app.domain.repository

import com.sathix.app.domain.model.Chat
import com.sathix.app.domain.model.Message
import com.sathix.app.domain.model.MessageType
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeChats(): Flow<List<Chat>>
    fun observeMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(chatId: String, content: String, type: MessageType = MessageType.TEXT, mediaUrl: String? = null, replyToId: String? = null): Result<Message>
    suspend fun markDelivered(chatId: String, messageId: String)
    suspend fun markSeen(chatId: String, messageId: String)
    suspend fun setTyping(chatId: String, typing: Boolean)
    suspend fun deleteMessage(chatId: String, messageId: String, forEveryone: Boolean)
    suspend fun editMessage(chatId: String, messageId: String, newContent: String)
    suspend fun retryFailed(messageId: String)
    suspend fun pinChat(chatId: String, pinned: Boolean)
    suspend fun muteChat(chatId: String, muted: Boolean)
}
