package com.feduss.timerwear.uistate.uistate

import com.feduss.timerwear.entity.TimerPickerModel

data class TimerPickerInputUiState(
    val value: TimerPickerModel? = null,
    val titleId: Int = -1,
    val placeholderId: Int = -1,
    val errorTextId: Int? = null
)