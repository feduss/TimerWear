package com.feduss.timerwear.view.timer

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.uistate.extension.getRawMp3
import com.feduss.timerwear.uistate.uistate.timer.TimerTYPViewUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.utils.AlarmUtils

@Composable
fun TimerTYPView(
    context: Context,
    viewModel: TimerViewModel,
    timerTYPViewUiState: TimerTYPViewUiState,
    onTimerSet: (String) -> Unit
) {

    onTimerSet("")

    LaunchedEffect(Unit) {
        if (viewModel.isSoundEnabled) {
            AlarmUtils.playSound(
                context = context,
                soundId = SoundType.Finish.getRawMp3()
            )
        }
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