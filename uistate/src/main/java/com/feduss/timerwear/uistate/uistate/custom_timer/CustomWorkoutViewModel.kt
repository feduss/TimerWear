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
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CustomWorkoutViewModel @Inject constructor() : ViewModel() {


    sealed class NavUiState {
        data object AddCustomWorkoutClicked: NavUiState()
        data class EditCustomWorkoutClicked(val id: Int): NavUiState()
        data class ExistingCustomWorkoutClicked(val customWorkoutCardUiState: CustomWorkoutCardUiState): NavUiState()
        data object BalloonDismissed: NavUiState()
        data class CustomWorkoutDeleted(val textId: Int): NavUiState()
    }

    private var _dataUiState = MutableStateFlow<CustomWorkoutUiState?>(null)
    var dataUiState = _dataUiState.asStateFlow()

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    private var customWorkoutModels: ArrayList<CustomWorkoutModel>? = null

    // Copies
    val headerTextId = R.string.custom_workout_header_text
    val addButtonTextId = R.string.custom_workout_add_button_text

    // Assets
    val workoutIconId = R.drawable.ic_run
    val workoutIconDescription = "ic_run"
    val addButtonIconId = R.drawable.ic_add
    val addButtonIconDescription = "ic_add"

    fun getCustomTimerList(context: Context) {

        this.customWorkoutModels = ArrayList(getCustomWorkoutModels(context) ?: listOf())

        _dataUiState.value = CustomWorkoutUiState(
            customWorkouts = customWorkoutModels?.mapIndexed { index, customWorkout ->
                val durationSecondsSum = customWorkout.timers.sumOf { it.duration.toSeconds() }
                val durationMins = durationSecondsSum / 60
                val durationSecs = durationSecondsSum % 60
                CustomWorkoutCardUiState(
                    leftIconId = workoutIconId,
                    leftIconDescription = workoutIconDescription,
                    id = customWorkout.id,
                    name = customWorkout.name,
                    duration = "${durationMins}m ${durationSecs}s x${customWorkout.repetition}",
                    isBalloonEnabled = needsToShowBalloon(context = context) && index == 0
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

    //Utils
    private fun needsToShowBalloon(context: Context): Boolean {
        return PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.BalloonDismissed.value
        ) == null
    }

    //Events

    fun navStateFired() {
        _navUiState.value = null
    }

    fun userHasClickedAddCustomWorkout() {
        _navUiState.value = NavUiState.AddCustomWorkoutClicked
    }

    fun onBalloonDismissed() {
        _navUiState.value = NavUiState.BalloonDismissed
    }

    fun balloonDismissed(context: Context) {
        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.BalloonDismissed.value,
            newValue = "true"
        )
    }

    fun userHasClickedEditCustomWorkout(id: Int) {
        _navUiState.value = NavUiState.EditCustomWorkoutClicked(id = id)
    }

    fun userHasClickedDeleteCustomWorkout(context: Context, id: Int) {

        val index = customWorkoutModels?.indexOfFirst { it.id == id }

        if (index != null) {
            customWorkoutModels?.removeAt(index)

            val json = Gson().toJson(customWorkoutModels)
            PrefsUtils.setStringPref(context, PrefParam.CustomWorkoutList.value, json)
            _dataUiState.update { state ->
                state?.copy(
                    customWorkouts = state.customWorkouts?.filter { it.id != id }
                )
            }
            _navUiState.value = NavUiState.CustomWorkoutDeleted(textId = R.string.custom_workout_deleted_text)
        }
    }
}