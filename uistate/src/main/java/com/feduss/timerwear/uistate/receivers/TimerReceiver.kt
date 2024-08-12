package com.feduss.timerwear.uistate.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import com.feduss.timerwear.entity.enums.Consts
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel.NavUiState
import com.feduss.timerwear.utils.AlarmUtils
import com.feduss.timerwear.utils.NotificationUtils
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import com.feduss.timerwear.utils.TimerUtils

class TimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when Alarm expired in background.

        val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isInteractive) {
            val wl = powerManager.newWakeLock(
        PowerManager.ACQUIRE_CAUSES_WAKEUP  or
                    PowerManager.ON_AFTER_RELEASE  or
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                "id:wakeupscreen"
            )
            wl.acquire(1000)
        }

        val currentWorkoutId = PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.CurrentWorkoutId.value
        )?.toIntOrNull()

        val currentWorkoutModel = TimerUtils.getCustomWorkoutModels(context = context)?.first {
            it.id == currentWorkoutId
        }

        val totalTimers = currentWorkoutModel?.timers?.size
        val totalRepetitions = currentWorkoutModel?.repetition

        val currentTimerIndex = PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.CurrentTimerIndex.value
        )?.toIntOrNull()

        val currentRepetition = PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.CurrentRepetition.value
        )?.toIntOrNull()

        if (totalTimers == null || totalRepetitions == null ||
            currentTimerIndex == null || currentRepetition == null) {
            return
        }

        val newCurrentTimerIndex: Int
        val newCurrentRepetition: Int
        val pair = TimerUtils.getNextTimerIndexAndRepetition(
            totalTimers = totalTimers,
            totalRepetitions = totalRepetitions,
            currentTimerIndex = currentTimerIndex,
            currentRepetition = currentRepetition
        )
        newCurrentRepetition = pair.first
        newCurrentTimerIndex = pair.second

        //end of timer
        if (newCurrentTimerIndex == -1 && newCurrentRepetition == -1) {
            PrefsUtils.setStringPref(
                context,
                PrefParam.IsTimerActive.value,
                "false"
            )
            AlarmUtils.removeBackgroundAlert(
                context = context,
                TimerReceiver::class.java
            )
            NotificationUtils.removeOngoingNotification(context)
            return
        }
        else {
            AlarmUtils.vibrate(
                context = context,
                vibrationType = VibrationType.SingleLong
            )
            AlarmUtils.sound(context)

            if (PrefsUtils.isAppInBackground(context)) {
                //Log.e("TEST123: ", "app in background: newCurrentTimerIndex $newCurrentTimerIndex, newCurrentRepetition: $newCurrentRepetition")
                PrefsUtils.setNextTimerInPrefs(
                    context = context,
                    newCurrentTimerIndex = newCurrentTimerIndex,
                    newCurrentRepetition = newCurrentRepetition
                )
            }

            val newTimer = currentWorkoutModel.timers[newCurrentTimerIndex]
            val duration = newTimer.duration.toSeconds()
            val name = newTimer.name

            val ongoingActivity = OngoingActivity.recoverOngoingActivity(
                context,
                Consts.OngoingActivityId.value.toInt()
            )

            PrefsUtils.setStringPref(
                context = context,
                pref = PrefParam.CurrentTimerSecondsRemaining.value,
                newValue = duration.toString()
            )

            AlarmUtils.setBackgroundAlert(
                context,
                TimerReceiver::class.java
            )

            ongoingActivity?.update(
                context,
                NotificationUtils.getOngoingStatus(
                    duration.toLong(),
                    name
                )
            )
        }
    }
}