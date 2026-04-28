package com.sathix.app.data.repository

import com.sathix.app.data.local.dao.ChatDao
import com.sathix.app.data.local.dao.MessageDao
import com.sathix.app.data.local.entity.MessageEntity
import com.sathix.app.data.remote.FirebaseChatDataSource
import com.sathix.app.domain.model.Chat
import com.sathix.app.domain.model.Message
import com.sathix.app.domain.model.MessageStatus
import com.sathix.app.domain.model.MessageType
import com.sathix.app.domain.repository.AuthRepository
import com.sathix.app.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val remote: FirebaseChatDataSource,
    private val authRepo: AuthRepository
) : ChatRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun observeChats(): Flow<List<Chat>> =
        chatDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeMessages(chatId: String): Flow<List<Message>> {
        scope.launch {
            remote.observeMessages(chatId).collect { msg ->
                messageDao.upsert(MessageEntity.fromDomain(msg.copy(chatId = chatId)))
            }
        }
        return messageDao.observeForChat(chatId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun sendMessage(
        chatId: String,
        content: String,
        type: MessageType,
        mediaUrl: String?,
        replyToId: String?
    ): Result<Message> = runCatching {
        val uid = authRepo.currentUserId ?: error("Not signed in")
        val localId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val pending = Message(
            id = localId, chatId = chatId, senderId = uid,
            type = type, content = content, mediaUrl = mediaUrl,
            replyToId = replyToId, status = MessageStatus.PENDING, timestamp = now
        )
        messageDao.upsert(MessageEntity.fromDomain(pending))
        val remoteId = runCatching { remote.pushMessage(chatId, pending) }.getOrNull()
        if (remoteId != null) {
            messageDao.delete(localId)
            messageDao.upsert(MessageEntity.fromDomain(pending.copy(id = remoteId, status = MessageStatus.SENT)))
            pending.copy(id = remoteId, status = MessageStatus.SENT)
        } else {
            messageDao.updateStatus(localId, MessageStatus.FAILED.name)
            pending.copy(status = MessageStatus.FAILED)
        }
    }

    override suspend fun markDelivered(chatId: String, messageId: String) {
        runCatching { remote.updateMessageStatus(chatId, messageId, MessageStatus.DELIVERED.name) }
        messageDao.updateStatus(messageId, MessageStatus.DELIVERED.name)
    }

    override suspend fun markSeen(chatId: String, messageId: String) {
        runCatching { remote.updateMessageStatus(chatId, messageId, MessageStatus.SEEN.name) }
        messageDao.updateStatus(messageId, MessageStatus.SEEN.name)
        chatDao.clearUnread(chatId)
    }

    override suspend fun setTyping(chatId: String, typing: Boolean) {
        val uid = authRepo.currentUserId ?: return
        runCatching { remote.setTyping(chatId, uid, typing) }
    }

    override suspend fun deleteMessage(chatId: String, messageId: String, forEveryone: Boolean) {
        runCatching { remote.deleteMessage(chatId, messageId, forEveryone) }
        if (forEveryone) messageDao.deleteForEveryone(messageId) else messageDao.delete(messageId)
    }

    override suspend fun editMessage(chatId: String, messageId: String, newContent: String) {
        runCatching { remote.editMessage(chatId, messageId, newContent) }
        messageDao.edit(messageId, newContent, System.currentTimeMillis())
    }

    override suspend fun retryFailed(messageId: String) {
        val pending = messageDao.pendingMessages().firstOrNull { it.id == messageId } ?: return
        val msg = pending.toDomain()
        runCatching {
            val remoteId = remote.pushMessage(msg.chatId, msg)
            messageDao.delete(messageId)
            messageDao.upsert(MessageEntity.fromDomain(msg.copy(id = remoteId, status = MessageStatus.SENT)))
        }
    }

    override suspend fun pinChat(chatId: String, pinned: Boolean) = chatDao.setPinned(chatId, pinned)
    override suspend fun muteChat(chatId: String, muted: Boolean) = chatDao.setMuted(chatId, muted)
}
