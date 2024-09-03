package com.feduss.timerwear.entity.enums

enum class WorkoutType {
    CustomWorkout,
    Emom,
    Hiit;

    override fun toString(): String {
        return when(this) {
            CustomWorkout -> "customWorkout"
            Emom -> "emom"
            Hiit -> "hiit"
        }
    }

    companion object {
        fun fromString(raw: String?): WorkoutType? {
            return when(raw) {
                CustomWorkout.toString() -> CustomWorkout
                Emom.toString() -> Emom
                Hiit.toString() -> Hiit
                else -> null
            }
        }
    }
}