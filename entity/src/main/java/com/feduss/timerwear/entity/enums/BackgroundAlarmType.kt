package com.feduss.timerwear.entity.enums

enum class BackgroundAlarmType {
    CountdownTimer,
    ActiveTimer;

    fun getRequestCode(): Int {
        return when(this) {
            CountdownTimer -> Consts.CountdownTimerAlarmEndRequestCode.value.toInt()
            ActiveTimer -> Consts.ActiveTimerAlarmEndRequestCode.value.toInt()
        }
    }
}