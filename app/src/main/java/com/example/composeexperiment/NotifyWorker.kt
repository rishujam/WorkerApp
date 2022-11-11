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


    override suspend fun doWork(): Result {
        askQuestion()
        return Result.success()
    }

    private fun askQuestion() {
        val intent = Intent(context, AskAudioService::class.java)
        context.startForegroundService(intent)
    }
}