package com.feduss.timerwear.entity

data class CustomWorkoutModel(
    val id: Int, val name: String, val timers: List<CustomTimerModel>,
    val repetition: Int, val intermediumRest: TimerPickerModel?, val intermediumRestFrequency: Int
)