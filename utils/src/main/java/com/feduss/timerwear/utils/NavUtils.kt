package com.feduss.timerwear.utils

import android.content.Context
import androidx.navigation.NavController
import com.feduss.timerwear.entity.enums.Params
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.entity.enums.WorkoutType

class NavUtils {
    companion object {
        fun goToExistingWorkout(
            context: Context,
            navController: NavController,
            workoutId: String,
            workoutType: WorkoutType,
            currentTimerIndex: String?,
            currentRepetition: String?,
            currentTimerSecondsRemaining: String?,
            checkAmbientMode: Boolean = true
        ) {

            var optionalArgs: Map<String, String>? = null
            if (currentTimerIndex != null && currentRepetition != null && currentTimerSecondsRemaining != null) {
                optionalArgs = mapOf(
                    Params.CurrentTimerIndex.name to currentTimerIndex,
                    Params.CurrentRepetition.name to currentRepetition,
                    Params.CurrentTimerSecondsRemaining.name to currentTimerSecondsRemaining
                )
            }
            val section: Section = if (checkAmbientMode && !AmbientUtils.isAmbientDisplayOn(context)) {
                Section.AmbientWarning
            } else {
                Section.Timer
            }

            navController.navigate(section.withArgs(
                args = listOf(workoutId, workoutType.toString()),
                optionalArgs = optionalArgs
            )) {
                if (section == Section.Timer) {
                    popUpTo(navController.currentBackStackEntry?.destination?.route ?: return@navigate) {
                        inclusive =  true
                    }
                }
            }
        }

        fun goToAddCustomWorkoutPage(
            navController: NavController,
            workoutId: String? = null,
            workoutType: WorkoutType
        ) {

            var optionalArgs: Map<String, String>? = null
            if (workoutId != null) {
                optionalArgs = mapOf(Params.WorkoutId.name to workoutId)
            }
            navController.navigate(
                Section.AddCustomWorkout.withArgs(
                    args = listOf(workoutType.toString()),
                    optionalArgs = optionalArgs
                )
            )
        }
    }
}