package com.feduss.timerwear.extension

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.Companion.then

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.infiniteMarquee(): Modifier = then(this.basicMarquee(iterations = Int.MAX_VALUE))