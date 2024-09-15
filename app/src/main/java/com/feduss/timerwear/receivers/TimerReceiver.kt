package com.feduss.timerwear.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.wear.ongoing.OngoingActivity
import com.feduss.timerwear.entity.CustomTimerModel
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.Consts
import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.uistate.extension.getRawMp3
import com.feduss.timerwear.utils.AlarmUtils
import com.feduss.timerwear.utils.NotificationUtils
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import com.feduss.timerwear.utils.TimerUtils
import com.feduss.timerwear.view.MainActivity

class TimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        //Retrieve timer and workout details from shared prefs,
        //then handle the next timer (can be the end of the workout,
        //or the next timer)

        val currentWorkoutId = PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.CurrentWorkoutId.value
        )?.toIntOrNull()

        val workoutTypeRaw = PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.WorkoutType.value
        )

        val workoutType = WorkoutType.fromString(workoutTypeRaw) ?: return

        val currentWorkoutModel = TimerUtils.getCustomWorkoutModels(
            context = context,
            workoutType = workoutType
        )?.first {
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

        handleNextTimer(
            totalTimers,
            totalRepetitions,
            currentTimerIndex,
            currentRepetition,
            currentWorkoutModel,
            context
        )
    }

    private fun handleNextTimer(
        totalTimers: Int,
        totalRepetitions: Int,
        currentTimerIndex: Int,
        currentRepetition: Int,
        currentWorkoutModel: CustomWorkoutModel,
        context: Context
    ) {

        //Get the next timer index and the new repetition number
        val newCurrentTimerIndex: Int
        val newCurrentRepetition: Int
        val pair = TimerUtils.getNextTimerIndexAndRepetition(
            totalTimers = totalTimers,
            totalRepetitions = totalRepetitions,
            currentTimerIndex = currentTimerIndex,
            currentRepetition = currentRepetition
        )
        newCurrentTimerIndex = pair.first
        newCurrentRepetition = pair.second

        val isAmbientModeEnabled = PrefsUtils.getAmbientModeState(context)

        if (isAmbientModeEnabled) {
            val appIntent = Intent(context, MainActivity::class.java)
            appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(appIntent)
        }
        //end of timer
        else if (newCurrentTimerIndex == -1 && newCurrentRepetition == -1) {
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

            AlarmUtils.vibrate(
                context = context,
                vibrationType = VibrationType.SingleLong
            )
            if (PrefsUtils.getSoundPreference(context)) {
                AlarmUtils.playSound(
                    context = context,
                    soundId = SoundType.Finish.getRawMp3()
                )
            }
        }
        //next timer
        else {
            val newTimer = currentWorkoutModel.timers[newCurrentTimerIndex]
            val needsToShowIntermediumRest = TimerUtils.needsToDisplayIntermediumRest(
                timer = newTimer,
                repetition = newCurrentRepetition,
                totalRepetitions = totalRepetitions,
                frequency = currentWorkoutModel.intermediumRestFrequency
            )
            //If it isn't time to show intermedium rest, get the next timer and re-call handleNextTimer method
            if (!needsToShowIntermediumRest && newTimer.type == TimerType.IntermediumRest) {
                handleNextTimer(
                    totalTimers = totalTimers,
                    totalRepetitions = totalRepetitions,
                    currentTimerIndex = newCurrentTimerIndex,
                    currentRepetition = newCurrentRepetition,
                    currentWorkoutModel = currentWorkoutModel,
                    context = context
                )
                return
            }

            val duration = newTimer.duration.toSeconds()
            val name = newTimer.name

            PrefsUtils.setNextTimerInPrefs(
                context = context,
                newCurrentTimerIndex = newCurrentTimerIndex,
                newCurrentRepetition = newCurrentRepetition,
                newCurrentTimerSecondsRemaining = duration
            )

            vibrateAndPlaySound(context, newTimer)

            scheduleNextBackgroundAlert(context)
            updateOngoingNotification(context, duration, name)
        }
    }

    private fun scheduleNextBackgroundAlert(context: Context) {
        AlarmUtils.setBackgroundAlert(
            context,
            TimerReceiver::class.java
        )
    }

    private fun updateOngoingNotification(context: Context, duration: Int, name: String) {
        val ongoingActivity = OngoingActivity.recoverOngoingActivity(
            context,
            Consts.OngoingActivityId.value.toInt()
        )

        val currentMillisecondsTimestamp = System.currentTimeMillis()
        PrefsUtils.setStringPref(
            context,
            PrefParam.OngoingNotificationStartTime.value,
            toString()
        )

        ongoingActivity?.update(
            context,
            NotificationUtils.getOngoingStatus(
                currentMillisecondsTimestamp.toLong(),
                name
            )
        )
    }

    private fun vibrateAndPlaySound(
        context: Context,
        newTimer: CustomTimerModel
    ) {
        AlarmUtils.vibrate(
            context = context,
            vibrationType = VibrationType.SingleLong
        )

        val soundType = when (newTimer.type) {
            TimerType.Work -> SoundType.Work
            TimerType.Rest -> SoundType.Rest
            TimerType.IntermediumRest -> SoundType.Rest
        }

        if (PrefsUtils.getSoundPreference(context)) {
            AlarmUtils.playSound(
                context = context,
                soundId = soundType.getRawMp3()
            )
        }
    }
}