package com.feduss.timerwear.extension

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
val Modifier.infiniteMarquee: Modifier
    get() { return then(basicMarquee(iterations = Int.MAX_VALUE)) }