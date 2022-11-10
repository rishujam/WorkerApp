package com.example.composeexperiment

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class CEApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        val channelFilter = NotificationChannel(
            Constants.FILTER_CHANNEL,
            Constants.FILTER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channelFilter)
    }
}