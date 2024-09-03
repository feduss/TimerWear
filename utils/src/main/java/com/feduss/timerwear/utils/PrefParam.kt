package com.feduss.timerwear.utils

sealed class PrefParam(val value: String) {
    data object CustomWorkoutList: PrefParam(value = "CustomWorkoutList")
    data object EmomList: PrefParam(value = "EmomList")
    data object HiitList: PrefParam(value = "HiitList")
    data object BalloonDismissed: PrefParam(value = "BalloonDismissed")
    data object CurrentTimerIndex: PrefParam(value = "CurrentTimerIndex")
    data object CurrentWorkoutId: PrefParam(value = "CurrentWorkoutId")
    data object CurrentTimerName: PrefParam(value = "CurrentTimerName")
    data object CurrentRepetition: PrefParam(value = "CurrentRepetition")
    data object CurrentTimerSecondsRemaining: PrefParam(value = "CurrentTimerSecondsRemaining")
    data object IsTimerActive: PrefParam(value = "IsTimerActive")
    data object WorkoutType: PrefParam(value = "WorkoutType")
    data object AlarmSetTime: PrefParam(value = "AlarmSetTime")
    data object OngoingNotificationStartTime: PrefParam(value = "OngoingNotificationStartTime")
    data object IsSoundEnabled: PrefParam(value = "IsSoundEnabled")
}