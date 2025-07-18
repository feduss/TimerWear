package com.feduss.timerwear.uistate.uistate.custom_timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.getStringId
import com.feduss.timerwear.uistate.uistate.GenericButtonCardUiState
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import com.feduss.timerwear.utils.extension.getPrefName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CustomWorkoutViewModel @AssistedInject constructor(
    @Assisted("workoutType") val workoutType: WorkoutType
) : ViewModel() {

    //Factory
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("workoutType") workoutType: WorkoutType
        ): CustomWorkoutViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            workoutType: WorkoutType
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(
                    workoutType
                ) as T
            }
        }
    }

    sealed class NavUiState {
        data class AddCustomWorkoutClicked(
            val workoutType: WorkoutType
        ): NavUiState()
        data class EditCustomWorkoutClicked(
            val workoutId: Int,
            val workoutType: WorkoutType
        ): NavUiState()
        data class ExistingCustomWorkoutClicked(
            val workoutId: Int,
            val workoutType: WorkoutType,
            val currentTimerIndex: Int? = null,
            val currentRepetition: Int? = null,
            val currentTimerSecondsRemaining: Double? = null
        ): NavUiState()
        data object BalloonDismissed: NavUiState()
        data class CustomWorkoutDeleted(val textId: Int): NavUiState()
    }

    private var _dataUiState = MutableStateFlow<CustomWorkoutUiState?>(null)
    var dataUiState = _dataUiState.asStateFlow()

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    private var customWorkoutModels: ArrayList<CustomWorkoutModel>? = null

    // Copies
    val addButtonTextId = R.string.custom_workout_add_button_text

    // Assets
    val workoutIconId = R.drawable.ic_run
    val workoutIconDescription = "ic_run"
    val addButtonIconId = R.drawable.ic_add
    val addButtonIconDescription = "ic_add"

    fun checkActiveTimer(context: Context) {
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

            // Check if existing workout is valid before resuming it
            // else wipe it, it's probably corrupted someway
            if (
                activeWorkoutId != null && activeWorkoutId > -1 &&
                activeTimerIndex != null && activeTimerIndex > -1 &&
                activeWorkoutRepetition != null && activeWorkoutRepetition > -1 &&
                activeTimerSecondsRemaining != null && activeTimerSecondsRemaining >= 0.0
            ) {
                _navUiState.value = NavUiState.ExistingCustomWorkoutClicked(
                    workoutId = activeWorkoutId,
                    workoutType = workoutType,
                    currentTimerIndex = activeTimerIndex,
                    currentRepetition = activeWorkoutRepetition,
                    currentTimerSecondsRemaining = activeTimerSecondsRemaining
                )
            } else {
                PrefsUtils.cancelTimerInPrefs(context)
            }
        }
    }

    fun loadUiState(context: Context) {
        this.customWorkoutModels = ArrayList(getCustomWorkoutModels(context) ?: listOf())

        _dataUiState.value = CustomWorkoutUiState(
            headerTitleId = workoutType.getStringId(),
            customWorkouts = customWorkoutModels?.mapIndexed { index, customWorkout ->
                val durationSecondsSum = customWorkout.timers.filter { it.type != TimerType.IntermediumRest }.sumOf { it.duration.toSeconds() }
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
            PrefsUtils.getStringPref(context, workoutType.getPrefName())
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
        _navUiState.value = NavUiState.AddCustomWorkoutClicked(workoutType = workoutType)
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
        _navUiState.value = NavUiState.EditCustomWorkoutClicked(
            workoutId = id,
            workoutType = workoutType
        )
    }

    fun userHasClickedDeleteCustomWorkout(context: Context, id: Int) {

        val index = customWorkoutModels?.indexOfFirst { it.id == id }

        if (index != null) {
            customWorkoutModels?.removeAt(index)

            val json = Gson().toJson(customWorkoutModels)
            PrefsUtils.setStringPref(context, workoutType.getPrefName(), json)
            _dataUiState.update { state ->
                state?.copy(
                    customWorkouts = state.customWorkouts?.filter { it.id != id }
                )
            }
            _navUiState.value = NavUiState.CustomWorkoutDeleted(textId = R.string.custom_workout_deleted_text)
        }
    }

    fun userHasClickedExistingWorkout(id: Int) {
        _navUiState.value = NavUiState.ExistingCustomWorkoutClicked(
            workoutId = id,
            workoutType = workoutType
        )
    }
}