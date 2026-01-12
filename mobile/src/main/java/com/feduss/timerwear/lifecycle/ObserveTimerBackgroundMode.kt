package com.feduss.timerwear.lifecycle

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
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.utils.AmbientUtils

//TODO: replace ongoing notification with live notification
@Composable
fun ObserveTimerBackgroundMode(
    viewModel: TimerViewModel,
    context: Context,
    onKeepScreenOn: (Boolean) -> Unit,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit,
    timer: CountDownTimer?,
    userHasSkippedTimer: MutableState<Boolean>? = null
) {
    var wasOnBackground: Boolean? by remember {
        mutableStateOf(null)
    }
    OnLifecycleEvent { _, event ->
        //This value isn't passed from method sign because of the following case:
        //The users switches to background from the countdown,
        //and resume the app during an active timer
        //So, the backgroundAlarmType has changed
        val backgroundAlarmType = viewModel.getBackgroundAlarmType(context)
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                onKeepScreenOn(true)

                //When the user resume the app remove background alert and ongoing notification
                if (wasOnBackground == true) {
                    wasOnBackground = false
                    viewModel.onExitBackgroundMode(
                        context = context,
                        backgroundAlarmType = backgroundAlarmType
                    )
                }
                onEnterBackgroundState(backgroundAlarmType, false)
            }
            Lifecycle.Event.ON_PAUSE -> {
                onKeepScreenOn(false)

                // When the user put the app in background
                // enable a background alert and an ongoing notification
                wasOnBackground = true
                timer?.cancel()
                viewModel.onEnterBackgroundMode(
                    context = context,
                    backgroundAlarmType = backgroundAlarmType,
                    isAmbientMode = false
                )
                onEnterBackgroundState(backgroundAlarmType, true)
            }
            Lifecycle.Event.ON_STOP,
            Lifecycle.Event.ON_DESTROY -> {
                onKeepScreenOn(false)
            }
            else -> { }
        }
    }
}