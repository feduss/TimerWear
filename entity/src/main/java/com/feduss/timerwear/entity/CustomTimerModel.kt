package com.feduss.timerwear.entity

import com.feduss.timerwear.entity.enums.CustomTimerType

data class CustomTimerModel(
    val id: Int, val name: String, val duration: TimerPickerModel,
    val type: CustomTimerType
)