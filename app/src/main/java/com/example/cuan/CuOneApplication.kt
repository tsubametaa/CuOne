package com.example.cuan

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * CuOne Application class with Hilt
 */
@HiltAndroidApp
class CuOneApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Setup notification channels
        setupNotificationChannels()
    }

    private fun setupNotificationChannels() {
        // Notification channels would be created here
        // For Android 13+, we need to request POST_NOTIFICATIONS permission at runtime
    }
}