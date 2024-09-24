

package com.feduss.timerwear.view

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.receivers.ActiveTimerReceiver
import com.feduss.timerwear.receivers.CountdownTimerReceiver
import com.feduss.timerwear.utils.AlarmUtils
import com.feduss.timerwear.utils.NotificationUtils
import com.feduss.timerwear.utils.PrefsUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val countdownTimerReceiver = CountdownTimerReceiver()
    private val activeTimerReceiver = ActiveTimerReceiver()
    private lateinit var notificationManager: NotificationManager

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val filter = IntentFilter()
        registerReceiver(countdownTimerReceiver, filter)
        registerReceiver(activeTimerReceiver, filter)

        PrefsUtils.restoreActiveTimerDetails(this)

        setContent {
            MaterialTheme {
                MainNavView(
                    mainActivity = this,
                    onEnterBackgroundState = { backgroundAlarmType, isActive ->
                        if (isActive) {
                            setOngoingNotification(backgroundAlarmType)
                        } else {
                            removeOngoingNotification(backgroundAlarmType)
                        }
                    },
                    onKeepScreenOn = {
                        if (it) {
                            keepScreenOn(this)
                        } else {
                            restoreScreenTimeout(this)
                        }
                    }
                )
            }
        }
    }

    // Lifecycle

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        unregisterReceiver(countdownTimerReceiver)
        unregisterReceiver(activeTimerReceiver)
        super.onDestroy()
    }

    // Helpers
    private fun setOngoingNotification(backgroundAlarmType: BackgroundAlarmType) {
        val isCountdownTimerActive = PrefsUtils.isCountdownTimerActive(this)
        val isTimerActive = PrefsUtils.isTimerActive(this)
        if (isCountdownTimerActive || isTimerActive) {
            val timerReceiverClass = when(backgroundAlarmType) {
                BackgroundAlarmType.CountdownTimer -> CountdownTimerReceiver::class.java
                BackgroundAlarmType.ActiveTimer -> ActiveTimerReceiver::class.java
            }

            AlarmUtils.setBackgroundAlert(
                context = this,
                timerReceiverClass = timerReceiverClass,
                backgroundAlarmType = backgroundAlarmType
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

    private fun removeOngoingNotification(backgroundAlarmType: BackgroundAlarmType) {

        val timerReceiverClass = when(backgroundAlarmType) {
            BackgroundAlarmType.CountdownTimer -> CountdownTimerReceiver::class.java
            BackgroundAlarmType.ActiveTimer -> ActiveTimerReceiver::class.java
        }

        AlarmUtils.removeBackgroundAlert(
            context = this,
            timerReceiverClass = timerReceiverClass,
            backgroundAlarmType = backgroundAlarmType
        )
        NotificationUtils.removeOngoingNotification(this)
    }

    private fun keepScreenOn(activity: MainActivity) {
        val window = activity.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun restoreScreenTimeout(activity: MainActivity) {
        val window = activity.window
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}