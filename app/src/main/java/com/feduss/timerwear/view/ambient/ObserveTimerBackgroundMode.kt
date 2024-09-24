package com.feduss.timerwear.view.ambient

import android.content.Context
import android.os.CountDownTimer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.lifecycle.OnLifecycleEvent
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.utils.AmbientUtils
import com.google.android.horologist.compose.ambient.AmbientState

@Composable
fun ObserveTimerBackgroundMode(
    ambientState: AmbientState,
    viewModel: TimerViewModel,
    context: Context,
    onKeepScreenOn: (Boolean) -> Unit,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit,
    onSetAmbientMode: () -> Unit,
    timer: CountDownTimer?,
    userHasSkippedTimer: MutableState<Boolean>? = null
) {
    var wasOnBackground: Boolean? by remember {
        mutableStateOf(null)
    }
    OnLifecycleEvent { _, event ->
        //This value isn't passed from method sign because of the following case:
        //The users switches to background (both ambient or app exit) from the countdown,
        //and resume the app during an active timer
        //So, the backgroundAlarmType has changed
        val backgroundAlarmType = viewModel.getBackgroundAlarmType(context)
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                onKeepScreenOn(!AmbientUtils.isAmbientDisplayOn(context))

                //When the user resume the app without ambient mode, remove background alert and ongoing notification
                if (ambientState is AmbientState.Interactive) {

                    if (wasOnBackground == true) {
                        wasOnBackground = false
                        viewModel.onExitBackgroundMode(
                            context = context,
                            backgroundAlarmType = backgroundAlarmType
                        )
                    }
                    onEnterBackgroundState(backgroundAlarmType, false)
                }
                //When the app is in ambient mode and the background alert expired
                //and the activity is still valid
                else {

                    viewModel.onRefreshAmbientMode(
                        context = context
                    )
                }
            }
            Lifecycle.Event.ON_PAUSE -> {
                onKeepScreenOn(false)

                // When the user put the app in background without ambient mode
                // enable a background alert and an ongoing notification
                if (ambientState is AmbientState.Interactive) {
                    wasOnBackground = true
                    timer?.cancel()
                    viewModel.onEnterBackgroundMode(
                        context = context,
                        backgroundAlarmType = backgroundAlarmType,
                        isAmbientMode = false
                    )
                    onEnterBackgroundState(backgroundAlarmType, true)
                }
            }
            Lifecycle.Event.ON_STOP,
            Lifecycle.Event.ON_DESTROY -> {
                onKeepScreenOn(false)
            }
            else -> { }
        }
    }

    var ambientMode: AmbientState? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(ambientState) {
        // Ambient mode var and these if checks are needed
        // to avoid to call userHasDisabledAmbientMode at first launch
        if (ambientMode == null) {
            ambientMode = ambientState

            viewModel.saveAmbientModeState(
                context = context,
                isEnabled = ambientMode is AmbientState.Ambient
            )
            return@LaunchedEffect
        }

        if (ambientMode == ambientState) {
            return@LaunchedEffect
        } else {
            ambientMode = ambientState
        }

        //This value isn't passed from method sign because of the following case:
        //The users switches to background (both ambient or app exit) from the countdown,
        //and resume the app during an active timer
        //So, the backgroundAlarmType has changed
        val backgroundAlarmType = viewModel.getBackgroundAlarmType(context)

        if (ambientMode is AmbientState.Interactive) {
            viewModel.onExitBackgroundMode(
                context = context,
                backgroundAlarmType = backgroundAlarmType
            )
            onKeepScreenOn(!AmbientUtils.isAmbientDisplayOn(context))

            // When the user exits from ambient mode
            // Remove the background alert and the ongoing notification
            onEnterBackgroundState(backgroundAlarmType, false)
        } else if (ambientMode is AmbientState.Ambient) {
            timer?.cancel()

            if (backgroundAlarmType == BackgroundAlarmType.ActiveTimer) {
                userHasSkippedTimer?.value = false
            }
            onSetAmbientMode()
        }

    }
}