package com.example.composeexperiment

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.example.composeexperiment.Constants.ACTION_STOP

class AskAudioService: Service() {

    lateinit var mediaPlayer: MediaPlayer

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null && intent.action.equals(
                ACTION_STOP, ignoreCase = true)) {
            stopForeground(true)
            stopSelf()
        }
        playSong()
        return START_NOT_STICKY
    }

    private fun playSong() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource("https://file-examples.com/storage/fe8c7eef0c6364f6c9504cc/2017/11/file_example_MP3_2MG.mp3")
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(this::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
        }
    }
}