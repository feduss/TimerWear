package com.feduss.timerwear.entity.enums

sealed class Consts(val value: String) {
    data object CountdownTimerAlarmEndRequestCode: Consts("321")
    data object CountdownTimerSeconds: Consts("5.9")
    data object ActiveTimerAlarmEndRequestCode: Consts("717")
    data object MainChannelId: Consts("TimerWearMainChannelId")
    data object MainNotificationId: Consts("16")
    data object MainNotificationVisibleChannel: Consts("Notifica del timer attivo")
    data object OngoingActivityId: Consts("13")
}
