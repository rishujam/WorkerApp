package com.example.composeexperiment

import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.composeexperiment.Constants.ACTION_STOP


class AskAudioService: Service() {

    lateinit var exoPlayer: ExoPlayer

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationBuilder.notifyM(this, "NotifyWorker", "Worker")
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null && intent.action.equals(
                ACTION_STOP, ignoreCase = true)) {
            stopForeground(true)
            stopSelf()
        }
        setupPlayer()
        return START_NOT_STICKY
    }

    private fun getAlarmUri(): Uri {
        var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
        }
        return alarmUri
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupPlayer(){
        val url = "https://file-examples.com/storage/fe8c7eef0c6364f6c9504cc/2017/11/file_example_MP3_2MG.mp3"
        exoPlayer = ExoPlayer.Builder(applicationContext)
            .build()
            .apply {
                val defaultDataSourceFactory = DefaultDataSource.Factory(applicationContext)
                val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                    applicationContext,
                    defaultDataSourceFactory
                )
                val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(getAlarmUri()))

                setMediaSource(source)
                prepare()
            }

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if(state==Player.STATE_READY){

                }
                if(state == Player.STATE_ENDED) {

                }
            }
        })
        exoPlayer.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if(this::exoPlayer.isInitialized) {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }
}