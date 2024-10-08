package com.feduss.timerwear.uistate.factory

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.uistate.hilt.ViewModelFactory
import com.feduss.timerwear.uistate.uistate.add_custom_timer.AddCustomWorkoutViewModel
import dagger.hilt.android.EntryPointAccessors

@Composable
fun getAddCustomWorkoutViewModel(
    activity: Activity,
    workoutType: WorkoutType,
    workoutId: Int?,
): AddCustomWorkoutViewModel = viewModel(
    factory = AddCustomWorkoutViewModel.provideFactory(
        assistedFactory = EntryPointAccessors.fromActivity(
            activity,
            ViewModelFactory::class.java
        ).addCustomWorkoutViewModelFactory(),
        workoutType = workoutType,
        workoutId = workoutId
    )
)