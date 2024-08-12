package com.feduss.timerwear.utils

import android.content.Context
import android.util.Log
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TimerUtils {

    companion object {

        fun getCustomWorkoutModels(context: Context): List<CustomWorkoutModel>? {
            val customWorkoutsRawModels =
                PrefsUtils.getStringPref(context, PrefParam.CustomWorkoutList.value)
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
                newCurrentTimerIndex = currentTimerIndex + 1
                newCurrentRepetition = currentRepetition
            }

            /*Log.e("TEST123: ", "func: newCurrentTimerIndex $newCurrentTimerIndex, " +
                    "newCurrentRepetition $newCurrentRepetition, totalTimers $totalTimers, " +
                    "totalRepetitions $totalRepetitions, currentTimerIndex $currentTimerIndex, " +
                    "currentRepetition $currentRepetition")*/

            return Pair(newCurrentRepetition, newCurrentTimerIndex)
        }
    }
}