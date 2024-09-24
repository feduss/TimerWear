package com.feduss.timerwear.uistate.factory

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.uistate.hilt.ViewModelFactory
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import dagger.hilt.android.EntryPointAccessors

@Composable
fun getTimerViewModel(
    activity: Activity,
    workoutId: Int,
    workoutType: WorkoutType,
    currentTimerIndex: Int? = null,
    currentRepetition: Int? = null,
    currentTimerSecondsRemaining: Double? = null
): TimerViewModel = viewModel(
    factory = TimerViewModel.provideFactory(
        assistedFactory = EntryPointAccessors.fromActivity(
            activity,
            ViewModelFactory::class.java
        ).timerViewModelFactory(),
        workoutId = workoutId,
        workoutType = workoutType,
        currentTimerIndex = currentTimerIndex,
        currentRepetition = currentRepetition,
        currentTimerSecondsRemaining = currentTimerSecondsRemaining
    )
)