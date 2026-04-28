package com.sathix.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class ChatType { ONE_TO_ONE, GROUP, COMMUNITY, CHANNEL }

@Parcelize
data class Chat(
    val id: String = "",
    val type: ChatType = ChatType.ONE_TO_ONE,
    val name: String = "",
    val photoUrl: String? = null,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val participants: List<String> = emptyList(),
    val muted: Boolean = false,
    val pinned: Boolean = false,
    val typingUserId: String? = null
) : Parcelable
