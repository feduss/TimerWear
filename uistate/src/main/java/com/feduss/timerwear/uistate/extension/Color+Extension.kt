package com.feduss.timerwear.uistate.extension

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt

val Color.Companion.Orange: Color
    get() { return Color("#FF8133".toColorInt()) }

val Color.Companion.Green400: Color
    get() { return Color("#66BB6A".toColorInt()) }

val Color.Companion.Purple200: Color
    get() { return Color("#BB86FC".toColorInt()) }

val Color.Companion.Purple500: Color
    get() { return Color("#6200EE".toColorInt()) }

val Color.Companion.Purple700: Color
    get() { return Color("#3700B3".toColorInt()) }

val Color.Companion.PurpleCustom: Color
    get() { return Color("#E3BAFF".toColorInt()) }

val Color.Companion.Teal200: Color
    get() { return Color("#03DAC5".toColorInt()) }

val Color.Companion.Blue500: Color
    get() { return Color("#2196F3".toColorInt()) }

val Color.Companion.Indigo500: Color
    get() { return Color("#3F51B5".toColorInt()) }

val Color.Companion.DarkGrayNew: Color
    get() { return Color("#232323".toColorInt()) }

//TODO: find another name
val Color.Companion.ActiveTimer: Color
    get() { return Color("#649e5d".toColorInt()) }

//TODO: find another name
val Color.Companion.InactiveTimer: Color
    get() { return Color("#a15757".toColorInt()) }

fun Color.toHexString(): String {
    return String.format("#%06X", 0xFFFFFF and this.toArgb())
}

fun Color.Companion.fromHex(hex: String?): Color? {
    if (hex.isNullOrEmpty()) {
        return null
    }
    return Color(android.graphics.Color.parseColor("#$hex"))
}