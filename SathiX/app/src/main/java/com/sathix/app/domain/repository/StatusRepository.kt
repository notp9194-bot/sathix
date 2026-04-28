package com.sathix.app.domain.repository

import com.sathix.app.domain.model.Status
import com.sathix.app.domain.model.StatusType
import kotlinx.coroutines.flow.Flow

interface StatusRepository {
    fun observeStatuses(): Flow<Map<String, List<Status>>>
    suspend fun postStatus(type: StatusType, content: String, mediaUrl: String? = null, backgroundColor: String? = null): Result<Status>
    suspend fun viewStatus(statusId: String, ownerId: String)
    suspend fun deleteStatus(statusId: String)
}
