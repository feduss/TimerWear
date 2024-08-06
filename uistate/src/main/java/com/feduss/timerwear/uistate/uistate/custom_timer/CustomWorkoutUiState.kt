package com.feduss.timerwear.uistate.uistate.custom_timer

import com.feduss.timerwear.uistate.uistate.GenericButtonCardUiState

data class CustomWorkoutUiState(
    val customWorkouts: List<CustomTimerCardUiState>?,
    val addCustomWorkoutButton: GenericButtonCardUiState
)