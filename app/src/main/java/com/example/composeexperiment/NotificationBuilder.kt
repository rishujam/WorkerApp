package com.example.composeexperiment

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat

object NotificationBuilder {

    fun notifyM(context: Context, text: String, title: String): Notification {
        return NotificationCompat.Builder(context, Constants.FILTER_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentText(text)
            .setContentTitle(title)
            .build()
    }
}