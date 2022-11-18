package com.example.composeexperiment

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

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