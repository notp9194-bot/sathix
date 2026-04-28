package com.sathix.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class StatusType { TEXT, IMAGE, VIDEO, AUDIO }

@Parcelize
data class Status(
    val id: String = "",
    val userId: String = "",
    val type: StatusType = StatusType.TEXT,
    val content: String = "",
    val mediaUrl: String? = null,
    val backgroundColor: String? = null,
    val font: String? = null,
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L,
    val viewers: List<String> = emptyList(),
    val privacy: String = "contacts"
) : Parcelable
