package com.feduss.timerwear.uistate.extension

import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.entity.enums.WorkoutType

fun WorkoutType.getStringId(): Int {
    return when(this) {
        WorkoutType.CustomWorkout -> R.string.main_page_custom_workout_button
        WorkoutType.Emom -> R.string.main_page_emom_timer_button
        WorkoutType.Tabata -> R.string.main_page_tabata_timer_button
    }
}