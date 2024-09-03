package com.feduss.timerwear.utils.extension

import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.entity.enums.WorkoutType.CustomWorkout
import com.feduss.timerwear.entity.enums.WorkoutType.Emom
import com.feduss.timerwear.entity.enums.WorkoutType.Hiit
import com.feduss.timerwear.utils.PrefParam

fun WorkoutType.getPrefName(): String {
    return when(this) {
        CustomWorkout -> PrefParam.CustomWorkoutList.value
        Emom -> PrefParam.EmomList.value
        Hiit -> PrefParam.HiitList.value
    }
}