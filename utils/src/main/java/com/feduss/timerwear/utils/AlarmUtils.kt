package com.feduss.timerwear.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.feduss.timerwear.entity.enums.Consts
import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.entity.enums.VibrationType

class AlarmUtils {

    companion object {

        fun<T> setBackgroundAlert(context: Context, timerReceiverClass: Class<T>) {
            //removeBackgroundAlert(context, timerReceiverClass)

            val timerSecondsRemaining = PrefsUtils.getStringPref(
                context,
                PrefParam.CurrentTimerSecondsRemaining.value
            )?.toLong() ?: 0L
            val currentMillisecondsTimestamp = System.currentTimeMillis()

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val broadcastReceiverIntent = Intent(context, timerReceiverClass)
            broadcastReceiverIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

            //Intent called when the timer ended
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                Consts.AlarmEnd.value.toInt(),
                broadcastReceiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmTime = (timerSecondsRemaining * 1000L) + currentMillisecondsTimestamp

            val alarmClockInfo = AlarmManager.AlarmClockInfo(alarmTime, null)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

            PrefsUtils.setStringPref(
                context,
                PrefParam.AlarmSetTime.value,
                (alarmTime / 1000).toString()
            )
        }

        fun<T> removeBackgroundAlert(context: Context, timerReceiverClass: Class<T>) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val broadcastReceiverIntent = Intent(context, timerReceiverClass)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                Consts.AlarmEnd.value.toInt(),
                broadcastReceiverIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            PrefsUtils.setStringPref(
                context,
                PrefParam.AlarmSetTime.value,
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

        /*fun sound(context: Context) {
            val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val mp: MediaPlayer? = MediaPlayer.create(context, alarmSound)

            if (mp != null) {
                mp.start()
                Handler(Looper.getMainLooper()).postDelayed({
                    mp.release()
                }, 5000)
            }
        }*/
    }
}