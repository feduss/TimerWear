package com.feduss.timerwear.view.timer

import android.content.Context
import android.os.CountDownTimer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.lifecycle.OnLifecycleEvent
import com.feduss.timerwear.uistate.extension.getRawMp3
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.uistate.uistate.timer.TimerViewUiState
import com.feduss.timerwear.utils.AlarmUtils
import com.feduss.timerwear.utils.AmbientUtils
import com.google.android.horologist.compose.ambient.AmbientState


@Composable
fun TimerView(
    context: Context,
    navController: NavHostController,
    viewModel: TimerViewModel,
    onTimerSet: (String) -> Unit = {},
    ambientState: MutableState<AmbientState>,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit
) {

    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()

    val userHasSkippedTimer = remember {
        mutableStateOf(false)
    }

    // Initial uistate loading
    LaunchedEffect(Unit) {
        viewModel.loadTimerCountdownUiState(
            context = context
        )
    }

    navUiState?.let {
        viewModel.navStateFired()
        when (it) {
            is TimerViewModel.NavUiState.TimerStarted -> {
                viewModel.loadTimerUiState(
                    context = context
                )

                if (it.isAmbientMode) {
                    setAmbientMode(
                        viewModel,
                        context,
                        backgroundAlarmType = BackgroundAlarmType.ActiveTimer,
                        onKeepScreenOn,
                        onEnterBackgroundState
                    )
                }
            }
            is TimerViewModel.NavUiState.GoBackToCustomWorkoutList -> {
                goBackToWorkoutList(
                    context = context,
                    viewModel = viewModel,
                    navController = navController,
                    onTimerSet = onTimerSet
                )
            }

            is TimerViewModel.NavUiState.GoToNextTimer -> {
                viewModel.setNextTimer(
                    context = context,
                    currentTimerIndex = it.currentTimerIndex,
                    currentRepetition = it.currentRepetition
                )

                if (it.isAmbientMode) {
                    setAmbientMode(
                        viewModel,
                        context,
                        backgroundAlarmType = BackgroundAlarmType.ActiveTimer,
                        onKeepScreenOn,
                        onEnterBackgroundState
                    )
                }
            }

            is TimerViewModel.NavUiState.SkipToNextTimer -> {
                userHasSkippedTimer.value = true
                viewModel.setNextTimer(
                    context = context,
                    currentTimerIndex = it.currentTimerIndex,
                    currentRepetition = it.currentRepetition
                )
            }

            TimerViewModel.NavUiState.GoToEndOfWorkout -> {
                viewModel.setTYPState(
                    context = context
                )
            }
            is TimerViewModel.NavUiState.ChangeTimerState -> {
                viewModel.setTimerState(
                    context = context,
                    timerSecondsRemaining = it.timerSecondsRemaining,
                    isTimerActive = it.isTimerActive,
                    completion = it.completion
                )
            }

            is TimerViewModel.NavUiState.ChangeAlertDialogState -> {
                viewModel.changeAlertDialogState(
                    isAlertDialogVisible = it.isAlertDialogVisible,
                    alertDialogType = it.alertDialogType,
                    completion = it.completion
                )
            }
        }
    }

    dataUiState?.let { state ->

        val timerCountdownUiState = state.timerCountdownUiState
        val alertDialogUiState = state.alertDialogUiState
        val timerViewUiState = state.timerViewUiState
        val timerTYPViewUiState = state.timerTYPViewUiState

        OnLifecycleEvent { _, event ->

            val backgroundAlarmType = if (timerCountdownUiState != null) {
                BackgroundAlarmType.CountdownTimer
            } else if (timerViewUiState != null) {
                BackgroundAlarmType.ActiveTimer
            } else {
                null
            }

            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    onKeepScreenOn(!AmbientUtils.isAmbientDisplayOn(context))

                    //When the user resume the app without ambient mode, remove background alert and ongoing notification
                    if (ambientState.value is AmbientState.Interactive) {
                        backgroundAlarmType?.let { onEnterBackgroundState(it, false) }
                    }
                    //When the app is in ambient mode and the background alert expired
                    //and the activity is still valid
                    else {
                        timerCountdownUiState?.let {
                            viewModel.countdownFinished(
                                context = context,
                                isAmbientMode = true
                            )
                        }

                        timerViewUiState?.let {
                            onTimerFinished(
                                context = context,
                                viewModel = viewModel,
                                timerViewUiState = it,
                                isAmbientMode = true
                            )
                        }
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    onKeepScreenOn(false)

                    // When the user put the app in background without ambient mode
                    // enable a background alert and an ongoing notification
                    if (ambientState.value is AmbientState.Interactive) {
                        backgroundAlarmType?.let { onEnterBackgroundState(it, true) }
                    }
                }
                Lifecycle.Event.ON_STOP,
                Lifecycle.Event.ON_DESTROY -> {
                    onKeepScreenOn(false)
                }
                else -> { }
            }
        }

        if (timerCountdownUiState != null) {
            TimerCountdownView(
                timerCountdownUiState = timerCountdownUiState,
                viewModel = viewModel,
                navController = navController,
                context = context,
                ambientState = ambientState.value,
                onTimerSet = onTimerSet,
                onEnterBackgroundState = onEnterBackgroundState,
                onKeepScreenOn = onKeepScreenOn,
                onVibrate = {
                    vibrate(
                        context = context,
                        vibrationType = it
                    )
                },
                onSetAmbientMode = {
                    setAmbientMode(
                        viewModel,
                        context,
                        backgroundAlarmType = BackgroundAlarmType.CountdownTimer,
                        onKeepScreenOn,
                        onEnterBackgroundState
                    )
                }
            )
        } else if(timerTYPViewUiState != null) {
            TimerTYPView(
                context = context,
                viewModel = viewModel,
                timerTYPViewUiState = timerTYPViewUiState,
                onTimerSet = onTimerSet
            )
        } else if (timerViewUiState != null) {
            ActiveTimerView(
                timerViewUiState = timerViewUiState,
                alertDialogUiState = alertDialogUiState,
                viewModel = viewModel,
                ambientState = ambientState.value,
                onTimerSet = onTimerSet,
                onEnterBackgroundState = onEnterBackgroundState,
                onKeepScreenOn = onKeepScreenOn,
                onVibrate = {
                    vibrate(
                        context = context,
                        vibrationType = it
                    )
                },
                onPlaySound = {
                    playSound(
                        context = context,
                        soundType = it
                    )
                },
                onTimerFinished = {
                    onTimerFinished(
                        context = context,
                        viewModel = viewModel,
                        timerViewUiState = timerViewUiState
                    )
                },
                onSetAmbientMode = {
                    setAmbientMode(
                        viewModel,
                        context,
                        backgroundAlarmType = BackgroundAlarmType.ActiveTimer,
                        onKeepScreenOn,
                        onEnterBackgroundState
                    )
                },
                userHasSkippedTimer = userHasSkippedTimer,
                context = context
            )
        }
    }
}

private fun setAmbientMode(
    viewModel: TimerViewModel,
    context: Context,
    backgroundAlarmType: BackgroundAlarmType,
    onKeepScreenOn: (Boolean) -> Unit,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit
) {
    viewModel.userHasEnabledAmbientMode(
        context = context,
        backgroundAlarmType = backgroundAlarmType
    )
    onKeepScreenOn(false)

    // When the user enters in ambient mode
    // Enable a background alert and an ongoing notification
    onEnterBackgroundState(backgroundAlarmType, true)
}

private fun onTimerFinished(
    context: Context,
    viewModel: TimerViewModel,
    timerViewUiState: TimerViewUiState,
    isAmbientMode: Boolean = false,
) {
    vibrate(
        context = context,
        vibrationType = VibrationType.SingleShort
    )
    viewModel.updateCircularProgressBarProgress(progress = 0.0)
    viewModel.updateMiddleLabelValue(currentTimerSecondsRemaining = 0)
    viewModel.userGoToNextTimer(
        currentTimerIndex = timerViewUiState.currentTimerId,
        currentRepetition = timerViewUiState.currentRepetition,
        isAmbientMode = isAmbientMode
    )
}

fun goBackToWorkoutList(
    context: Context, viewModel: TimerViewModel, navController: NavHostController,
    onTimerSet: (String) -> Unit
){
    onTimerSet("")
    viewModel.cancelTimer(context)
    navController.popBackStack()
}

private fun vibrate(
    context: Context,
    vibrationType: VibrationType
) {
    AlarmUtils.vibrate(
        context = context,
        vibrationType = vibrationType
    )
}

private fun playSound(
    context: Context,
    soundType: SoundType
) {
    AlarmUtils.playSound(
        context = context,
        soundId = soundType.getRawMp3()
    )
}

