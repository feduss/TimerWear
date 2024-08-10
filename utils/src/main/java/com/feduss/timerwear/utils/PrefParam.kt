package com.feduss.timerwear.utils

sealed class PrefParam(val value: String) {
    data object CustomWorkoutList: PrefParam(value = "CustomWorkoutList")
    data object BalloonDismissed: PrefParam(value = "BalloonDismissed")
    data object CurrentTimerIndex: PrefParam(value = "CurrentTimerIndex")
    data object CurrentWorkoutId: PrefParam(value = "CurrentWorkoutId")
    data object CurrentTimerName: PrefParam(value = "CurrentTimerName")
    data object CurrentRepetition: PrefParam(value = "CurrentRepetition")
    data object CurrentTimerSecondsRemaining: PrefParam(value = "CurrentTimerSecondsRemaining")
    data object IsTimerActive: PrefParam(value = "IsTimerActive")
    data object TimerType: PrefParam(value = "TimerType")
    data object KeepScreenOn: PrefParam(value = "KeepScreenOn")
    data object AlarmSetTime: PrefParam(value = "AlarmSetTime")
    data object OngoingNotificationStartTime: PrefParam(value = "OngoingNotificationStartTime")
}