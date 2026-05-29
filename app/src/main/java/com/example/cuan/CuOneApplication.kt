package com.example.cuan

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.sync.DailyReminderWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * CuOne Application class with Hilt
 */
@HiltAndroidApp
class CuOneApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        setupNotificationChannels()
        scheduleReminderOnStartup()
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "daily_reminder_channel"
            val channelName = "Reminder Harian"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Pengingat mencatat transaksi harian"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleReminderOnStartup() {
        val appDataStore = AppDataStore(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val enabled = appDataStore.dailyReminderEnabled.first()
                if (enabled) {
                    DailyReminderWorker.schedule(this@CuOneApplication)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}