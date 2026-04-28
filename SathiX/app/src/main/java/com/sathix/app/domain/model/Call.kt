package com.sathix.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class CallType { AUDIO, VIDEO }
enum class CallDirection { INCOMING, OUTGOING }
enum class CallStatus { RINGING, ONGOING, ENDED, MISSED, REJECTED, FAILED }

@Parcelize
data class Call(
    val id: String = "",
    val callerId: String = "",
    val calleeId: String = "",
    val groupId: String? = null,
    val type: CallType = CallType.AUDIO,
    val direction: CallDirection = CallDirection.OUTGOING,
    val status: CallStatus = CallStatus.RINGING,
    val startedAt: Long = 0L,
    val endedAt: Long? = null,
    val durationSec: Long = 0L
) : Parcelable
