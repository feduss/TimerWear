package com.feduss.timerwear.uistate

import android.content.Context
import androidx.lifecycle.ViewModel
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor() : ViewModel() {

    sealed class NavUiState {
        data class GoToCustomWorkout(val isTimerActive: Boolean): NavUiState()
        data class GoToEmom(val isTimerActive: Boolean): NavUiState()
        data class GoToHiit(val isTimerActive: Boolean): NavUiState()
        data object GoToSettings : NavUiState()
    }

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    fun loadActiveTimer(context: Context) {
        if (PrefsUtils.isTimerActive(context)) {

            val activeWorkoutId = PrefsUtils.getStringPref(
                context = context,
                pref = PrefParam.CurrentWorkoutId.value
            )?.toIntOrNull()

            val activeTimerIndex = PrefsUtils.getStringPref(
                context = context,
                pref = PrefParam.CurrentTimerIndex.value
            )?.toIntOrNull()

            val activeWorkoutRepetition = PrefsUtils.getStringPref(
                context = context,
                pref = PrefParam.CurrentRepetition.value
            )?.toIntOrNull()

            val activeTimerSecondsRemaining = PrefsUtils.getStringPref(
                context = context,
                pref = PrefParam.CurrentTimerSecondsRemaining.value
            )?.toDoubleOrNull()

            val workoutType = PrefsUtils.getWorkoutType(context)

            // Check if existing workout is valid before resuming it
            // else wipe it, it's probably corrupted someway
            if (
                activeWorkoutId != null && activeWorkoutId > -1 &&
                activeTimerIndex != null && activeTimerIndex > -1 &&
                activeWorkoutRepetition != null && activeWorkoutRepetition > -1 &&
                activeTimerSecondsRemaining != null && activeTimerSecondsRemaining >= 0.0 &&
                workoutType != null
            ) {
                when(workoutType) {
                    WorkoutType.CustomWorkout -> _navUiState.value = NavUiState.GoToCustomWorkout(true)
                    WorkoutType.Emom -> _navUiState.value = NavUiState.GoToEmom(true)
                    WorkoutType.Hiit -> _navUiState.value = NavUiState.GoToHiit(true)
                }
            } else {
                PrefsUtils.cancelTimerInPrefs(context)
            }
        }
    }

    fun userClickedOnCustomWorkout() {
        _navUiState.value = NavUiState.GoToCustomWorkout(false)
    }

    fun userClickedOnEmom() {
        _navUiState.value = NavUiState.GoToEmom(false)
    }

    fun userClickedOnHiit() {
        _navUiState.value = NavUiState.GoToHiit(false)
    }

    fun userClickedOnSettings() {
        _navUiState.value = NavUiState.GoToSettings
    }


    fun navStateFired() {
        _navUiState.value = null
    }
}