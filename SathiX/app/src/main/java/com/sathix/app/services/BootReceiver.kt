package com.sathix.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val svc = Intent(context, SyncService::class.java)
        runCatching { context.startForegroundService(svc) }
    }
}
