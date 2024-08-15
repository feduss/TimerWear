package com.feduss.timerwear.uistate.uistate.add_custom_timer

import com.feduss.timerwear.uistate.uistate.GenericButtonCardUiState
import com.feduss.timerwear.uistate.uistate.GenericTextInputUiState
import com.feduss.timerwear.uistate.uistate.TimerPickerInputUiState

data class AddCustomWorkoutUiState(
    val titleUiState: GenericTextInputUiState,
    val repetitionsUiState: GenericTextInputUiState,
    val intermediumRestUiState: TimerPickerInputUiState? = null,
    val intermediumRestFrequencyUiState: GenericTextInputUiState? = null,
    val customTimerUiStates: List<CustomTimerUiState>? = null,
    val addTimerButtonUiState: GenericButtonCardUiState? = null,
    val addWorkoutConfirmButtonUiState: GenericButtonCardUiState,
    val bottomValidationErrors: List<String>
)