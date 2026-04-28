package com.sathix.app.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.sathix.app.data.remote.FirebasePaths
import com.sathix.app.domain.model.Call
import com.sathix.app.domain.model.CallDirection
import com.sathix.app.domain.model.CallStatus
import com.sathix.app.domain.model.CallType
import com.sathix.app.domain.repository.AuthRepository
import com.sathix.app.domain.repository.CallRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor(
    private val db: FirebaseDatabase,
    private val authRepo: AuthRepository
) : CallRepository {

    override fun observeCallHistory(): Flow<List<Call>> = callbackFlow {
        val uid = authRepo.currentUserId
        if (uid == null) { trySend(emptyList()); awaitClose {}; return@callbackFlow }
        val ref = db.getReference("${FirebasePaths.CALLS}/history/$uid")
        val l = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                trySend(s.children.mapNotNull { it.getValue(Call::class.java) })
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        }
        ref.addValueEventListener(l)
        awaitClose { ref.removeEventListener(l) }
    }

    override fun observeIncomingCalls(): Flow<Call?> = callbackFlow {
        val uid = authRepo.currentUserId
        if (uid == null) { trySend(null); awaitClose {}; return@callbackFlow }
        val ref = db.getReference("${FirebasePaths.CALLS}/incoming/$uid")
        val l = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                trySend(s.children.firstOrNull()?.getValue(Call::class.java))
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        }
        ref.addValueEventListener(l)
        awaitClose { ref.removeEventListener(l) }
    }

    override suspend fun startCall(calleeId: String, type: CallType, groupId: String?): Result<Call> = runCatching {
        val uid = authRepo.currentUserId ?: error("Not signed in")
        val ref = db.getReference("${FirebasePaths.CALLS}/active").push()
        val callId = ref.key!!
        val call = Call(
            id = callId, callerId = uid, calleeId = calleeId,
            groupId = groupId, type = type, direction = CallDirection.OUTGOING,
            status = CallStatus.RINGING, startedAt = System.currentTimeMillis()
        )
        ref.setValue(call).await()
        db.getReference("${FirebasePaths.CALLS}/incoming/$calleeId/$callId")
            .setValue(call.copy(direction = CallDirection.INCOMING)).await()
        call
    }

    override suspend fun acceptCall(callId: String): Result<Unit> = runCatching {
        db.getReference("${FirebasePaths.CALLS}/active/$callId/status")
            .setValue(CallStatus.ONGOING.name).await()
    }

    override suspend fun rejectCall(callId: String): Result<Unit> = runCatching {
        db.getReference("${FirebasePaths.CALLS}/active/$callId/status")
            .setValue(CallStatus.REJECTED.name).await()
        val uid = authRepo.currentUserId ?: return@runCatching
        db.getReference("${FirebasePaths.CALLS}/incoming/$uid/$callId").removeValue().await()
    }

    override suspend fun endCall(callId: String): Result<Unit> = runCatching {
        val updates = mapOf(
            "status" to CallStatus.ENDED.name,
            "endedAt" to ServerValue.TIMESTAMP
        )
        db.getReference("${FirebasePaths.CALLS}/active/$callId").updateChildren(updates).await()
    }

    override suspend fun logMissed(callId: String) {
        runCatching {
            db.getReference("${FirebasePaths.CALLS}/active/$callId/status")
                .setValue(CallStatus.MISSED.name).await()
        }
    }
}
