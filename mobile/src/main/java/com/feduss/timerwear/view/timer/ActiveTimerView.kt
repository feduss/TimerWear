package com.feduss.timerwear.view.timer

import android.content.Context
import android.os.CountDownTimer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.lifecycle.ObserveTimerBackgroundMode
import com.feduss.timerwear.uistate.extension.PurpleCustom
import com.feduss.timerwear.uistate.uistate.timer.TimerAlertDialogUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.uistate.uistate.timer.TimerViewUiState

@Composable
fun ActiveTimerView(
    timerViewUiState: TimerViewUiState,
    alertDialogUiState: TimerAlertDialogUiState?,
    viewModel: TimerViewModel,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit,
    onVibrate: (VibrationType) -> Unit,
    onPlaySound: (SoundType) -> Unit,
    onActiveTimerFinished: () -> Unit,
    userHasSkippedTimer: MutableState<Boolean>,
    context: Context
) {

    val newTimerSecondsRemaining = remember {
        mutableDoubleStateOf(timerViewUiState.timerSecondsRemaining)
    }

    var timer: CountDownTimer? by remember {
        mutableStateOf(null)
    }

    val isAlertDialogVisible by remember(alertDialogUiState?.isAlertDialogVisible) {
        mutableStateOf(alertDialogUiState?.isAlertDialogVisible == true)
    }

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
    val swipeBackClosure = {
        viewModel.userChangedTimerState(
            timerSecondsRemaining = newTimerSecondsRemaining.doubleValue,
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
        backgroundContent = {
            /*if (alertDialogUiState != null) {
                TimerAlertDialogView(
                    alertDialogUiState = alertDialogUiState,
                    viewModel = viewModel,
                    currentTimerIndex = timerViewUiState.currentTimerId,
                    currentRepetition = timerViewUiState.currentRepetition,
                    userGoBack = swipeBackClosure
                )
            }*/
        },
        onDismiss = {
            swipeBackClosure() //swipe to dismiss gesture
        },
    ) {

        LaunchedEffect(
            timerViewUiState.uuid,
            timerViewUiState.isTimerActive,
            timerViewUiState.resumedFromBackGround
        ) {
            if (timerViewUiState.isTimerActive) {
                val countDownDurationMillis = (timerViewUiState.timerSecondsRemaining * 1000).toLong()
                var elapsedSeconds = 0f
                var prevMillisUntilFinished = 0L
                timer = (object : CountDownTimer(countDownDurationMillis, 10) {
                    override fun onTick(millisUntilFinished: Long) {
                        var currentTimerSecondsRemaining = millisUntilFinished.toDouble() / 1000
                        if (currentTimerSecondsRemaining == timerViewUiState.timerSecondsRemaining) {
                            currentTimerSecondsRemaining -= 0.1
                        }

                        val progress = (1f - (1f - currentTimerSecondsRemaining
                            .div(timerViewUiState.maxTimerSeconds)))
                        viewModel.updateCircularProgressBarProgress(progress = progress)
                        viewModel.updateMiddleLabelValue(currentTimerSecondsRemaining = currentTimerSecondsRemaining)

                        viewModel.saveCurrentTimerData(
                            context,
                            currentTimerId = timerViewUiState.currentTimerId,
                            currentRepetition = timerViewUiState.currentRepetition,
                            currentTimerSecondsRemaining = currentTimerSecondsRemaining
                        )
                        newTimerSecondsRemaining.doubleValue = currentTimerSecondsRemaining

                        if (currentTimerSecondsRemaining < viewModel.timerWarningSeconds) {

                            viewModel.setNextTimerTimeText(
                                context = context
                            )

                            //elapsedSeconds is not ensure that the vibration is played every second
                            if (elapsedSeconds > 1.0) {
                                elapsedSeconds = 0f
                                onVibrate(VibrationType.SingleShort)
                            } else if (prevMillisUntilFinished != 0L) {
                                elapsedSeconds += (prevMillisUntilFinished - millisUntilFinished).div(1000f)
                                prevMillisUntilFinished = millisUntilFinished
                            } else {
                                prevMillisUntilFinished = millisUntilFinished
                                onVibrate(VibrationType.SingleShort)
                            }
                        }
                    }

                    override fun onFinish() {
                        if (!viewModel.endOfWorkout) {
                            onVibrate(VibrationType.DoubleShort)
                            if (viewModel.isSoundEnabled) {
                                val soundType = when (timerViewUiState.timerType) {
                                    TimerType.Work -> SoundType.Work
                                    TimerType.Rest -> SoundType.Rest
                                    TimerType.IntermediumRest -> SoundType.Rest
                                }
                                onPlaySound(soundType)
                            }
                        } else {
                            onVibrate(VibrationType.SingleLong)
                        }
                        userHasSkippedTimer.value = false
                        onActiveTimerFinished()
                    }
                })
                timer?.start()
            } else {
                timer?.cancel()
            }
        }

        ObserveTimerBackgroundMode(
            viewModel = viewModel,
            context = context,
            onKeepScreenOn = onKeepScreenOn,
            onEnterBackgroundState = onEnterBackgroundState,
            timer = timer,
            userHasSkippedTimer = userHasSkippedTimer
        )

        if (alertDialogUiState != null && isAlertDialogVisible) {
            TimerAlertDialogView(
                alertDialogUiState = alertDialogUiState,
                viewModel = viewModel,
                currentTimerIndex = timerViewUiState.currentTimerId,
                currentRepetition = timerViewUiState.currentRepetition,
                userGoBack = swipeBackClosure
            )
        }

        TimerViewMainContent(
            timerViewUiState = timerViewUiState,
            viewModel = viewModel,
            context = context,
            newTimerSecondsRemaining = newTimerSecondsRemaining,
            onVibrate = onVibrate
        )
    }
}

@Composable
private fun TimerViewMainContent(
    timerViewUiState: TimerViewUiState,
    viewModel: TimerViewModel,
    context: Context,
    newTimerSecondsRemaining: MutableDoubleState,
    onVibrate: (VibrationType) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timerViewUiState.timeText,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                fontSize = TextUnit(15f, TextUnitType.Sp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { timerViewUiState.circularSliderProgress.toFloat() },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onBackground,
                    strokeWidth = 8.dp,
                )
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(vertical = 32.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = timerViewUiState.currentProgress,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        fontSize = TextUnit(15f, TextUnitType.Sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = timerViewUiState.middleTimerStatusValueText,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        fontSize = TextUnit(20.0f, TextUnitType.Sp),
                        fontFamily = FontFamily(Typeface(android.graphics.Typeface.MONOSPACE))
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
                        IconButton(
                            modifier = Modifier
                                .width(48.dp)
                                .aspectRatio(1f)
                                .background(
                                    color = color,
                                    shape = CircleShape
                                ),
                            colors = IconButtonDefaults.iconButtonColors(color, color),
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
                                    timerSecondsRemaining = newTimerSecondsRemaining.doubleValue,
                                    isTimerActive = !timerViewUiState.isTimerActive,
                                    completion = {}
                                )
                            }
                        )
                        IconButton(
                            modifier = Modifier
                                .width(48.dp)
                                .aspectRatio(1f)
                                .background(
                                    color = color,
                                    shape = CircleShape
                                ),
                            colors = IconButtonDefaults.iconButtonColors(color, color),
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
                                    timerSecondsRemaining = newTimerSecondsRemaining.doubleValue,
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
        }
    }
}