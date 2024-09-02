package com.feduss.timerwear.view.component

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.curvedText
import com.feduss.timerwear.uistate.extension.PurpleCustom
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.scrollAway
import java.util.Locale

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun CustomTimeText(
    columnState: ScalingLazyColumnState,
    ambientState: MutableState<AmbientState>,
    endCurvedText: String?
) {
    val timeSource = TimeTextDefaults.timeSource(
        DateFormat.getBestDateTimePattern(Locale.getDefault(), "HH:mm")
    )

    val textColor = if (ambientState.value is AmbientState.Interactive) {
        Color.PurpleCustom
    } else {
        Color.White
    }

    val modifier = Modifier.scrollAway(columnState)

    if (endCurvedText?.isNotEmpty() == true) {
        ResponsiveTimeText(
            modifier = modifier,
            timeSource = timeSource,
            endCurvedContent = {
                curvedText(
                    text = endCurvedText,
                    color = textColor
                )
            }
        )
    } else {
        ResponsiveTimeText(
            modifier = modifier,
            timeSource = timeSource,
        )
    }
}