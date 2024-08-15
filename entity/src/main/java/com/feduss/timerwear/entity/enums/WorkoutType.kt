package com.feduss.timerwear.entity.enums

enum class WorkoutType {
    CustomWorkout,
    Emom,
    Tabata;

    override fun toString(): String {
        return when(this) {
            CustomWorkout -> "customWorkout"
            Emom -> "emom"
            Tabata -> "tabata"
        }
    }

    companion object {
        fun fromString(raw: String?): WorkoutType? {
            return when(raw) {
                CustomWorkout.toString() -> CustomWorkout
                Emom.toString() -> Emom
                Tabata.toString() -> Tabata
                else -> null
            }
        }
    }
}