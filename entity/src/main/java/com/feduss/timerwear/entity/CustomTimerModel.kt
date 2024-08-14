package com.feduss.timerwear.entity

import com.feduss.timerwear.entity.enums.CustomTimerType
import java.util.UUID

data class CustomTimerModel(
    val id: Int,
    val uuid: UUID = UUID.randomUUID(),
    val name: String,
    val duration: TimerPickerModel,
    val type: CustomTimerType
)