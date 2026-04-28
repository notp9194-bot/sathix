package com.sathix.app.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.sathix.app.data.remote.FirebasePaths
import com.sathix.app.domain.model.Group
import com.sathix.app.domain.model.GroupMember
import com.sathix.app.domain.model.GroupRole
import com.sathix.app.domain.repository.AuthRepository
import com.sathix.app.domain.repository.GroupRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val db: FirebaseDatabase,
    private val authRepo: AuthRepository
) : GroupRepository {

    override fun observeGroups(): Flow<List<Group>> = callbackFlow {
        val uid = authRepo.currentUserId
        if (uid == null) { trySend(emptyList()); awaitClose {}; return@callbackFlow }
        val ref = db.getReference("${FirebasePaths.USERS}/$uid/groups")
        val l = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                trySend(s.children.mapNotNull { it.getValue(Group::class.java) })
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        }
        ref.addValueEventListener(l)
        awaitClose { ref.removeEventListener(l) }
    }

    override suspend fun createGroup(
        name: String, description: String, memberIds: List<String>, photoUrl: String?
    ): Result<Group> = runCatching {
        val uid = authRepo.currentUserId ?: error("Not signed in")
        val ref = db.getReference(FirebasePaths.GROUPS).push()
        val now = System.currentTimeMillis()
        val members = (listOf(uid) + memberIds).distinct().mapIndexed { i, m ->
            GroupMember(uid = m, role = if (i == 0) GroupRole.OWNER else GroupRole.MEMBER, joinedAt = now)
        }
        val group = Group(
            id = ref.key!!, name = name, description = description, photoUrl = photoUrl,
            createdBy = uid, createdAt = now, members = members
        )
        ref.setValue(group).await()
        members.forEach {
            db.getReference("${FirebasePaths.USERS}/${it.uid}/groups/${group.id}").setValue(group).await()
        }
        group
    }

    override suspend fun addMembers(groupId: String, uids: List<String>): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        uids.forEach { uid ->
            db.getReference("${FirebasePaths.GROUPS}/$groupId/members/$uid")
                .setValue(GroupMember(uid, GroupRole.MEMBER, now)).await()
        }
    }

    override suspend fun removeMember(groupId: String, uid: String): Result<Unit> = runCatching {
        db.getReference("${FirebasePaths.GROUPS}/$groupId/members/$uid").removeValue().await()
        db.getReference("${FirebasePaths.USERS}/$uid/groups/$groupId").removeValue().await()
    }

    override suspend fun setRole(groupId: String, uid: String, role: GroupRole): Result<Unit> = runCatching {
        db.getReference("${FirebasePaths.GROUPS}/$groupId/members/$uid/role").setValue(role.name).await()
    }

    override suspend fun leaveGroup(groupId: String): Result<Unit> = runCatching {
        val uid = authRepo.currentUserId ?: error("Not signed in")
        removeMember(groupId, uid).getOrThrow()
    }

    override suspend fun joinViaLink(link: String): Result<Group> = runCatching {
        val token = link.substringAfterLast("/")
        val groupId = db.getReference("inviteLinks/$token").get().await().getValue(String::class.java)
            ?: error("Invalid invite")
        val uid = authRepo.currentUserId ?: error("Not signed in")
        addMembers(groupId, listOf(uid)).getOrThrow()
        db.getReference("${FirebasePaths.GROUPS}/$groupId").get().await().getValue(Group::class.java)
            ?: error("Group not found")
    }

    override suspend fun generateInviteLink(groupId: String): Result<String> = runCatching {
        val token = UUID.randomUUID().toString().take(12)
        db.getReference("inviteLinks/$token").setValue(groupId).await()
        val link = "https://sathix.app/g/$token"
        db.getReference("${FirebasePaths.GROUPS}/$groupId/joinLink").setValue(link).await()
        link
    }
}
