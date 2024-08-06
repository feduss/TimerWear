package com.feduss.timerwear.uistate.uistate.add_custom_timer

import com.feduss.timerwear.uistate.uistate.GenericButtonCardUiState
import com.feduss.timerwear.uistate.uistate.GenericTextInputUiState
import com.feduss.timerwear.uistate.uistate.TimerPickerInputUiState

data class AddCustomWorkoutUiState(
    val titleUiState: GenericTextInputUiState,
    val repetitionsUiState: GenericTextInputUiState,
    val intermediumRestUiState: TimerPickerInputUiState,
    val customTimerUiStates: List<CustomTimerUiState>,
    val addTimerButtonUiState: GenericButtonCardUiState,
    val addWorkoutConfirmButtonUiState: GenericButtonCardUiState,
    val bottomValidationErrors: List<String>
)