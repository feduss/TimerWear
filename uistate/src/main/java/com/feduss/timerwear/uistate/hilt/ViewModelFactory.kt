package com.feduss.timerwear.uistate.hilt

import com.feduss.timerwear.uistate.uistate.add_custom_timer.AddCustomWorkoutViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactory {
    fun addCustomWorkoutViewModelFactory(): AddCustomWorkoutViewModel.Factory
}