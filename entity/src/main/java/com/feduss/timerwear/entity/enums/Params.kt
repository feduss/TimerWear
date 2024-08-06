package com.feduss.timerwear.entity.enums

sealed class Params(val name: String) {
    data object WorkoutId: Params("workoutId")
}
