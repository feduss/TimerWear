package com.feduss.timerwear.uistate.uistate.picker

import com.feduss.timerwear.entity.TimerPickerModel

data class TimerPickerUiState(
    val titleId: Int = -1,
    val initialMinutesValue: Int? = 0,
    val maxMinutesOptions: Int? = 60,
    val initialSecondsValue: Int = 0,
    val maxSecondsOptions: Int = 60,
    val onValueChanged: (TimerPickerModel) -> Unit
)
