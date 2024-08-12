package com.feduss.timerwear.entity.enums

enum class TimerType {
    CustomWorkout,
    Emom,
    Tabata;

    override fun toString(): String {
        return when(this) {
            CustomWorkout -> "CustomWorkout"
            Emom -> "Emom"
            Tabata -> "Tabata"
        }
    }

    companion object {
        fun fromString(raw: String?): TimerType? {
            return when(raw) {
                CustomWorkout.toString() -> CustomWorkout
                Emom.toString() -> Emom
                Tabata.toString() -> Tabata
                else -> null
            }
        }
    }
}