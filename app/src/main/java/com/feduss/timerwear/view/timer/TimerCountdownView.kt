package com.feduss.timerwear.view.timer

import android.content.Context
import android.os.CountDownTimer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.Text
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.uistate.uistate.timer.TimerCountdownUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import com.feduss.timerwear.view.ambient.ObserveTimerAmbientMode
import com.google.android.horologist.compose.ambient.AmbientState
import kotlin.math.ceil

@Composable
fun TimerCountdownView(
    timerCountdownUiState: TimerCountdownUiState,
    viewModel: TimerViewModel,
    navController: NavController,
    context: Context,
    ambientState: AmbientState,
    onTimerSet: (String) -> Unit,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit,
    onVibrate: (VibrationType) -> Unit,
    onSetAmbientMode: () -> Unit
) {

    onTimerSet(timerCountdownUiState.workoutName)

    var countDownTimer: CountDownTimer? by remember(Unit) {
        mutableStateOf(null)
    }

    LaunchedEffect(
        timerCountdownUiState.isTimerActive
    ) {
        if (timerCountdownUiState.isTimerActive) {
            countDownTimer = object : CountDownTimer(
                timerCountdownUiState.countdown * 1000L, 1000
            ) {
                override fun onTick(millisUntilFinished: Long) {
                    val currentTimerSecondsRemaining = ceil((millisUntilFinished).toDouble() / 1000).toInt()

                    viewModel.saveCountdownData(
                        context = context,
                        currentTimerSecondsRemaining = currentTimerSecondsRemaining
                    )
                    viewModel.updateCountdown(currentTimerSecondsRemaining)

                    onVibrate(VibrationType.SingleShort)
                }

                override fun onFinish() {
                    onVibrate(VibrationType.SingleLong)
                    viewModel.countdownFinished(
                        context = context,
                        isAmbientMode = false
                    )
                }
            }
            countDownTimer?.start()
        } else {
            countDownTimer?.cancel()
        }

    }

    ObserveTimerAmbientMode(
        ambientState = ambientState,
        viewModel = viewModel,
        context = context,
        backgroundAlarmType = BackgroundAlarmType.CountdownTimer,
        onKeepScreenOn = onKeepScreenOn,
        onEnterBackgroundState = onEnterBackgroundState,
        onSetAmbientMode = onSetAmbientMode,
        timer = countDownTimer
    )

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()

    //Back button
    BackHandler {
        cancelCountdownTimersAndPop(
            context = context,
            timer = countDownTimer,
            navigationController = navController,
            onEnterBackgroundState = onEnterBackgroundState
        )
    }

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        onDismissed = {
            cancelCountdownTimersAndPop(
                context = context,
                timer = countDownTimer,
                navigationController = navController,
                onEnterBackgroundState = onEnterBackgroundState

            )
        }, //swipe to dismiss gesture
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {

            if (ambientState is AmbientState.Interactive) {
                Text(
                    text = timerCountdownUiState.countdown.toString(),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = TextUnit(40f, TextUnitType.Sp),
                    lineHeight = TextUnit(40f, TextUnitType.Sp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(timerCountdownUiState.ambientCountDownTextId),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        imageVector = ImageVector.vectorResource(id = timerCountdownUiState.ambientCountDownIconId),
                        contentDescription = timerCountdownUiState.ambientCountDownIconDescription,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private fun cancelCountdownTimersAndPop(
    context: Context,
    timer: CountDownTimer?,
    navigationController: NavController,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit
) {

    PrefsUtils.setStringPref(context, PrefParam.WorkoutType.value, null)

    PrefsUtils.setStringPref(
        context = context,
        pref = PrefParam.IsCountdownTimerActive.value,
        newValue = false.toString()
    )

    onEnterBackgroundState(BackgroundAlarmType.CountdownTimer, false)
    timer?.cancel()
    navigationController.popBackStack()
}
