package com.feduss.timerwear.view.timer

import android.content.Context
import android.os.CountDownTimer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import java.util.Calendar


@Composable
fun TimerView(
    context: Context,
    navController: NavHostController,
    viewModel: TimerViewModel,
    onTimerSet: (String) -> Unit = {},
) {

    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUiState(context)
    }

    navUiState?.let {
        viewModel.navStateFired()
        when (it) {
            is TimerViewModel.NavUiState.GoBackToCustomWorkoutList ->
                goBackToWorkoutList(
                    context = context,
                    viewModel = viewModel,
                    navController = navController
                )

            is TimerViewModel.NavUiState.GoToNextTimer -> {
                goToNextTimer(
                    context = context,
                    viewModel = viewModel,
                    currentTimerIndex = it.currentTimerIndex,
                    currentRepetition = it.currentRepetition
                )
            }

            TimerViewModel.NavUiState.GoToEndOfWorkout -> {
                TODO()
            }
            is TimerViewModel.NavUiState.ChangeTimerState -> {
                viewModel.setTimerState(
                    timerSecondsRemaining = it.timerSecondsRemaining,
                    isTimerActive = it.isTimerActive
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

        var newTimerSecondsRemaining by remember {
            mutableIntStateOf(state.timerSecondsRemaining)
        }
        val timer by remember(state.currentTimerId, state.timerSecondsRemaining) {
            mutableStateOf(object : CountDownTimer(
                state.timerSecondsRemaining * 1000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val currentTimerSecondsRemaining = (millisUntilFinished / 1000).toInt()

                    val progress = (1f - (1f - currentTimerSecondsRemaining.toFloat().div(state.maxTimerSeconds))).toDouble()
                    viewModel.updateCircularProgressBarProgress(progress = progress)
                    viewModel.updateMiddleLabelValue(currentTimerSecondsRemaining = currentTimerSecondsRemaining)

                    viewModel.saveCurrentTimerData(
                        context,
                        currentTimerName = state.currentTimerName,
                        currentTimerId = state.currentTimerId,
                        currentRepetition = state.currentRepetition,
                        currentTimerSecondsRemaining = currentTimerSecondsRemaining
                    )
                    newTimerSecondsRemaining = currentTimerSecondsRemaining

                }
                override fun onFinish() {
                    viewModel.userGoToNextTimer(
                        currentTimerIndex = state.currentTimerId,
                        currentRepetition = state.currentRepetition
                    )
                }

            }.start())
        }

        val swipeToDismissBoxState = rememberSwipeToDismissBoxState()

        //Update the timetext label once for timer
        updateTimeText(
            secondsRemaining = state.timerSecondsRemaining,
            onTimerSet = onTimerSet
        )

        val swipeBackClosure = {
            viewModel.userChangeAlertDialogState(
                isAlertDialogVisible = !state.isAlertDialogVisible,
                alertDialogType = if (!state.isAlertDialogVisible) AlertDialogType.StopTimer else null,
                completion = {
                    viewModel.userChangedTimerState(
                        timerSecondsRemaining = newTimerSecondsRemaining,
                        isTimerActive = false
                    )
                }
            )
        }

        BackHandler {
            userSwippedBack(swipeBackClosure)
        }

        SwipeToDismissBox(
            state = swipeToDismissBoxState,
            onDismissed = {
                userSwippedBack(swipeBackClosure)
            }
        ) {
            if (state.isAlertDialogVisible) {
                onTimerSet("")
                timer.cancel()

                val resumeTimer = {
                    viewModel.userChangeAlertDialogState(
                        isAlertDialogVisible = false,
                        alertDialogType = null,
                        completion = {
                            viewModel.userChangedTimerState(
                                timerSecondsRemaining = newTimerSecondsRemaining,
                                isTimerActive = true,
                            )
                        }
                    )
                }

                when(state.alertDialogType) {
                    AlertDialogType.SkipTimer -> {
                        AlertDialog(
                            titleId = state.alertDialogSkipTitleId,
                            negativeButtonIconId = state.alertDialogSkipNegativeIconId,
                            negativeButtonIconDesc = state.alertDialogSkipNegativeIconDescription,
                            negativeButtonClicked = resumeTimer,
                            positiveButtonIconId = state.alertDialogSkipPositiveIconId,
                            positiveButtonIconDesc = state.alertDialogSkipPositiveIconDescription,
                            positiveButtonClicked = {
                                viewModel.userChangeAlertDialogState(
                                    isAlertDialogVisible = false,
                                    alertDialogType = null,
                                    completion = {
                                        viewModel.userSkipToNextTimer(
                                            currentTimerIndex = state.currentTimerId,
                                            currentRepetition = state.currentRepetition
                                        )
                                    }
                                )
                            }
                        )
                    }

                    AlertDialogType.StopTimer -> {
                        AlertDialog(
                            titleId = state.alertDialogStopTitleId,
                            negativeButtonIconId = state.alertDialogStopNegativeIconId,
                            negativeButtonIconDesc = state.alertDialogStopNegativeIconDescription,
                            negativeButtonClicked = resumeTimer,
                            positiveButtonIconId = state.alertDialogStopPositiveIconId,
                            positiveButtonIconDesc = state.alertDialogStopPositiveIconDescription,
                            positiveButtonClicked = {
                                viewModel.userChangeAlertDialogState(
                                    isAlertDialogVisible = false,
                                    alertDialogType = null,
                                    completion = {
                                        viewModel.userGoBackToWorkoutList()
                                    }
                                )
                            }
                        )
                    }

                    null -> {}
                }

            }
            else {
                CircularProgressIndicator(
                    progress = { state.circularSliderProgress.toFloat() },
                    modifier = Modifier.fillMaxSize(),
                    color = state.circularSliderColor,
                    strokeWidth = 4.dp,
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp, bottom = 32.dp, start = 8.dp, end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.currentTimerName,
                        color = Color("#E3BAFF".toColorInt()),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(
                            state.repetitionTitleId,
                            state.currentRepetition + 1,
                            state.customWorkoutModel.repetition
                        ),
                        color = Color("#E3BAFF".toColorInt()),
                        textAlign = TextAlign.Center,
                        fontSize = TextUnit(10f, TextUnitType.Sp)
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        text = state.middleTimerStatusValueText,
                        color = Color("#E3BAFF".toColorInt()),
                        textAlign = TextAlign.Center
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        val color = Color("#E3BAFF".toColorInt())
                        CompactButton(
                            modifier = Modifier
                                .width(24.dp)
                                .aspectRatio(1f)
                                .background(
                                    color = color,
                                    shape = CircleShape
                                ),
                            colors = ButtonDefaults.primaryButtonColors(color, color),
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = state.bottomLeftButtonId),
                                    contentDescription = state.bottomLeftButtonDescription,
                                    tint = state.bottomLeftButtonColor
                                )
                            },
                            onClick = {
                                //Pause/Play button
                                if (state.isTimerActive) {
                                    timer.cancel()
                                }
                                viewModel.userChangedTimerState(
                                    timerSecondsRemaining = newTimerSecondsRemaining,
                                    isTimerActive = !state.isTimerActive
                                )
                            }
                        )
                        val isLastTimer =
                            state.currentTimerId == state.customWorkoutModel.timers.last().id &&
                            state.currentRepetition == state.customWorkoutModel.repetition - 1
                        if (!isLastTimer) {
                            CompactButton(
                                modifier = Modifier
                                    .width(24.dp)
                                    .aspectRatio(1f)
                                    .background(
                                        color = color,
                                        shape = CircleShape
                                    ),
                                colors = ButtonDefaults.primaryButtonColors(color, color),
                                content = {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = state.bottomRightButtonId),
                                        contentDescription = state.bottomRightButtonDescription,
                                        tint = state.bottomRightButtonColor
                                    )
                                },
                                onClick = {
                                    //Skip button
                                    viewModel.userChangeAlertDialogState(
                                        isAlertDialogVisible = true,
                                        alertDialogType = AlertDialogType.SkipTimer,
                                        completion = {
                                            viewModel.userChangedTimerState(
                                                timerSecondsRemaining = newTimerSecondsRemaining,
                                                isTimerActive = false
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
}

@Composable
private fun AlertDialog(
    titleId: Int,
    negativeButtonIconId: Int, negativeButtonIconDesc: String, negativeButtonClicked: () -> Unit,
    positiveButtonIconId: Int, positiveButtonIconDesc: String, positiveButtonClicked: () -> Unit
) {
    Alert(
        title = {
            Text(
                text = stringResource(titleId),
                textAlign = TextAlign.Center,
                color = Color.White
            )
        },
        verticalArrangement = Arrangement.Center,
        negativeButton = {
            val color = Color.DarkGray
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
                        imageVector = ImageVector.vectorResource(
                            id = negativeButtonIconId
                        ),
                        contentDescription = negativeButtonIconDesc,
                        tint = Color.White
                    )
                },
                onClick = negativeButtonClicked
            )
        },
        positiveButton = {
            val color = Color("#E3BAFF".toColorInt())
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
                        imageVector = ImageVector.vectorResource(
                            id = positiveButtonIconId
                        ),
                        contentDescription = positiveButtonIconDesc,
                        tint = Color.Black
                    )
                },
                onClick = positiveButtonClicked
            )
        }
    )
}

private fun updateTimeText(
    secondsRemaining: Int,
    onTimerSet: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.SECOND, secondsRemaining)

    val calendarHour = calendar.get(Calendar.HOUR_OF_DAY)
    val calendarMinutes = calendar.get(Calendar.MINUTE)
    val calendarSeconds = calendar.get(Calendar.SECOND)
    val minutes = if (calendarMinutes < 10) "0$calendarMinutes" else calendarMinutes
    val seconds = if (calendarMinutes < 10) "0$calendarSeconds" else calendarSeconds
    onTimerSet("$calendarHour:$minutes:$seconds")
}

private fun userSwippedBack(swipeBackClosure: () -> Unit) {
    swipeBackClosure()
}

fun goBackToWorkoutList(context: Context, viewModel: TimerViewModel, navController: NavHostController){
    viewModel.cancelTimer(context)
    navController.popBackStack()
}

private fun goToNextTimer(
    context: Context, viewModel: TimerViewModel, currentTimerIndex: Int, currentRepetition: Int
) {
    viewModel.setNextTimer(
        context = context,
        currentTimerIndex = currentTimerIndex,
        currentRepetition = currentRepetition
    )
}