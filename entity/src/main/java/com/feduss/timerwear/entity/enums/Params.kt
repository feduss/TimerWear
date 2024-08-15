package com.feduss.timerwear.entity.enums

sealed class Params(val name: String) {
    data object WorkoutId: Params("workoutId")
    data object WorkoutType: Params("workoutType")
    data object CurrentTimerIndex: Params("currentTimerIndex")
    data object CurrentRepetition: Params("currentRepetition")
    data object CurrentTimerSecondsRemaining: Params("currentTimerSecondsRemaining")
}
