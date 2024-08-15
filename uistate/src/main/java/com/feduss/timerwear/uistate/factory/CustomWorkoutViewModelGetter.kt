package com.feduss.timerwear.uistate.factory

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.uistate.hilt.ViewModelFactory
import com.feduss.timerwear.uistate.uistate.custom_timer.CustomWorkoutViewModel
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import dagger.hilt.android.EntryPointAccessors

@Composable
fun getCustomWorkoutView(
    activity: Activity,
    workoutType: WorkoutType
): CustomWorkoutViewModel = viewModel(
    factory = CustomWorkoutViewModel.provideFactory(
        assistedFactory = EntryPointAccessors.fromActivity(
            activity,
            ViewModelFactory::class.java
        ).customWorkoutViewModelFactory(),
        workoutType = workoutType
    )
)