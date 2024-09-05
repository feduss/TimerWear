package com.feduss.timerwear.view.timer

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.lifecycle.OnLifecycleEvent
import com.feduss.timerwear.uistate.extension.PurpleCustom
import com.feduss.timerwear.uistate.extension.getRawMp3
import com.feduss.timerwear.uistate.uistate.timer.TimerAlertDialogUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerCountdownUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerTYPViewUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.uistate.uistate.timer.TimerViewUiState
import com.feduss.timerwear.utils.AlarmUtils
import com.feduss.timerwear.view.ambient.AmbientTimer
import com.feduss.timerwear.view.dialog.AlertDialog
import com.google.android.horologist.compose.ambient.AmbientState
import kotlin.math.ceil


@Composable
fun TimerView(
    context: Context,
    navController: NavHostController,
    viewModel: TimerViewModel,
    onTimerSet: (String) -> Unit = {},
    ambientState: MutableState<AmbientState>,
    onSetOngoingNotification: () -> Unit,
    onRemoveOngoingNotification: () -> Unit
) {

    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()

    val userHasSkippedTimer = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(ambientState.value) {
        if (ambientState.value is AmbientState.Interactive) {
            onRemoveOngoingNotification()
        } else {
            onSetOngoingNotification()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTimerCountdownUiState(
            context = context
        )
    }

    navUiState?.let {
        viewModel.navStateFired()
        when (it) {
            TimerViewModel.NavUiState.TimerStarted -> {
                viewModel.loadTimerUiState(
                    context = context
                )
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

        if (timerCountdownUiState != null) {
            TimerCountdownView(
                timerCountdownUiState = timerCountdownUiState,
                viewModel = viewModel,
                navController = navController,
                context = context,
                onTimerSet = onTimerSet
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
                timerViewUiState,
                alertDialogUiState,
                viewModel,
                ambientState = ambientState.value,
                onTimerSet,
                userHasSkippedTimer,
                context
            )
        }
    }
}

@Composable
private fun ActiveTimerView(
    timerViewUiState: TimerViewUiState,
    alertDialogUiState: TimerAlertDialogUiState?,
    viewModel: TimerViewModel,
    ambientState: AmbientState,
    onTimerSet: (String) -> Unit,
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
                    vibrate(
                        context = context,
                        vibrationType = VibrationType.SingleLong
                    )

                    val soundType = when (timerViewUiState.timerType) {
                        TimerType.Work -> SoundType.Work
                        TimerType.Rest -> SoundType.Rest
                        TimerType.IntermediumRest -> SoundType.Rest
                    }

                    if (viewModel.isSoundEnabled) {
                        playSound(
                            context = context,
                            soundType = soundType
                        )
                    }
                }
            }

            LaunchedEffect(
                timerViewUiState.uuid,
                timerViewUiState.isTimerActive,
                timerViewUiState.resumedFromBackGround
            ) {
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

                                vibrate(
                                    context = context,
                                    vibrationType = VibrationType.SingleShort
                                )
                            }
                        }

                        override fun onFinish() {
                            vibrate(
                                context = context,
                                vibrationType = VibrationType.SingleShort
                            )
                            userHasSkippedTimer.value = false
                            viewModel.updateCircularProgressBarProgress(progress = 0.0)
                            viewModel.updateMiddleLabelValue(currentTimerSecondsRemaining = 0)
                            viewModel.userGoToNextTimer(
                                currentTimerIndex = timerViewUiState.currentTimerId,
                                currentRepetition = timerViewUiState.currentRepetition
                            )
                            Log.e("TEST123 --> ", "Timer $timer expired")
                        }

                    })
                    timer?.start()
                    Log.e("TEST123 --> ", "Timer $timer created")
                } else {
                    Log.e("TEST123 --> ", "Timer $timer canceled (isNotActive")
                    timer?.cancel()
                }
            }

            OnLifecycleEvent { owner, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_DESTROY -> {
                        Log.e("TEST123 --> ", "Timer $timer canceled (goingBackground)")
                        timer?.cancel()
                    }
                    else -> { }
                }
            }

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
                    onTimerSet = onTimerSet
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
    onTimerSet: (String) -> Unit
) {

    onTimerSet(timerViewUiState.timeText)

    CircularProgressIndicator(
        progress = { timerViewUiState.circularSliderProgress.toFloat() },
        modifier = Modifier.fillMaxSize(),
        color = timerViewUiState.circularSliderColor,
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
                    vibrate(
                        context = context,
                        vibrationType = VibrationType.SingleVeryShort
                    )
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
                    vibrate(
                        context = context,
                        vibrationType = VibrationType.SingleVeryShort
                    )
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

