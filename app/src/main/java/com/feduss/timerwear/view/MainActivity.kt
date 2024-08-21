

package com.feduss.timerwear.view

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val filter = IntentFilter()
        registerReceiver(timerReceiver, filter)

        if(intent.getBooleanExtra(Consts.FromOngoingNotification.value, false)) {
            NotificationUtils.restoreTimerSecondsFromOngoingNotification(this)
        }

        setContent {
            MaterialTheme {
                MainNavView(
                    mainActivity = this,
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

    override fun onPause() {
        super.onPause()

        val isTimerActive = PrefsUtils.isTimerActive(this)

        if (isTimerActive) {
            restoreScreenTimeout(this)
            AlarmUtils.setBackgroundAlert(
                this,
                TimerReceiver::class.java
            )
            NotificationUtils.setOngoingNotification(
                this,
                iconId = R.drawable.ic_app
            )
        }
    }

    override fun onResume() {
        super.onResume()

        val isTimerActive = PrefsUtils.isTimerActive(this)
        val keepScreenOn = PrefsUtils.getKeepScreenOnPref(this)

        if (isTimerActive && keepScreenOn) {
            keepScreenOn(this)
        }

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

    private fun keepScreenOn(activity: MainActivity) {
        val window = activity.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun restoreScreenTimeout(activity: MainActivity) {
        val window = activity.window
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}