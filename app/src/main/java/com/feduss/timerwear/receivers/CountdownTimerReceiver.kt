package com.feduss.timerwear.receivers

import android.content.Context
import android.content.Intent
import android.util.Log
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.utils.NotificationUtils
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import com.feduss.timerwear.utils.TimerUtils
import com.feduss.timerwear.view.MainActivity

class CountdownTimerReceiver: BaseBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val workoutTypeRaw = PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.WorkoutType.value
        )

        val workoutType = WorkoutType.fromString(workoutTypeRaw) ?: return

        val currentWorkoutModel = TimerUtils.getCustomWorkoutModels(
            context = context,
            workoutType = workoutType
        )?.first()

        val timer = currentWorkoutModel?.timers?.first()

        Log.e("TEST123 --> ", "currentExpireTimer: countdown")

        timer?.let {
            PrefsUtils.setNextTimerInPrefs(
                context = context,
                newCurrentTimerIndex = 0,
                newCurrentRepetition = 0,
                newCurrentTimerSecondsRemaining = timer.duration.toSeconds()
            )

            val isAmbientModeEnabled = PrefsUtils.getAmbientModeState(context)

            vibrateAndPlaySound(context, timer)
            scheduleNextBackgroundAlert(context)
            NotificationUtils.updateOngoingNotification(
                context = context,
                name = timer.name,
                timerSecondsRemaining = timer.duration.toSeconds().toDouble()
            )

            if (isAmbientModeEnabled) {
                val appIntent = Intent(context, MainActivity::class.java)
                appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(appIntent)
            }

        }
    }


}