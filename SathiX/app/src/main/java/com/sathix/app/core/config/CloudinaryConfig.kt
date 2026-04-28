package com.sathix.app.core.config

import android.content.Context
import com.cloudinary.android.MediaManager
import com.sathix.app.BuildConfig

object CloudinaryConfig {
    @Volatile private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val cfg = HashMap<String, Any>().apply {
                put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME)
                if (BuildConfig.CLOUDINARY_API_KEY.isNotBlank()) put("api_key", BuildConfig.CLOUDINARY_API_KEY)
                if (BuildConfig.CLOUDINARY_API_SECRET.isNotBlank()) put("api_secret", BuildConfig.CLOUDINARY_API_SECRET)
                put("secure", true)
            }
            runCatching { MediaManager.init(context.applicationContext, cfg) }
            initialized = true
        }
    }
}
