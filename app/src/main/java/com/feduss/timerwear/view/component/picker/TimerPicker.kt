package com.feduss.timerwear.view.component.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.PickerState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerState
import com.feduss.timerwear.entity.TimerPickerModel
import com.feduss.timerwear.uistate.extension.Purple500
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.Purple200
import com.feduss.timerwear.uistate.extension.Purple700
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.picker.toRotaryScrollAdapter
import com.google.android.horologist.compose.rotaryinput.rotaryWithSnap
import java.util.Timer

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun TimerPicker(
    titleId: Int,
    initialMinutesValue: Int = 0,
    initialSecondsValue: Int = 0,
    onValuesConfirmed: (TimerPickerModel) -> Unit
) {

    val minutesPickerState = rememberPickerState(
        initialNumberOfOptions = 60,
        initiallySelectedOption = initialMinutesValue
    )
    val secondsPickerState = rememberPickerState(
        initialNumberOfOptions = 60,
        initiallySelectedOption = initialSecondsValue
    )

    val minutesPickerContentDescription by remember { derivedStateOf { "${minutesPickerState.selectedOption + 1}" } }
    val secondsPickerContentDescription by remember { derivedStateOf { "${secondsPickerState.selectedOption + 1}" } }
    val color = Color.Purple200
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp, 24.dp, 8.dp, 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = titleId),
            color = Color.Purple200
        )
        
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Picker(
                state = minutesPickerState,
                contentDescription =  minutesPickerContentDescription,
                titleId = R.string.timer_picker_minutes_title,
                color = color
            )

            Picker(
                state = secondsPickerState,
                contentDescription =  secondsPickerContentDescription,
                titleId = R.string.timer_picker_seconds_title,
                color = color
            )
        }

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
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_check),
                    contentDescription = "Confirm icon",
                    tint = Color.Black
                )
            },
            onClick = {
                onValuesConfirmed(
                    TimerPickerModel(
                        minutes = minutesPickerState.selectedOption,
                        seconds = secondsPickerState.selectedOption
                    )
                )
            }
        )
    }

}

@Composable
@OptIn(ExperimentalHorologistApi::class)
private fun Picker(
    state: PickerState,
    contentDescription: String,
    titleId: Int,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = titleId),
            color = Color.White,
            fontSize = TextUnit(10f, TextUnitType.Sp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        
        Picker(
            state = state,
            contentDescription = contentDescription,
            readOnly = false,
            readOnlyLabel = {},
            onSelected = {},
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .rotaryWithSnap(
                    state.toRotaryScrollAdapter()
                ),
            separation = 4.dp
        ) {
            Text(
                text = it.toString(),
                color = color
            )
        }
    }
}