package com.feduss.timerwear.uistate.uistate.custom_timer

import androidx.compose.ui.graphics.Color

data class CustomTimerCardUiState(
    val leftIconId: Int = -1,
    val leftIconDescription: String = "",
    val leftIconTintColor: Color = Color.White,
    val timerId: Int = -1,
    val timerName: String = "",
    val timerDuration: String = ""
)