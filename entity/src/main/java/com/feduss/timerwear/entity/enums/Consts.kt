package com.feduss.timerwear.entity.enums

sealed class Consts(val value: String) {
    data object AlarmEnd: Consts("321")
    data object MainChannelId: Consts("TomatoMainChannelId")
    data object SubChannelId: Consts("TomatoSubChannelId")
    data object NotificationChannelId: Consts("TomatoNotificationChannelId")
    data object MainNotificationId: Consts("16")
    data object SubNotificationId: Consts("10")
    data object MainNotificationVisibleChannel: Consts("Notifica del timer attivo")
    data object SubNotificationVisibleChannel: Consts("Notifica del timer scaduto")
    data object FromOngoingNotification: Consts("FromOngoingNotification")
    data object OngoingActivityId: Consts("13")
}
