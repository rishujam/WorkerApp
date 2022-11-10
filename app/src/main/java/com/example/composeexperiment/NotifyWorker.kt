package com.example.composeexperiment

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class NotifyWorker(
    private val context: Context,
    private val workParams: WorkerParameters
) : CoroutineWorker(context, workParams) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        val notification = NotificationBuilder.notifyM(context, "NotifyWorker", "Worker")
        notificationManager.notify(
            Random.nextInt(), notification
        )
        askQuestion()
        return Result.success()
    }

    private fun askQuestion() {
        val intent = Intent(context, AskAudioService::class.java)
        context.startService(intent)
    }
}