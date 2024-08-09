

package com.feduss.timerwear.view

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import com.feduss.timerwear.R
import com.feduss.timerwear.entity.enums.Consts
import com.feduss.timerwear.uistate.receivers.TimerReceiver
import com.feduss.timerwear.utils.AlarmUtils
import com.feduss.timerwear.utils.NotificationUtils
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val timerReceiver = TimerReceiver()

    private lateinit var notificationManager: NotificationManager

    private lateinit var context: Context

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        context = this

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val filter = IntentFilter()
        registerReceiver(timerReceiver, filter)

        if(intent.getBooleanExtra(Consts.FromOngoingNotification.value, false)) {
            NotificationUtils.restoreTimerSecondsFromOngoingNotification(context)
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

        val isTimerActive = PrefsUtils.isTimerActive(context)

        if (isTimerActive) {
            AlarmUtils.setBackgroundAlert(
                context,
                TimerReceiver::class.java
            )
            NotificationUtils.setOngoingNotification(
                context,
                iconId = R.mipmap.ic_app
            )
        }
    }

    override fun onResume() {
        super.onResume()
        AlarmUtils.removeBackgroundAlert(
            context,
            TimerReceiver::class.java
        )
        NotificationUtils.removeOngoingNotification(context)
    }

    override fun onDestroy() {
        unregisterReceiver(timerReceiver)
        super.onDestroy()
    }
}