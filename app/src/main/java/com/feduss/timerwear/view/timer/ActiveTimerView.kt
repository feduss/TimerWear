package com.feduss.timerwear.view.timer

import android.content.Context
import android.os.CountDownTimer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.Text
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.uistate.extension.PurpleCustom
import com.feduss.timerwear.uistate.uistate.timer.TimerAlertDialogUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.uistate.uistate.timer.TimerViewUiState
import com.feduss.timerwear.view.ambient.AmbientTimer
import com.feduss.timerwear.view.ambient.ObserveTimerAmbientMode
import com.google.android.horologist.compose.ambient.AmbientState
import kotlin.math.ceil

@Composable
fun ActiveTimerView(
    timerViewUiState: TimerViewUiState,
    alertDialogUiState: TimerAlertDialogUiState?,
    viewModel: TimerViewModel,
    ambientState: AmbientState,
    onTimerSet: (String) -> Unit,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit,
    onVibrate: (VibrationType) -> Unit,
    onPlaySound: (SoundType) -> Unit,
    onTimerFinished: () -> Unit,
    onSetAmbientMode: () -> Unit,
    userHasSkippedTimer: MutableState<Boolean>,
    context: Context
) {
    val newTimerSecondsRemaining = remember {
        mutableIntStateOf(timerViewUiState.timerSecondsRemaining)
    }

    var timer: CountDownTimer? by remember {
        mutableStateOf(null)
    }

    val isAlertDialogVisible by remember(alertDialogUiState?.isAlertDialogVisible) {
        mutableStateOf(alertDialogUiState?.isAlertDialogVisible == true)
    }

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
    val swipeBackClosure = {
        //Log.e("TEST123: ", "timer $timer resumed = $isAlertDialogVisible, stopped = ${!isAlertDialogVisible}, timerSecondsRemaining: ${newTimerSecondsRemaining.intValue}")

        viewModel.userChangedTimerState(
            timerSecondsRemaining = newTimerSecondsRemaining.intValue,
            isTimerActive = isAlertDialogVisible,
            completion = {
                viewModel.userChangeAlertDialogState(
                    isAlertDialogVisible = !isAlertDialogVisible,
                    alertDialogType = if (!isAlertDialogVisible) AlertDialogType.StopTimer else null,
                    completion = {}
                )
            }
        )
    }

    //Back button
    BackHandler {
        swipeBackClosure()
    }

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        onDismissed = swipeBackClosure, //swipe to dismiss gesture
    ) { isBackGround ->

        if (isBackGround) {
            if (alertDialogUiState != null) {
                TimerAlertDialogView(
                    alertDialogUiState = alertDialogUiState,
                    viewModel = viewModel,
                    currentTimerIndex = timerViewUiState.currentTimerId,
                    currentRepetition = timerViewUiState.currentRepetition,
                    onTimerSet = onTimerSet,
                    userGoBack = swipeBackClosure
                )
            }
        } else {

            LaunchedEffect(timerViewUiState.uuid) {
                if (!userHasSkippedTimer.value && !timerViewUiState.resumedFromBackGround) {
                    onVibrate(VibrationType.SingleLong)

                    val soundType = when (timerViewUiState.timerType) {
                        TimerType.Work -> SoundType.Work
                        TimerType.Rest -> SoundType.Rest
                        TimerType.IntermediumRest -> SoundType.Rest
                    }

                    if (viewModel.isSoundEnabled) {
                        onPlaySound(soundType)
                    }
                }
            }

            LaunchedEffect(
                timerViewUiState.uuid,
                timerViewUiState.isTimerActive,
                timerViewUiState.resumedFromBackGround
            ) {

                if (viewModel.isAmbientModeEnabled(context)) {
                    return@LaunchedEffect
                }

                if (timerViewUiState.isTimerActive) {

                    timer = (object : CountDownTimer(
                        timerViewUiState.timerSecondsRemaining * 1000L, 1000
                    ) {
                        override fun onTick(millisUntilFinished: Long) {
                            val currentTimerSecondsRemaining =
                                ceil((millisUntilFinished).toDouble() / 1000).toInt()

                            val progress = (1f - (1f - currentTimerSecondsRemaining.toFloat()
                                .div(timerViewUiState.maxTimerSeconds))).toDouble()
                            viewModel.updateCircularProgressBarProgress(progress = progress)
                            viewModel.updateMiddleLabelValue(currentTimerSecondsRemaining = currentTimerSecondsRemaining)

                            viewModel.saveCurrentTimerData(
                                context,
                                currentTimerId = timerViewUiState.currentTimerId,
                                currentRepetition = timerViewUiState.currentRepetition,
                                currentTimerSecondsRemaining = currentTimerSecondsRemaining
                            )
                            newTimerSecondsRemaining.intValue = currentTimerSecondsRemaining

                            if (currentTimerSecondsRemaining < 6) {

                                viewModel.setNextTimerTimeText(
                                    context = context
                                )

                                onVibrate(VibrationType.SingleShort)
                            }
                        }

                        override fun onFinish() {
                            userHasSkippedTimer.value = false
                            onTimerFinished()
                            //Log.e("TEST123 --> ", "Timer $timer expired")
                        }
                    })
                    timer?.start()
                    //Log.e("TEST123 --> ", "Timer $timer created")
                } else {
                    //Log.e("TEST123 --> ", "Timer $timer canceled (isNotActive")
                    timer?.cancel()
                }
            }

            ObserveTimerAmbientMode(
                ambientState = ambientState,
                viewModel = viewModel,
                context = context,
                backgroundAlarmType = BackgroundAlarmType.ActiveTimer,
                onKeepScreenOn = onKeepScreenOn,
                onEnterBackgroundState = onEnterBackgroundState,
                onSetAmbientMode = onSetAmbientMode,
                timer = timer,
                userHasSkippedTimer = userHasSkippedTimer
            )

            if (alertDialogUiState != null && isAlertDialogVisible) {
                TimerAlertDialogView(
                    alertDialogUiState = alertDialogUiState,
                    viewModel = viewModel,
                    currentTimerIndex = timerViewUiState.currentTimerId,
                    currentRepetition = timerViewUiState.currentRepetition,
                    onTimerSet = onTimerSet,
                    userGoBack = swipeBackClosure
                )
            } else if (ambientState is AmbientState.Interactive) {
                TimerViewMainContent(
                    timerViewUiState = timerViewUiState,
                    viewModel = viewModel,
                    context = context,
                    newTimerSecondsRemaining = newTimerSecondsRemaining,
                    onTimerSet = onTimerSet,
                    onVibrate = onVibrate
                )
            } else if (ambientState is AmbientState.Ambient) {
                AmbientTimer(
                    timerViewUiState,
                    ambientState = ambientState,
                    onTimerSet
                )
            }
        }
    }
}

@Composable
private fun TimerViewMainContent(
    timerViewUiState: TimerViewUiState,
    viewModel: TimerViewModel,
    context: Context,
    newTimerSecondsRemaining: MutableIntState,
    onTimerSet: (String) -> Unit,
    onVibrate: (VibrationType) -> Unit
) {

    onTimerSet(timerViewUiState.timeText)

    CircularProgressIndicator(
        progress = { timerViewUiState.circularSliderProgress.toFloat() },
        modifier = Modifier.fillMaxSize(),
        color = timerViewUiState.circularSliderColor,
        trackColor = Color.Black,
        strokeWidth = 4.dp,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = timerViewUiState.currentProgress,
            color = Color.PurpleCustom,
            textAlign = TextAlign.Center,
            fontSize = TextUnit(10f, TextUnitType.Sp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = timerViewUiState.middleTimerStatusValueText,
            color = Color.PurpleCustom,
            textAlign = TextAlign.Center,
            fontSize = TextUnit(20.0f, TextUnitType.Sp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                16.dp,
                Alignment.CenterHorizontally
            )
        ) {
            val color = Color.PurpleCustom
            CompactButton(
                modifier = Modifier
                    .width(48.dp)
                    .aspectRatio(1f)
                    .background(
                        color = color,
                        shape = CircleShape
                    ),
                colors = ButtonDefaults.primaryButtonColors(color, color),
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = timerViewUiState.bottomLeftButtonId),
                        contentDescription = timerViewUiState.bottomLeftButtonDescription,
                        tint = timerViewUiState.bottomLeftButtonColor
                    )
                },
                onClick = {
                    //Pause/Play button
                    onVibrate(VibrationType.SingleVeryShort)
                    viewModel.userChangedTimerState(
                        timerSecondsRemaining = newTimerSecondsRemaining.intValue,
                        isTimerActive = !timerViewUiState.isTimerActive,
                        completion = {}
                    )
                }
            )
            CompactButton(
                modifier = Modifier
                    .width(48.dp)
                    .aspectRatio(1f)
                    .background(
                        color = color,
                        shape = CircleShape
                    ),
                colors = ButtonDefaults.primaryButtonColors(color, color),
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = timerViewUiState.bottomRightButtonId),
                        contentDescription = timerViewUiState.bottomRightButtonDescription,
                        tint = timerViewUiState.bottomRightButtonColor
                    )
                },
                onClick = {
                    //Skip button
                    onVibrate(VibrationType.SingleVeryShort)
                    viewModel.userChangedTimerState(
                        timerSecondsRemaining = newTimerSecondsRemaining.intValue,
                        isTimerActive = false,
                        completion = {
                            viewModel.userChangeAlertDialogState(
                                isAlertDialogVisible = true,
                                alertDialogType = AlertDialogType.SkipTimer,
                                completion = {}
                            )
                        }
                    )
                }
            )
        }
    }

}