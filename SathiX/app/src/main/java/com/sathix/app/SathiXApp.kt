package com.sathix.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.sathix.app.core.config.CloudinaryConfig
import com.sathix.app.core.utils.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SathiXApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        FirebaseApp.initializeApp(this)
        runCatching { FirebaseDatabase.getInstance().setPersistenceEnabled(true) }

        CloudinaryConfig.init(this)

        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "New chat messages" }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.CALLS,
                "Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming voice and video calls"
                setBypassDnd(true)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.SYNC,
                "Background Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Syncing your data" }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.STATUS,
                "Status Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    companion object {
        @Volatile lateinit var instance: SathiXApp
            private set
    }
}
