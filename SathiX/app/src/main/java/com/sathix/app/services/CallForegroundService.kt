package com.sathix.app.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.sathix.app.R
import com.sathix.app.core.utils.NotificationChannels
import com.sathix.app.features.calls.CallActivity

class CallForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callId = intent?.getStringExtra("callId").orEmpty()
        val peerId = intent?.getStringExtra("peerId").orEmpty()
        val type = intent?.getStringExtra("callType") ?: "AUDIO"
        val tap = Intent(this, CallActivity::class.java).apply {
            putExtra("callId", callId); putExtra("peerId", peerId); putExtra("callType", type)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(this, 0, tap,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(this, NotificationChannels.CALLS)
            .setSmallIcon(R.drawable.ic_call).setContentTitle("On call")
            .setContentText("$type call with $peerId").setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true).setContentIntent(pi).build()
        val fgType = if (Build.VERSION.SDK_INT >= 30) ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL else 0
        ServiceCompat.startForeground(this, NOTIF_ID, n, fgType)
        return START_STICKY
    }

    companion object { private const val NOTIF_ID = 4242 }
}
