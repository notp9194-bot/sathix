package com.sathix.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class GroupRole { OWNER, ADMIN, MEMBER }

@Parcelize
data class GroupMember(
    val uid: String = "",
    val role: GroupRole = GroupRole.MEMBER,
    val joinedAt: Long = 0L
) : Parcelable

@Parcelize
data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val members: List<GroupMember> = emptyList(),
    val maxParticipants: Int = 1024,
    val joinLink: String? = null,
    val onlyAdminsCanMessage: Boolean = false,
    val communityId: String? = null
) : Parcelable
