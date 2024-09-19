package com.feduss.timerwear.uistate.uistate.timer

import androidx.compose.ui.graphics.Color
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.entity.enums.TimerType
import java.util.UUID

data class TimerUiState(
    val timerCountdownUiState: TimerCountdownUiState? = null,
    val timerViewUiState: TimerViewUiState? = null,
    val alertDialogUiState: TimerAlertDialogUiState? = null,
    val timerTYPViewUiState: TimerTYPViewUiState? = null
)

data class TimerCountdownUiState(
    val workoutName: String,
    val countdown: Int,
    val ambientCountDownTextId: Int,
    val ambientCountDownIconId: Int,
    val ambientCountDownIconDescription: String,
    val isTimerActive: Boolean
)

data class TimerViewUiState(
    val customWorkoutModel: CustomWorkoutModel,
    val uuid: UUID,
    val currentTimerId: Int = 0,
    val currentRepetition: Int = 0,
    val isTimerActive: Boolean,
    val maxTimerSeconds: Int,
    val timerSecondsRemaining: Int,
    val timerType: TimerType,
    val circularSliderColor: Color,
    val circularSliderProgress: Double = 0.0,
    val currentProgress: String,
    val middleTimerStatusValueText: String,
    val bottomLeftButtonId: Int,
    val bottomLeftButtonDescription: String,
    val bottomLeftButtonColor: Color = Color.Black,
    val bottomRightButtonId: Int,
    val bottomRightButtonDescription: String,
    val bottomRightButtonColor: Color = Color.Black,
    val ambientIconId: Int,
    val ambientIconDescription: String,
    val ambientTintColor: Color = Color.White,
    val resumedFromBackGround: Boolean,
    val timeText: String
)

data class TimerAlertDialogUiState(
    val isAlertDialogVisible: Boolean = false,
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
    val alertDialogStopNegativeIconDescription: String
)

data class TimerTYPViewUiState(
    val imageId: Int,
    val imageDescription: String,
    val titleId: Int
)
