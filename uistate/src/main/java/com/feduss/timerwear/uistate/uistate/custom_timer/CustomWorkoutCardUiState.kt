package com.feduss.timerwear.uistate.uistate.custom_timer

import androidx.compose.ui.graphics.Color

data class CustomWorkoutCardUiState(
    val leftIconId: Int = -1,
    val leftIconDescription: String = "",
    val leftIconTintColor: Color = Color.White,
    val id: Int = -1,
    val name: String = "",
    val duration: String = "",
    val isBalloonEnabled: Boolean,
    var onCardClicked: () -> Unit = {},
    var onBalloonDismissed: () -> Unit = {},
    var onEditWorkoutButtonClicked: () -> Unit = {},
    var onDeleteWorkoutButtonClicked: () -> Unit = {}
)