package com.sathix.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val phone: String = "",
    val name: String = "",
    val about: String = "Hey there! I am using SathiX.",
    val photoUrl: String? = null,
    val online: Boolean = false,
    val lastSeen: Long = 0L,
    val publicKey: String? = null,
    val fcmToken: String? = null
) : Parcelable
