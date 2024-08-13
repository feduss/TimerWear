package com.feduss.timerwear.view.timer

import android.content.Context
import android.os.CountDownTimer
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
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
import com.feduss.timerwear.entity.enums.CustomTimerType
import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.entity.enums.VibrationType
import com.feduss.timerwear.extension.infiniteMarquee
import com.feduss.timerwear.uistate.extension.PurpleCustom
import com.feduss.timerwear.uistate.extension.getRawMp3
import com.feduss.timerwear.uistate.uistate.timer.TimerAlertDialogUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerCountdownUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerTYPViewUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.uistate.uistate.timer.TimerViewUiState
import com.feduss.timerwear.utils.AlarmUtils
import com.feduss.timerwear.utils.PrefsUtils
import com.feduss.timerwear.utils.TimerUtils
import kotlin.math.ceil


@Composable
fun TimerView(
    context: Context,
    navController: NavHostController,
    viewModel: TimerViewModel,
    onTimerSet: (String) -> Unit = {},
    onKeepScreenOn: (Boolean) -> Unit = {}
) {

    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()

    var userHasSkippedTimer by remember {
        mutableStateOf(false)
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
                onKeepScreenOn(true)
                viewModel.loadTimerUiState(
                    context = context
                )
            }
            is TimerViewModel.NavUiState.GoBackToCustomWorkoutList -> {
                onKeepScreenOn(false)
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
                userHasSkippedTimer = true
                viewModel.setNextTimer(
                    context = context,
                    currentTimerIndex = it.currentTimerIndex,
                    currentRepetition = it.currentRepetition
                )
            }

            TimerViewModel.NavUiState.GoToEndOfWorkout -> {
                onKeepScreenOn(false)
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
                timerTYPViewUiState = timerTYPViewUiState,
                onTimerSet = onTimerSet
            )
        } else if (timerViewUiState != null) {

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
                            currentTimerType = timerViewUiState.timerType,
                            onTimerSet = onTimerSet,
                            userGoBack = swipeBackClosure
                        )
                    }
                } else {

                    LaunchedEffect(timerViewUiState.currentTimerId) {

                        if (!userHasSkippedTimer) {
                            vibrate(
                                context = context,
                                vibrationType = VibrationType.SingleLong
                            )

                            val soundType = when(timerViewUiState.timerType) {
                                CustomTimerType.Work -> SoundType.Work
                                CustomTimerType.Rest -> SoundType.Rest
                                CustomTimerType.IntermediumRest -> SoundType.Rest
                            }

                            playSound(
                                context = context,
                                soundType = soundType
                            )
                        }
                    }

                    LaunchedEffect(
                        timerViewUiState.currentTimerId,
                        timerViewUiState.isTimerActive
                    ) {
                        if (timerViewUiState.isTimerActive) {
                            timer = (object : CountDownTimer(
                                timerViewUiState.timerSecondsRemaining * 1000L, 1000
                            ) {
                                override fun onTick(millisUntilFinished: Long) {
                                    val currentTimerSecondsRemaining = ceil((millisUntilFinished).toDouble() / 1000).toInt()

                                    val progress = (1f - (1f - currentTimerSecondsRemaining.toFloat()
                                        .div(timerViewUiState.maxTimerSeconds))).toDouble()
                                    viewModel.updateCircularProgressBarProgress(progress = progress)
                                    viewModel.updateMiddleLabelValue(currentTimerSecondsRemaining = currentTimerSecondsRemaining)

                                    viewModel.saveCurrentTimerData(
                                        context,
                                        currentTimerName = timerViewUiState.currentTimerName,
                                        currentTimerId = timerViewUiState.currentTimerId,
                                        currentRepetition = timerViewUiState.currentRepetition,
                                        currentTimerSecondsRemaining = currentTimerSecondsRemaining
                                    )
                                    newTimerSecondsRemaining.intValue = currentTimerSecondsRemaining

                                    if (currentTimerSecondsRemaining < 4) {
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
                                    userHasSkippedTimer = false
                                    viewModel.updateCircularProgressBarProgress(progress = 0.0)
                                    viewModel.updateMiddleLabelValue(currentTimerSecondsRemaining = 0)
                                    viewModel.userGoToNextTimer(
                                        currentTimerIndex = timerViewUiState.currentTimerId,
                                        currentRepetition = timerViewUiState.currentRepetition
                                    )
                                }

                            })
                            timer?.start()
                            //Log.e("TEST123: ", "LaunchedEffect: timer $timer created (started), timerSecondsRemaining: ${timerViewUiState.timerSecondsRemaining}")
                        } else {
                            //Log.e("TEST123: ", "LaunchedEffect: timer $timer destroyed (paused), timerSecondsRemaining: ${timerViewUiState.timerSecondsRemaining}")
                            timer?.cancel()
                        }
                    }

                    if (alertDialogUiState != null && isAlertDialogVisible) {
                        TimerAlertDialogView(
                            alertDialogUiState = alertDialogUiState,
                            viewModel = viewModel,
                            currentTimerIndex = timerViewUiState.currentTimerId,
                            currentRepetition = timerViewUiState.currentRepetition,
                            currentTimerType = timerViewUiState.timerType,
                            onTimerSet = onTimerSet,
                            userGoBack = swipeBackClosure
                        )
                    } else {
                        TimerViewMainContent(
                            timerViewUiState = timerViewUiState,
                            viewModel = viewModel,
                            context = context,
                            newTimerSecondsRemaining = newTimerSecondsRemaining,
                            onTimerSet = onTimerSet,
                            onKeepScreenOn = onKeepScreenOn
                        )
                    }
                }
            }
        }
    }
}

private fun vibrate(
    context: Context,
    vibrationType: VibrationType
) {
    if (!PrefsUtils.isAppInBackground(context)) {
        AlarmUtils.vibrate(
            context = context,
            vibrationType = vibrationType
        )
    }
}

private fun playSound(
    context: Context,
    soundType: SoundType
) {
    if (!PrefsUtils.isAppInBackground(context)) {
        AlarmUtils.playSound(
            context = context,
            soundId = soundType.getRawMp3()
        )
    }
}

@Composable
fun TimerTYPView(context: Context, timerTYPViewUiState: TimerTYPViewUiState, onTimerSet: (String) -> Unit) {

    onTimerSet("")

    LaunchedEffect(Unit) {
        playSound(
            context = context,
            soundType = SoundType.Finish
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.height(96.dp),
            painter = painterResource(timerTYPViewUiState.imageId),
            contentDescription = timerTYPViewUiState.imageDescription
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(id = timerTYPViewUiState.titleId),
            textAlign = TextAlign.Center,
            fontSize = TextUnit(20f, TextUnitType.Sp),
            color = Color.White
        )
    }
}

@Composable
private fun TimerCountdownView(
    timerCountdownUiState: TimerCountdownUiState,
    viewModel: TimerViewModel,
    navController: NavController,
    context: Context,
    onTimerSet: (String) -> Unit
) {

    onTimerSet("")
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

private fun cancelCountdownTimers(timers: List<CountDownTimer?>, navigationController: NavController) {
    timers.forEach { it?.cancel() }
    navigationController.popBackStack()
}

@Composable
private fun TimerAlertDialogView(
    alertDialogUiState: TimerAlertDialogUiState,
    viewModel: TimerViewModel,
    currentTimerIndex: Int,
    currentRepetition: Int,
    currentTimerType: CustomTimerType,
    onTimerSet: (String) -> Unit,
    userGoBack: () -> Unit
) {
    onTimerSet("")

    when (alertDialogUiState.alertDialogType) {
        AlertDialogType.SkipTimer -> {
            AlertDialog(
                titleId = alertDialogUiState.alertDialogSkipTitleId,
                negativeButtonIconId = alertDialogUiState.alertDialogSkipNegativeIconId,
                negativeButtonIconDesc = alertDialogUiState.alertDialogSkipNegativeIconDescription,
                negativeButtonClicked = userGoBack,
                positiveButtonIconId = alertDialogUiState.alertDialogSkipPositiveIconId,
                positiveButtonIconDesc = alertDialogUiState.alertDialogSkipPositiveIconDescription,
                positiveButtonClicked = {
                    viewModel.userChangeAlertDialogState(
                        isAlertDialogVisible = false,
                        alertDialogType = null,
                        completion = {
                            viewModel.userSkipToNextTimer(
                                currentTimerIndex = currentTimerIndex,
                                currentRepetition = currentRepetition
                            )
                        }
                    )
                }
            )
        }

        AlertDialogType.StopTimer -> {
            AlertDialog(
                titleId = alertDialogUiState.alertDialogStopTitleId,
                negativeButtonIconId = alertDialogUiState.alertDialogStopNegativeIconId,
                negativeButtonIconDesc = alertDialogUiState.alertDialogStopNegativeIconDescription,
                negativeButtonClicked = userGoBack,
                positiveButtonIconId = alertDialogUiState.alertDialogStopPositiveIconId,
                positiveButtonIconDesc = alertDialogUiState.alertDialogStopPositiveIconDescription,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerViewMainContent(
    timerViewUiState: TimerViewUiState,
    viewModel: TimerViewModel,
    context: Context,
    newTimerSecondsRemaining: MutableIntState,
    onTimerSet: (String) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit
) {

    var keepScreenOn by remember {
        mutableStateOf(timerViewUiState.isCheckboxSelected)
    }

    LaunchedEffect(keepScreenOn) {
        onKeepScreenOn(keepScreenOn)
    }

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
        val timerName = timerViewUiState.currentTimerName
        Text(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .infiniteMarquee,
            text = timerName,
            color = Color.PurpleCustom,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

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
            horizontalArrangement = Arrangement.Center
        ) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                Checkbox(
                    modifier = Modifier.scale(0.75f),
                    checked = keepScreenOn,
                    onCheckedChange = {
                        keepScreenOn = it
                        onKeepScreenOn(it)
                        viewModel.saveKeepScreenOnPref(
                            context = context,
                            keepScreenOn = it
                        )
                    }
                )
            }
            Text(
                modifier = Modifier.infiniteMarquee,
                text = stringResource(id = timerViewUiState.checkboxTextId),
                color = Color.White,
                textAlign = TextAlign.Left,
                fontSize = TextUnit(12f, TextUnitType.Sp)
            )
        }

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
                    .width(24.dp)
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
                    viewModel.userChangedTimerState(
                        timerSecondsRemaining = newTimerSecondsRemaining.intValue,
                        isTimerActive = !timerViewUiState.isTimerActive,
                        completion = {}
                    )
                }
            )
            val isLastTimer =
                timerViewUiState.currentTimerId == timerViewUiState.customWorkoutModel.timers.last().id &&
                        timerViewUiState.currentRepetition == timerViewUiState.customWorkoutModel.repetition - 1
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
                            imageVector = ImageVector.vectorResource(id = timerViewUiState.bottomRightButtonId),
                            contentDescription = timerViewUiState.bottomRightButtonDescription,
                            tint = timerViewUiState.bottomRightButtonColor
                        )
                    },
                    onClick = {
                        //Skip button
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

fun goBackToWorkoutList(
    context: Context, viewModel: TimerViewModel, navController: NavHostController,
    onTimerSet: (String) -> Unit
){
    onTimerSet("")
    viewModel.cancelTimer(context)
    navController.popBackStack()
}