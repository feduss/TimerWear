

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
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.material.MaterialTheme
import com.feduss.timerwear.entity.enums.Consts
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

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            // ... Called when moving from interactive mode into ambient mode.

        }

        override fun onExitAmbient() {
            // ... Called when leaving ambient mode, back into interactive mode.
        }

        override fun onUpdateAmbient() {
            // ... Called by the system in order to allow the app to periodically
            // update the display while in ambient mode. Typically the system will
            // call this every 60 seconds.
        }
    }

    private val ambientObserver = AmbientLifecycleObserver(this, ambientCallback)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(ambientObserver)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val filter = IntentFilter()
        registerReceiver(timerReceiver, filter)

        if(intent.getBooleanExtra(Consts.FromOngoingNotification.value, false)) {
            NotificationUtils.restoreTimerSecondsFromOngoingNotification(this)
        }


        setContent {
            MaterialTheme {
                MainNavView(
                    mainActivity = this
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val isTimerActive = PrefsUtils.isTimerActive(this)

        if (isTimerActive) {
            AlarmUtils.setBackgroundAlert(
                this,
                TimerReceiver::class.java
            )

            // Create a pending intent that point to your always-on activity
            val appIntent = Intent(this, MainActivity::class.java)
            appIntent.putExtra(Consts.FromOngoingNotification.value, true)
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

    override fun onResume() {
        super.onResume()

        lifecycle.removeObserver(ambientObserver)
        AlarmUtils.removeBackgroundAlert(
            this,
            TimerReceiver::class.java
        )
        NotificationUtils.removeOngoingNotification(this)
    }

    override fun onDestroy() {
        unregisterReceiver(timerReceiver)
        super.onDestroy()
    }
}