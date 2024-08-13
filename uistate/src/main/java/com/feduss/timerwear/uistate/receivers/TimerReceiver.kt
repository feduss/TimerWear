package com.feduss.timerwear.uistate.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.os.PowerManager
import androidx.wear.ongoing.OngoingActivity
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.Consts
import com.feduss.timerwear.entity.enums.CustomTimerType
import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.uistate.extension.getRawMp3
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

            AlarmUtils.playSound(
                context = context,
                soundId = SoundType.Finish.getRawMp3()
            )
            return
        }

        val newTimer = currentWorkoutModel.timers[newCurrentTimerIndex]
        val needsToShowIntermediumRest = TimerUtils.needsToDisplayIntermediumRest(
            timer = newTimer,
            repetition = newCurrentRepetition,
            totalRepetitions = totalRepetitions,
            frequency = currentWorkoutModel.intermediumRestFrequency
        )
        if (!needsToShowIntermediumRest && newTimer.type == CustomTimerType.IntermediumRest) {
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

        AlarmUtils.vibrate(
            context = context,
            vibrationType = VibrationType.SingleLong
        )

        PrefsUtils.setNextTimerInPrefs(
            context = context,
            newCurrentTimerIndex = newCurrentTimerIndex,
            newCurrentRepetition = newCurrentRepetition
        )

        val duration = newTimer.duration.toSeconds()
        val name = newTimer.name

        val soundType = when (newTimer.type) {
            CustomTimerType.Work -> SoundType.Work
            CustomTimerType.Rest -> SoundType.Rest
            CustomTimerType.IntermediumRest -> SoundType.Rest
        }

        AlarmUtils.playSound(
            context = context,
            soundId = soundType.getRawMp3()
        )

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