package com.feduss.timerwear.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.entity.enums.VibrationType

class AlarmUtils {

    companion object {

        fun<T> setBackgroundAlert(
            context: Context,
            timerReceiverClass: Class<T>,
            backgroundAlarmType: BackgroundAlarmType
        ) {
            //Log.e("TEST123 --> ", "BackgroundAlert set")

            val timerSecondsRemaining = PrefsUtils.getStringPref(
                context,
                PrefParam.CurrentTimerSecondsRemaining.value
            )?.toDoubleOrNull() ?: 0.0

            val currentMillisecondsTimestamp = System.currentTimeMillis()

            val alarmTime: Long = (timerSecondsRemaining * 1000).toLong() + currentMillisecondsTimestamp

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val broadcastReceiverIntent = Intent(context, timerReceiverClass)
            broadcastReceiverIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

            //Intent called when the timer ended
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                backgroundAlarmType.getRequestCode(),
                broadcastReceiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )

            PrefsUtils.setStringPref(
                context,
                PrefParam.TimerActiveAlarmSetTime.value,
                currentMillisecondsTimestamp.toString()
            )
        }

        fun<T> removeBackgroundAlert(
            context: Context,
            timerReceiverClass: Class<T>,
            backgroundAlarmType: BackgroundAlarmType
        ) {
            //Log.e("TEST123 --> ", "BackgroundAlert removed")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val broadcastReceiverIntent = Intent(context, timerReceiverClass)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                backgroundAlarmType.getRequestCode(),
                broadcastReceiverIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            PrefsUtils.setStringPref(
                context,
                PrefParam.TimerActiveAlarmSetTime.value,
                null
            )
        }

        @SuppressLint("MissingPermission")
        fun vibrate(context: Context, vibrationType: VibrationType) {
            val vibrationPattern = vibrationType.toPattern()
            val vibrator: Vibrator = getVibrator(context)
            //Log.e("TEST123: ", "has vibration? ${vibrator.hasVibrator()}")
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        vibrationPattern,
                        -1
                    )
                )
            }
        }

        private fun getVibrator(context: Context): Vibrator {
            val vibrator: Vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorService =
                        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorService.defaultVibrator
                } else {
                    context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
                }
            return vibrator
        }

        fun playSound(context: Context, soundId: Int) {
            val mediaPlayer = MediaPlayer.create(context, soundId)
            mediaPlayer.setOnPreparedListener{
                mediaPlayer.start()
            }
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
            }
        }
    }
}