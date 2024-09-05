package com.feduss.timerwear.view.ambient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.feduss.timerwear.uistate.uistate.timer.TimerViewUiState
import com.google.android.horologist.compose.ambient.AmbientState

@Composable
fun AmbientTimer(
    timerViewUiState: TimerViewUiState,
    ambientState: AmbientState.Ambient,
    onTimerSet: (String) -> Unit
) {

    //TODO: handle ambient details
    if (ambientState.ambientDetails?.burnInProtectionRequired == true) {

    }

    if (ambientState.ambientDetails?.deviceHasLowBitAmbient == true) {

    }
    //endtodo

    onTimerSet(timerViewUiState.timeText)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timerViewUiState.currentProgress,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = TextUnit(10f, TextUnitType.Sp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = timerViewUiState.middleTimerStatusValueText,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = TextUnit(20.0f, TextUnitType.Sp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            imageVector = ImageVector.vectorResource(id = timerViewUiState.ambientIconId),
            contentDescription = timerViewUiState.ambientIconDescription,
            tint = Color.White
        )
    }
}