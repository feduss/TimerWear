package com.feduss.timerwear.uistate.uistate.timer

import androidx.compose.ui.graphics.Color
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.AlertDialogType

data class TimerUiState(
    val customWorkoutModel: CustomWorkoutModel,
    val currentTimerId: Int = 0,
    val currentTimerName: String,
    val currentRepetition: Int = 0,
    val isTimerActive: Boolean,
    val maxTimerSeconds: Int,
    val timerSecondsRemaining: Int,
    val circularSliderColor: Color,
    val circularSliderProgress: Double = 0.0,
    val middleTimerStatusValueText: String,
    val bottomLeftButtonId: Int,
    val bottomLeftButtonDescription: String,
    val bottomLeftButtonColor: Color = Color.Black,
    val bottomRightButtonId: Int,
    val bottomRightButtonDescription: String,
    val bottomRightButtonColor: Color = Color.Black,
    val isAlertDialogVisible: Boolean,
    val alertDialogType: AlertDialogType? = null,
    val alertDialogSkipTitleId: Int,
    val alertDialogSkipPositiveIconId: Int,
    val alertDialogSkipPositiveIconDescription: String,
    val alertDialogSkipNegativeIconId: Int,
    val alertDialogSkipNegativeIconDescription: String,
    val alertDialogStopTitleId: Int,
    val alertDialogStopPositiveIconId: Int,
    val alertDialogStopPositiveIconDescription: String,
    val alertDialogStopNegativeIconId: Int,
    val alertDialogStopNegativeIconDescription: String,
    val repetitionTitleId: Int

)
