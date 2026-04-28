package com.sathix.app.data.remote

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.sathix.app.domain.model.Chat
import com.sathix.app.domain.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseChatDataSource @Inject constructor(
    private val db: FirebaseDatabase
) {

    fun observeChats(uid: String): Flow<List<Chat>> = callbackFlow {
        val ref = db.getReference(FirebasePaths.userChats(uid))
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chats = snapshot.children.mapNotNull { it.getValue(Chat::class.java) }
                trySend(chats)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeMessages(chatId: String): Flow<Message> = callbackFlow {
        val ref = db.getReference(FirebasePaths.userMessages(chatId))
            .orderByChild("timestamp")
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, prev: String?) {
                snapshot.getValue(Message::class.java)?.let { trySend(it.copy(id = snapshot.key ?: it.id)) }
            }
            override fun onChildChanged(snapshot: DataSnapshot, prev: String?) {
                snapshot.getValue(Message::class.java)?.let { trySend(it.copy(id = snapshot.key ?: it.id)) }
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, prev: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun pushMessage(chatId: String, message: Message): String {
        val ref = db.getReference(FirebasePaths.userMessages(chatId)).push()
        val map = mapOf(
            "senderId" to message.senderId,
            "type" to message.type.name,
            "content" to message.content,
            "mediaUrl" to message.mediaUrl,
            "mediaThumbnail" to message.mediaThumbnail,
            "mediaSize" to message.mediaSize,
            "mediaDuration" to message.mediaDuration,
            "replyToId" to message.replyToId,
            "status" to "SENT",
            "timestamp" to ServerValue.TIMESTAMP,
            "encrypted" to message.encrypted
        )
        ref.setValue(map).await()
        return ref.key!!
    }

    suspend fun updateMessageStatus(chatId: String, messageId: String, status: String) {
        db.getReference("${FirebasePaths.userMessages(chatId)}/$messageId/status")
            .setValue(status).await()
    }

    suspend fun setTyping(chatId: String, uid: String, typing: Boolean) {
        val ref: DatabaseReference = db.getReference(FirebasePaths.userTyping(chatId, uid))
        if (typing) {
            ref.setValue(ServerValue.TIMESTAMP).await()
            ref.onDisconnect().removeValue()
        } else {
            ref.removeValue().await()
        }
    }

    suspend fun deleteMessage(chatId: String, messageId: String, forEveryone: Boolean) {
        val ref = db.getReference("${FirebasePaths.userMessages(chatId)}/$messageId")
        if (forEveryone) {
            ref.updateChildren(mapOf("deletedForEveryone" to true, "content" to "")).await()
        } else {
            ref.removeValue().await()
        }
    }

    suspend fun editMessage(chatId: String, messageId: String, newContent: String) {
        db.getReference("${FirebasePaths.userMessages(chatId)}/$messageId")
            .updateChildren(mapOf("content" to newContent, "editedAt" to ServerValue.TIMESTAMP)).await()
    }
}
