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
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.utils.AmbientUtils
import com.google.android.horologist.compose.ambient.AmbientState

@Composable
fun ObserveTimerAmbientMode(
    ambientState: AmbientState,
    viewModel: TimerViewModel,
    context: Context,
    backgroundAlarmType: BackgroundAlarmType,
    onKeepScreenOn: (Boolean) -> Unit,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit,
    onSetAmbientMode: () -> Unit,
    timer: CountDownTimer?,
    userHasSkippedTimer: MutableState<Boolean>? = null
) {
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

        if (ambientMode is AmbientState.Interactive) {
            viewModel.userHasDisabledAmbientMode(
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