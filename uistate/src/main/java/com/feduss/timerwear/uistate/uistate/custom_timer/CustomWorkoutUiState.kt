package com.feduss.timerwear.uistate.uistate.custom_timer

import com.feduss.timerwear.uistate.uistate.GenericButtonCardUiState

data class CustomWorkoutUiState(
    val customWorkouts: List<CustomWorkoutCardUiState>?,
    val addCustomWorkoutButton: GenericButtonCardUiState
)