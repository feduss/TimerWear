package com.feduss.timerwear.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.feduss.timerwear.entity.enums.Consts
import java.util.concurrent.TimeUnit


class NotificationUtils {

    companion object {

        fun setOngoingNotification(
            context: Context,
            touchIntent: PendingIntent,
            iconId: Int
        ) {

            val timerName = PrefsUtils.getStringPref(
                context,
                PrefParam.CurrentTimerName.value
            ) ?: "Error"

            val timerSecondsRemaining = PrefsUtils.getStringPref(
                context,
                PrefParam.CurrentTimerSecondsRemaining.value
            )?.toDoubleOrNull() ?: 0.0

            if (
                !updateOngoingNotification(
                    context = context,
                    name = timerName,
                    timerSecondsRemaining = timerSecondsRemaining
                )
            ) {

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val channel = NotificationChannel(
                    Consts.MainChannelId.value,
                    Consts.MainNotificationVisibleChannel.value,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)

                val bigTextStyle = NotificationCompat.BigTextStyle()
                    .bigText(timerName)

                val notificationBuilder = NotificationCompat.Builder(
                    context,
                    Consts.MainChannelId.value
                )
                    .setContentTitle(timerName)
                    .setSmallIcon(iconId)
                    //.setColor(Color.Red.toArgb())
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(bigTextStyle)
                    .setOngoing(true)

                val status = getOngoingStatus(
                    timerSecondsRemaining,
                    timerName
                )

                val ongoingActivity =
                    OngoingActivity.Builder(
                        context.applicationContext,
                        Consts.MainNotificationId.value.toInt(),
                        notificationBuilder
                    )
                    .setStaticIcon(iconId)
                    .setTouchIntent(touchIntent)
                    .setStatus(status)
                    .setOngoingActivityId(Consts.OngoingActivityId.value.toInt())
                    .build()

                ongoingActivity.apply(context.applicationContext)

                notificationManager.notify(
                    Consts.MainNotificationId.value.toInt(),
                    notificationBuilder.build()
                )
            }
        }

        fun updateOngoingNotification(
            context: Context,
            name: String,
            timerSecondsRemaining: Double
        ): Boolean {
            var ongoingActivity = OngoingActivity.recoverOngoingActivity(
                context,
                Consts.OngoingActivityId.value.toInt()
            )

            if (ongoingActivity != null) {

                ongoingActivity.update(
                    context,
                    NotificationUtils.getOngoingStatus(
                        timerSecondsRemaining,
                        name
                    )
                )
                return true
            } else {
                return false
            }
        }

        fun getOngoingStatus(timerSecondsRemaining: Double, timerName: String): Status {
            val runStartTime = SystemClock.elapsedRealtime() + TimeUnit.SECONDS.toMillis(timerSecondsRemaining.toLong())
            val status = Status.Builder()
                .addTemplate("#timerSeconds# (#timerName#)")
                .addPart("timerSeconds", Status.StopwatchPart(runStartTime))
                .addPart("timerName", Status.TextPart(timerName))
                .build()

            return status
        }

        fun removeOngoingNotification(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(Consts.MainNotificationId.value.toInt())
        }


    }
}