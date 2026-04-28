package com.sathix.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sathix.app.R
import com.sathix.app.core.utils.NotificationChannels
import com.sathix.app.data.local.dao.MessageDao
import com.sathix.app.domain.repository.ChatRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : Service() {

    @Inject lateinit var messageDao: MessageDao
    @Inject lateinit var chatRepo: ChatRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val n = NotificationCompat.Builder(this, NotificationChannels.SYNC)
            .setSmallIcon(R.drawable.ic_sync).setContentTitle("Syncing SathiX")
            .setContentText("Updating chats and messages").setOngoing(true).build()
        startForeground(7777, n)
        scope.launch {
            messageDao.pendingMessages().forEach { runCatching { chatRepo.retryFailed(it.id) } }
            stopSelf()
        }
        return START_NOT_STICKY
    }
}
