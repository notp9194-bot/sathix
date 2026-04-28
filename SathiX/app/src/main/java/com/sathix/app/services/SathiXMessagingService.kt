package com.sathix.app.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sathix.app.R
import com.sathix.app.core.utils.NotificationChannels
import com.sathix.app.domain.repository.AuthRepository
import com.sathix.app.features.calls.CallActivity
import com.sathix.app.features.chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SathiXMessagingService : FirebaseMessagingService() {

    @Inject lateinit var authRepo: AuthRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { authRepo.updateFcmToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        when (data["type"]) {
            "call" -> showIncomingCall(data)
            "message" -> showMessage(data)
            else -> showGeneric(message.notification?.title, message.notification?.body)
        }
    }

    private fun showMessage(data: Map<String, String>) {
        val chatId = data["chatId"].orEmpty()
        val title = data["senderName"] ?: "New message"
        val body = data["preview"] ?: "You have a new message"
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chatId", chatId); putExtra("chatName", title)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(this, chatId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(this, NotificationChannels.MESSAGES)
            .setSmallIcon(R.drawable.ic_chat).setContentTitle(title)
            .setContentText(body).setAutoCancel(true).setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm().notify(chatId.hashCode(), n)
    }

    private fun showIncomingCall(data: Map<String, String>) {
        val callId = data["callId"].orEmpty()
        val peerId = data["peerId"].orEmpty()
        val callType = data["callType"] ?: "AUDIO"
        val intent = Intent(this, CallActivity::class.java).apply {
            putExtra("callId", callId); putExtra("peerId", peerId)
            putExtra("callType", callType); putExtra("isCaller", false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pi = PendingIntent.getActivity(this, callId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(this, NotificationChannels.CALLS)
            .setSmallIcon(R.drawable.ic_call).setContentTitle("Incoming ${callType.lowercase()} call")
            .setContentText("From $peerId").setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX).setOngoing(true)
            .setFullScreenIntent(pi, true).setContentIntent(pi).setAutoCancel(true)
            .build()
        nm().notify(callId.hashCode(), n)
    }

    private fun showGeneric(title: String?, body: String?) {
        val n = NotificationCompat.Builder(this, NotificationChannels.MESSAGES)
            .setSmallIcon(R.drawable.ic_chat).setContentTitle(title ?: "SathiX")
            .setContentText(body ?: "").setAutoCancel(true)
            .build()
        nm().notify(System.currentTimeMillis().toInt(), n)
    }

    private fun nm() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}
