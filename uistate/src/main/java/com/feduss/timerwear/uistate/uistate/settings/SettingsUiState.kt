package com.feduss.timerwear.uistate.uistate.settings

data class SettingsUiState(
    val headerTextId: Int,
    val isSoundEnabled: Boolean,
    val soundCheckboxTextId: Int,
    val appVersionTextId: Int,
    val feedbackTextId: Int,
    val feedbackEmail: String
)