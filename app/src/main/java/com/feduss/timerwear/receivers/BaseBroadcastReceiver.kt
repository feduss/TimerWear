package com.feduss.timerwear.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.wear.ongoing.OngoingActivity
import com.feduss.timerwear.entity.CustomTimerModel
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.entity.enums.Consts
import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.uistate.extension.getRawMp3
import com.feduss.timerwear.utils.AlarmUtils
import com.feduss.timerwear.utils.NotificationUtils
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils

open class BaseBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {}

    fun vibrateAndPlaySound(
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

    fun scheduleNextBackgroundAlert(context: Context) {
        AlarmUtils.setBackgroundAlert(
            context = context,
            timerReceiverClass = ActiveTimerReceiver::class.java,
            backgroundAlarmType = BackgroundAlarmType.ActiveTimer
        )
    }
}