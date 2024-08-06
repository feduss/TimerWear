package com.feduss.timerwear.uistate.uistate.custom_timer

import android.content.Context
import androidx.lifecycle.ViewModel
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.uistate.GenericButtonCardUiState
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CustomWorkoutViewModel @Inject constructor() : ViewModel() {


    sealed class NavUiState {
        data object AddCustomTimerClicked: NavUiState()
        data class EditCustomTimerClicked(val id: String): NavUiState()
        data class ExistingCustomTimerClicked(val customTimerCardUiState: CustomTimerCardUiState): NavUiState()
    }

    private var _dataUiState = MutableStateFlow<CustomWorkoutUiState?>(null)
    var dataUiState = _dataUiState.asStateFlow()

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    private var customWorkoutModels: List<CustomWorkoutModel>? = null

    // Copies
    val headerTextId = R.string.custom_workout_header_text
    val addButtonTextId = R.string.custom_workout_add_button_text

    // Assets
    val workoutIconId = R.drawable.ic_run
    val workoutIconDescription = "ic_run"
    val addButtonIconId = R.drawable.ic_add
    val addButtonIconDescription = "ic_add"

    fun getCustomTimerList(context: Context) {

        val customWorkoutModels: List<CustomWorkoutModel>? = getCustomWorkoutModels(context)
        this.customWorkoutModels = customWorkoutModels

        _dataUiState.value = CustomWorkoutUiState(
            customWorkouts = customWorkoutModels?.map { customWorkout ->
                val durationSecondsSum = customWorkout.timers.sumOf { it.duration.toSeconds() }
                val durationMins = durationSecondsSum / 60
                val durationSecs = durationSecondsSum % 60
                CustomTimerCardUiState(
                    leftIconId = workoutIconId,
                    leftIconDescription = workoutIconDescription,
                    timerId = customWorkout.id,
                    timerName = customWorkout.name,
                    timerDuration = "${durationMins}m ${durationSecs}s x${customWorkout.repetition}"
                )
            },
            addCustomWorkoutButton = GenericButtonCardUiState(
                leftIconId = addButtonIconId,
                leftIconDescription = addButtonIconDescription,
                textId = addButtonTextId
            )
        )
    }

    private fun getCustomWorkoutModels(context: Context): List<CustomWorkoutModel>? {
        val customWorkoutsRawModels =
            PrefsUtils.getStringPref(context, PrefParam.CustomWorkoutList.value)
        val sType = object : TypeToken<List<CustomWorkoutModel>>() {}.type
        val customWorkoutModels: List<CustomWorkoutModel>? =
            Gson().fromJson<List<CustomWorkoutModel>?>(customWorkoutsRawModels, sType)
        return customWorkoutModels
    }

    fun navStateFired() {
        _navUiState.value = null
    }

    fun userHasClickedAddTimer() {
        _navUiState.value = NavUiState.AddCustomTimerClicked
    }
}