package com.example.composeexperiment

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.composeexperiment.ui.theme.ComposeExperimentTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val workManager = WorkManager.getInstance(applicationContext)

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
                    onClick(workManager, mTime.value)
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
        }
    }
}

