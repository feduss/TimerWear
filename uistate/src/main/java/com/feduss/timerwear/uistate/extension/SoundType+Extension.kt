package com.feduss.timerwear.uistate.extension

import com.feduss.timerwear.entity.enums.SoundType
import com.feduss.timerwear.entity.enums.SoundType.Finish
import com.feduss.timerwear.entity.enums.SoundType.Rest
import com.feduss.timerwear.entity.enums.SoundType.Work
import com.feduss.timerwear.uistate.R

fun SoundType.getRawMp3(): Int {
    return when(this) {
        Work -> R.raw.start
        Rest -> R.raw.pause
        Finish -> R.raw.finish
    }
}