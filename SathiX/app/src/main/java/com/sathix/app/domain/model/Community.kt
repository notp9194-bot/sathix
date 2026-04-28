package com.sathix.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Community(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val groupIds: List<String> = emptyList(),
    val announcementGroupId: String? = null
) : Parcelable

@Parcelize
data class Channel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val ownerId: String = "",
    val followers: Int = 0,
    val verified: Boolean = false,
    val createdAt: Long = 0L
) : Parcelable
