package com.sathix.app.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.sathix.app.data.remote.FirebasePaths
import com.sathix.app.domain.model.Status
import com.sathix.app.domain.model.StatusType
import com.sathix.app.domain.repository.AuthRepository
import com.sathix.app.domain.repository.StatusRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepositoryImpl @Inject constructor(
    private val db: FirebaseDatabase,
    private val authRepo: AuthRepository
) : StatusRepository {

    override fun observeStatuses(): Flow<Map<String, List<Status>>> = callbackFlow {
        val ref = db.getReference(FirebasePaths.STATUSES)
        val l = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                val now = System.currentTimeMillis()
                val map = mutableMapOf<String, List<Status>>()
                s.children.forEach { userNode ->
                    val list = userNode.children.mapNotNull { it.getValue(Status::class.java) }
                        .filter { it.expiresAt > now }
                    if (list.isNotEmpty()) map[userNode.key.orEmpty()] = list
                }
                trySend(map)
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        }
        ref.addValueEventListener(l)
        awaitClose { ref.removeEventListener(l) }
    }

    override suspend fun postStatus(
        type: StatusType,
        content: String,
        mediaUrl: String?,
        backgroundColor: String?
    ): Result<Status> = runCatching {
        val uid = authRepo.currentUserId ?: error("Not signed in")
        val ref = db.getReference(FirebasePaths.userStatuses(uid)).push()
        val now = System.currentTimeMillis()
        val status = Status(
            id = ref.key!!, userId = uid, type = type, content = content,
            mediaUrl = mediaUrl, backgroundColor = backgroundColor,
            createdAt = now, expiresAt = now + 24L * 60 * 60 * 1000
        )
        ref.setValue(status).await()
        status
    }

    override suspend fun viewStatus(statusId: String, ownerId: String) {
        val uid = authRepo.currentUserId ?: return
        runCatching {
            db.getReference("${FirebasePaths.userStatuses(ownerId)}/$statusId/viewers/$uid")
                .setValue(ServerValue.TIMESTAMP).await()
        }
    }

    override suspend fun deleteStatus(statusId: String) {
        val uid = authRepo.currentUserId ?: return
        runCatching { db.getReference("${FirebasePaths.userStatuses(uid)}/$statusId").removeValue().await() }
    }
}
