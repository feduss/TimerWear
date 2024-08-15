package com.feduss.timerwear.uistate.extension

import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.entity.enums.TimerType.IntermediumRest
import com.feduss.timerwear.entity.enums.TimerType.Rest
import com.feduss.timerwear.entity.enums.TimerType.Work
import com.feduss.timerwear.uistate.R

fun TimerType.getStringId(): Int {
    return when (this) {
        Work -> R.string.timer_type_work_text
        Rest -> R.string.timer_type_rest_text
        IntermediumRest -> R.string.timer_type_intermedium_rest_text
    }
}