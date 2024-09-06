

package com.feduss.timerwear.view

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.receivers.TimerReceiver
import com.feduss.timerwear.utils.AlarmUtils
import com.feduss.timerwear.utils.NotificationUtils
import com.feduss.timerwear.utils.PrefsUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val timerReceiver = TimerReceiver()
    private lateinit var notificationManager: NotificationManager

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val filter = IntentFilter()
        registerReceiver(timerReceiver, filter)

        removeOngoingNotification()
        PrefsUtils.restoreActiveTimerDetails(this)

        setContent {
            MaterialTheme {
                MainNavView(
                    mainActivity = this
                )
            }
        }
    }

    // Lifecycle

    override fun onPause() {
        super.onPause()
        setOngoingNotification()
    }

    override fun onResume() {
        super.onResume()
        removeOngoingNotification()
    }

    override fun onDestroy() {
        unregisterReceiver(timerReceiver)
        super.onDestroy()
    }

    // Helpers
    private fun setOngoingNotification() {
        val isTimerActive = PrefsUtils.isTimerActive(this)
        if (isTimerActive) {
            AlarmUtils.setBackgroundAlert(
                this,
                TimerReceiver::class.java
            )

            // Create a pending intent that point to your always-on activity
            val appIntent = Intent(this, MainActivity::class.java)
            val touchIntent =
                PendingIntent.getActivity(
                    this,
                    0,
                    appIntent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

            NotificationUtils.setOngoingNotification(
                context = this,
                iconId = R.drawable.ic_app,
                touchIntent = touchIntent
            )
        }
    }

    private fun removeOngoingNotification() {

        AlarmUtils.removeBackgroundAlert(
            this,
            TimerReceiver::class.java
        )
        NotificationUtils.removeOngoingNotification(this)
    }
}