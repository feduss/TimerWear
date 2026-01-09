package com.feduss.timerwear.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.PositionIndicator
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun PageView(
    columnState: ScalingLazyColumnState,
    ambientState: MutableState<AmbientState>,
    endCurvedText: String? = null,
    content: @Composable BoxScope.(ScalingLazyColumnState) -> Unit
) {

    ScreenScaffold(
        timeText = {
            CustomTimeText(
                ambientState = ambientState,
                endCurvedText = endCurvedText
            )
        },
        scrollState = columnState,
        positionIndicator = { PositionIndicator(scalingLazyListState = columnState.state) },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
        content(columnState)
    }
}
