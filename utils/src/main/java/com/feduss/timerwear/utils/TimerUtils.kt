package com.feduss.timerwear.utils

import android.content.Context
import com.feduss.timerwear.entity.CustomTimerModel
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.utils.extension.getPrefName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TimerUtils {

    companion object {

        fun getCustomWorkoutModels(context: Context, workoutType: WorkoutType): List<CustomWorkoutModel>? {
            val customWorkoutsRawModels =
                PrefsUtils.getStringPref(context, workoutType.getPrefName())
            val sType = object : TypeToken<List<CustomWorkoutModel>>() {}.type
            val customWorkoutModels: List<CustomWorkoutModel>? =
                Gson().fromJson<List<CustomWorkoutModel>?>(customWorkoutsRawModels, sType)
            return customWorkoutModels
        }

        fun getNextTimerIndexAndRepetition(
            totalTimers: Int,
            totalRepetitions: Int,
            currentTimerIndex: Int,
            currentRepetition: Int
        ): Pair<Int, Int> {
            val newCurrentTimerIndex: Int
            val newCurrentRepetition: Int
            if (currentTimerIndex == totalTimers - 1) {
                if (currentRepetition == totalRepetitions - 1) {
                    newCurrentTimerIndex = -1
                    newCurrentRepetition = -1
                } else {
                    newCurrentTimerIndex = 0
                    newCurrentRepetition = currentRepetition + 1
                }
            } else {
                if (totalTimers > 1) {
                    newCurrentTimerIndex = currentTimerIndex + 1
                    newCurrentRepetition = currentRepetition
                } else {
                    newCurrentTimerIndex = 0
                    newCurrentRepetition = currentRepetition + 1
                }
            }

            /*Log.e("TEST123: ", "func: newCurrentTimerIndex $newCurrentTimerIndex, " +
                    "newCurrentRepetition $newCurrentRepetition, totalTimers $totalTimers, " +
                    "totalRepetitions $totalRepetitions, currentTimerIndex $currentTimerIndex, " +
                    "currentRepetition $currentRepetition")*/

            return Pair(newCurrentTimerIndex, newCurrentRepetition)
        }

        fun needsToDisplayIntermediumRest(
            timer: CustomTimerModel,
            repetition: Int,
            totalRepetitions: Int,
            frequency: Int?
        ): Boolean {
            return if (timer.type == TimerType.IntermediumRest && frequency != null) {
                // Skip last intermedium rest
                if (repetition == totalRepetitions - 1) {
                    false
                } else if (frequency == 1) {
                    true
                } else {
                    repetition > 0 && (repetition + 1) % frequency == 0
                }
            } else {
                false
            }
        }

    }
}