package com.feduss.timerwear.uistate.uistate.add_custom_timer

import androidx.compose.ui.graphics.Color
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.uistate.uistate.GenericTextInputUiState
import com.feduss.timerwear.uistate.uistate.TimerPickerInputUiState
import java.util.UUID

data class CustomTimerUiState(
    val leftIconId: Int = -1,
    val leftIconDescription: String = "",
    val leftIconTintColor: Color,
    val rightIconId: Int? = null,
    val rightIconDescription: String? = null,
    val rightIconTintColor: Color? = null,
    val isExpanded: Boolean,
    val id: Int = -1,
    val uuid: UUID = UUID.randomUUID(),
    val nameUiState: GenericTextInputUiState,
    val isNameReadOnly: Boolean,
    val durationUiState: TimerPickerInputUiState,
    val typeUiState: CustomTimerTypeUiState,
    val removeButtonTextId: Int?,
    val isValid: Boolean = nameUiState.errorTextId == null && durationUiState.errorTextId == null
)

data class CustomTimerTypeUiState(
    val value: TimerType = TimerType.Work,
    val titleId: Int = -1,
    val isReadOnly: Boolean
)