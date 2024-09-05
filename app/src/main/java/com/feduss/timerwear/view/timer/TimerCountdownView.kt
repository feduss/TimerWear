package com.feduss.timerwear.view.timer

import android.content.Context
import android.os.CountDownTimer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.Text
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.uistate.uistate.timer.TimerCountdownUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.utils.AlarmUtils
import kotlin.math.ceil

@Composable
fun TimerCountdownView(
    timerCountdownUiState: TimerCountdownUiState,
    viewModel: TimerViewModel,
    navController: NavController,
    context: Context,
    onTimerSet: (String) -> Unit
) {

    onTimerSet(timerCountdownUiState.workoutName)
    val preCountdownText by remember {
        mutableStateOf(context.getString(timerCountdownUiState.preCountdownTextId))
    }

    val postCountdownText by remember {
        mutableStateOf(context.getString(timerCountdownUiState.postCountdownTextId))
    }

    var centeredText by remember {
        mutableStateOf(preCountdownText)
    }

    val postCountDownTimer by remember(Unit) {
        mutableStateOf(object : CountDownTimer(
            timerCountdownUiState.postCountdownSeconds * 1000L, 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                viewModel.countdownFinished()
            }
        })
    }

    val countDownTimer by remember(Unit) {
        mutableStateOf(object : CountDownTimer(
            timerCountdownUiState.countdown * 1000L, 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val currentTimerSecondsRemaining = ceil((millisUntilFinished).toDouble() / 1000).toInt()

                centeredText = currentTimerSecondsRemaining.toString()

                vibrate(
                    context = context,
                    vibrationType = VibrationType.SingleShort
                )
            }

            override fun onFinish() {
                vibrate(
                    context = context,
                    vibrationType = VibrationType.SingleLong
                )
                centeredText = postCountdownText
                postCountDownTimer.start()
            }
        })
    }

    val preCountDownTimer by remember(Unit) {
        mutableStateOf(object : CountDownTimer(
            timerCountdownUiState.preCountdownDuration * 1000L, 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                centeredText = timerCountdownUiState.postCountdownSeconds.toString()
                countDownTimer.start()
            }
        }.start())
    }

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()

    //Back button
    BackHandler {
        cancelCountdownTimers(
            timers = listOf(preCountDownTimer, countDownTimer, postCountDownTimer),
            navigationController = navController
        )
    }

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        onDismissed = {
            cancelCountdownTimers(
                timers = listOf(preCountDownTimer, countDownTimer, postCountDownTimer),
                navigationController = navController
            )
        }, //swipe to dismiss gesture
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = centeredText,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = TextUnit(40f, TextUnitType.Sp),
                lineHeight = TextUnit(40f, TextUnitType.Sp)
            )
        }
    }
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

private fun cancelCountdownTimers(timers: List<CountDownTimer?>, navigationController: NavController) {
    timers.forEach { it?.cancel() }
    navigationController.popBackStack()
}
