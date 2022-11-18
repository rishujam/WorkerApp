package com.example.composeexperiment

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.composeexperiment.feature_gsheet.TestApi
import com.example.composeexperiment.feature_gsheet.TestData
import com.example.composeexperiment.ui.theme.ComposeExperimentTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private var recorder: MediaRecorder? = null
    private var fileName: String = ""
    private val mainViewModel: MainViewModel by viewModels()
    lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val workManager = WorkManager.getInstance(applicationContext)

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }

        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        setContent {
            ComposeExperimentTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MyContent(workManager)
                }
            }
        }

        checkPermission()
    }

    private fun sendToSheets() = CoroutineScope(Dispatchers.IO).launch {
        try {
            TestApi.instance.exportData(TestData("Sudhanshu", "12"))
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception) {
            withContext(Dispatchers.Main) {
                Log.e("RishuTest", e.message.toString())
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupPlayer(){
        val url = "https://file-examples.com/storage/fe8c7eef0c6364f6c9504cc/2017/11/file_example_MP3_2MG.mp3"
        try {
            exoPlayer = ExoPlayer.Builder(applicationContext)
                .build()
                .apply {
                    val defaultDataSourceFactory = DefaultDataSource.Factory(applicationContext)
                    val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                        applicationContext,
                        defaultDataSourceFactory
                    )
                    val uri = getAlarmUri()
                    Log.d("RishuTest", uri.toString())
                    val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(uri))

                    setMediaSource(source)
                    prepare()
                }

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if(state== Player.STATE_READY){

                    }
                    if(state == Player.STATE_ENDED) {

                    }
                }
            })
            exoPlayer.playWhenReady = true
        }catch (e:Exception) {
            Log.d("RishuTest", e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

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

    private fun startRecording() {
        if(!mainViewModel.isRecording) {
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(fileName)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                try {
                    prepare()
                    mainViewModel.isRecording = true
                    Toast.makeText(this@MainActivity, "Started", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    Log.e("MainActivity", "prepare() failed")
                    Toast.makeText(this@MainActivity, "Error while starting", Toast.LENGTH_SHORT).show()
                }
                start()
            }
        }else{
            Toast.makeText(this, "Already recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        if(mainViewModel.isRecording) {
            recorder?.apply {
                stop()
                release()
                mainViewModel.isRecording = false
                Toast.makeText(this@MainActivity, "Stopped", Toast.LENGTH_SHORT).show()
            }
            recorder = null
        }else {
            Toast.makeText(this, "Recorder is off", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("RestrictedApi", "VisibleForTests")
    private fun onClick(workManager: WorkManager, selectedTime: String) {
        val currTime = System.currentTimeMillis()
        val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val currTimeInHoursMin = formatter.format(currTime)
        val currTimeInMillis =
            TimeUnit.MINUTES.toMillis(
                (currTimeInHoursMin.split(":")[0].toInt() * 60 + currTimeInHoursMin.split(
                    ":"
                )[1].toInt()).toLong()
            )
        if (selectedTime.length < 4) {
            Toast.makeText(this, "Please select time", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedTimeInMillis =
            TimeUnit.MINUTES.toMillis(
                (selectedTime.split(":")[0].toInt() * 60 + selectedTime.split(
                    ":"
                )[1].toInt()).toLong()
            )
        val initialDelay = selectedTimeInMillis - currTimeInMillis
        try {
            val alarmRequest = OneTimeWorkRequestBuilder<NotifyWorker>()
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()
            workManager
                .beginUniqueWork(
                    "download",
                    ExistingWorkPolicy.KEEP,
                    alarmRequest
                )
                .enqueue()
            Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressWarnings("deprecation")
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        try {
            val manager =
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(
                Int.MAX_VALUE
            )) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    private fun checkPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    @Composable
    fun MyContent(workManager: WorkManager) {
        val mContext = LocalContext.current
        val mCalendar = Calendar.getInstance()
        val mHour = mCalendar[Calendar.HOUR_OF_DAY]
        val mMinute = mCalendar[Calendar.MINUTE]
        val mTime = remember { mutableStateOf("") }
        val mTimePickerDialog = TimePickerDialog(
            mContext,
            { _, mHour1: Int, mMinute1: Int ->
                mTime.value = "$mHour1:$mMinute1"
            }, mHour, mMinute, false
        )


        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                IconButton(
                    onClick = {
                        startRecording()
                    },
                    modifier = Modifier.background(Color.Gray)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        "play"
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                IconButton(
                    onClick = {
                        stopRecording()
                    },
                    modifier = Modifier.background(Color.Gray)
                ) {
                    Icon(
                        Icons.Filled.Stop,
                        "Pause",
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { mTimePickerDialog.show() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0XFF0F9D58))
            ) {
                Text(text = "Open Time Picker", color = Color.White)
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Selected Time: ${mTime.value}", fontSize = 16.sp)
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = {
//                    onClick(workManager, mTime.value)
//                    setupPlayer()
                    sendToSheets()
                }
            ) {
                Text(text = "Start")
            }
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = {
                    stopService(Intent(applicationContext, AskAudioService::class.java))
                },
                enabled = isMyServiceRunning(AskAudioService::class.java)
            ) {
                Text(text = "Stop Service")
            }
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

