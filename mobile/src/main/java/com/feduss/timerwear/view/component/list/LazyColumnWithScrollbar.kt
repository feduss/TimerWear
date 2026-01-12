package com.feduss.timerwear.view.component.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Composable
fun LazyColumnWithScrollbar(
    modifier: Modifier = Modifier,
    state: LazyListState,
    spaceBetweenItems: Int = 0,
    content: LazyListScope.() -> Unit
) {
    LazyColumnScrollbar(
        state = state,
        settings = ScrollbarSettings.Default.copy(enabled = false) //TODO: temporary disabled
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    top = 16.dp,
                    bottom = 4.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            state = state,
            verticalArrangement = Arrangement.spacedBy(spaceBetweenItems.dp)
        ) {
            content()
        }
    }
}