package com.sathix.app.domain.repository

import com.sathix.app.domain.model.Call
import com.sathix.app.domain.model.CallType
import kotlinx.coroutines.flow.Flow

interface CallRepository {
    fun observeCallHistory(): Flow<List<Call>>
    fun observeIncomingCalls(): Flow<Call?>
    suspend fun startCall(calleeId: String, type: CallType, groupId: String? = null): Result<Call>
    suspend fun acceptCall(callId: String): Result<Unit>
    suspend fun rejectCall(callId: String): Result<Unit>
    suspend fun endCall(callId: String): Result<Unit>
    suspend fun logMissed(callId: String)
}
