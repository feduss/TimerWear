package com.feduss.timerwear.uistate.uistate.ambient

import androidx.compose.ui.graphics.Color
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.entity.enums.TimerType
import java.util.UUID

data class AmbientWarningUiState(
    val titleId: Int,
    val subTitleId: Int,
    val positiveIconId: Int,
    val positiveIconDescription: String,
    val negativeIconId: Int,
    val negativeIconDescription: String,
)
