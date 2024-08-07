package com.feduss.timerwear.utils

sealed class PrefParam(val value: String) {
    data object CustomWorkoutList: PrefParam(value = "CustomWorkoutList")
    data object BalloonDismissed: PrefParam(value = "BalloonDismissed")
}