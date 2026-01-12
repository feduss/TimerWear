package com.feduss.timerwear.view.component.picker

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anhaki.picktime.PickHourMinuteSecond
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle
import com.feduss.timerwear.entity.TimerPickerModel
import com.feduss.timerwear.extension.infiniteMarquee
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.Purple200
import com.feduss.timerwear.view.component.bottomsheet.CustomModalBottomSheet
import java.util.Calendar
import kotlin.String

//TODO: remove wear os code
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerPicker(
    titleId: Int,
    initialMinutesValue: Int?,
    initialSecondsValue: Int,
    onDismiss: (TimerPickerModel) -> Unit
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )

    var minutes by remember { mutableStateOf(initialMinutesValue ?: 0) }
    var seconds by remember { mutableStateOf(initialSecondsValue) }

    CustomModalBottomSheet(
        sheetState = sheetState,
        title = stringResource(id = titleId),
        content = {
            // TODO: ask the dev a picker without hour
            PickHourMinuteSecond(
                initialHour = 0,
                onHourChange = { },
                initialMinute = initialMinutesValue ?: 0,
                onMinuteChange = { minutes = it },
                initialSecond = initialSecondsValue,
                onSecondChange = { seconds = it },
                containerColor = MaterialTheme.colorScheme.surface,
                selectedTextStyle = PickTimeTextStyle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                ),
                unselectedTextStyle = PickTimeTextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                ),
                focusIndicator = PickTimeFocusIndicator(
                    enabled = true,
                    widthFull = false,
                    background = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(0.dp, MaterialTheme.colorScheme.onBackground),
                )
            )
        },
        footerText = "UI del picker in beta (libreria terze parti), le ore vanno rimosse", //TODO: to remove
        onDismissRequest = {
            onDismiss(
                TimerPickerModel(
                    minutes = minutes,
                    seconds = seconds
                )
            )
        }
    )
}