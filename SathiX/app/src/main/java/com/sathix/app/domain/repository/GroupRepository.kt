package com.sathix.app.domain.repository

import com.sathix.app.domain.model.Group
import com.sathix.app.domain.model.GroupRole
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun observeGroups(): Flow<List<Group>>
    suspend fun createGroup(name: String, description: String, memberIds: List<String>, photoUrl: String?): Result<Group>
    suspend fun addMembers(groupId: String, uids: List<String>): Result<Unit>
    suspend fun removeMember(groupId: String, uid: String): Result<Unit>
    suspend fun setRole(groupId: String, uid: String, role: GroupRole): Result<Unit>
    suspend fun leaveGroup(groupId: String): Result<Unit>
    suspend fun joinViaLink(link: String): Result<Group>
    suspend fun generateInviteLink(groupId: String): Result<String>
}
