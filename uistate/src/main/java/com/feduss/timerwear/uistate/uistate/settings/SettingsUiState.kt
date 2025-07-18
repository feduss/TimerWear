package com.feduss.timerwear.uistate.uistate.settings

import com.feduss.timerwear.uistate.uistate.TimerPickerInputUiState

data class SettingsUiState(
    val headerTextId: Int,
    val isSoundEnabled: Boolean,
    val timerWarningSecondsUiState: TimerPickerInputUiState,
    val soundCheckboxTextId: Int,
    val appVersionTextId: Int,
    val feedbackTextId: Int,
    val feedbackEmail: String
)