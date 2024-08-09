package com.feduss.timerwear.uistate.uistate.timer

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.ActiveTimer
import com.feduss.timerwear.uistate.extension.InactiveTimer
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TimerViewModel @AssistedInject constructor(
    @Assisted("workoutId") val workoutId: Int,
    @Assisted("currentTimerIndex") val currentTimerIndex: Int?,
    @Assisted("currentRepetition") val currentRepetition: Int?,
    @Assisted("currentTimerSecondsRemaining") val currentTimerSecondsRemaining: Int?
) : ViewModel() {

    //Factory
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("workoutId") workoutId: Int,
            @Assisted("currentTimerIndex") currentTimerIndex: Int?,
            @Assisted("currentRepetition") currentRepetition: Int?,
            @Assisted("currentTimerSecondsRemaining") currentTimerSecondsRemaining: Int?
        ): TimerViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            workoutId: Int,
            currentTimerIndex: Int?,
            currentRepetition: Int?,
            currentTimerSecondsRemaining: Int?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(
                    workoutId,
                    currentTimerIndex,
                    currentRepetition,
                    currentTimerSecondsRemaining
                ) as T
            }
        }
    }

    sealed class NavUiState {
        data object GoToEndOfWorkout: NavUiState()
        data object GoBackToCustomWorkoutList: NavUiState()
        data class GoToNextTimer(val currentTimerIndex: Int, val currentRepetition: Int): NavUiState()
        data class ChangeAlertDialogState(
            val isAlertDialogVisible: Boolean,
            val alertDialogType: AlertDialogType?,
            val completion: () -> Unit
        ): NavUiState()
        data class ChangeTimerState(
            val timerSecondsRemaining: Int,
            val isTimerActive: Boolean
        ): NavUiState()
    }

    private var _dataUiState = MutableStateFlow<TimerUiState?>(null)
    var dataUiState = _dataUiState.asStateFlow()

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    private var customWorkoutModel: CustomWorkoutModel? = null

    //Copies
    private val alertDialogSkipTitleId = R.string.timer_skip_timer
    private val alertDialogStopTitleId = R.string.timer_stop_timer_question
    private val repetitionTitleId = R.string.timer_repetition_title

    //Assets
    private val playIconId = R.drawable.ic_play
    private val playIconDescription = "ic_play"
    private val pauseIconId = R.drawable.ic_pause
    private val pauseIconDescription = "ic_pause"
    private val skipNextIcon = R.drawable.ic_skip_next
    private val skipNextIconDescription = "ic_skip_next"
    private val closeIconId = R.drawable.ic_close
    private val checkIconId = R.drawable.ic_check

    //Colors
    private val activeColor = Color.ActiveTimer
    private val inactiveColor = Color.InactiveTimer

    //State
    fun loadUiState(context: Context) {

        if (_dataUiState.value != null) return

        val id = workoutId.toInt()
        customWorkoutModel = getCustomWorkoutModels(context = context)?.first { it.id == id }

        _dataUiState.value = customWorkoutModel?.let {
            val currentTimer = it.timers[currentTimerIndex ?: 0]
            TimerUiState(
                customWorkoutModel = it,
                currentTimerId = currentTimerIndex ?: 0,
                currentTimerName = currentTimer.name,
                currentRepetition = currentRepetition ?:0,
                middleTimerStatusValueText = currentTimer.duration.toString(),
                isTimerActive = true,
                maxTimerSeconds = currentTimer.duration.toSeconds(),
                timerSecondsRemaining = currentTimerSecondsRemaining ?: currentTimer.duration.toSeconds(),
                bottomLeftButtonId = pauseIconId,
                bottomLeftButtonDescription = pauseIconDescription,
                bottomRightButtonId = skipNextIcon,
                bottomRightButtonDescription = skipNextIconDescription,
                circularSliderColor = activeColor,
                isAlertDialogVisible = false,
                alertDialogSkipTitleId = alertDialogSkipTitleId,
                alertDialogSkipNegativeIconId = closeIconId,
                alertDialogSkipNegativeIconDescription = "Close icon",
                alertDialogSkipPositiveIconId = checkIconId,
                alertDialogSkipPositiveIconDescription = "Check icon",
                alertDialogStopTitleId = alertDialogStopTitleId,
                alertDialogStopNegativeIconId = closeIconId,
                alertDialogStopNegativeIconDescription = "Close icon",
                alertDialogStopPositiveIconId = checkIconId,
                alertDialogStopPositiveIconDescription = "Check icon",
                repetitionTitleId = repetitionTitleId
            )
        }
    }

    fun userGoToNextTimer(currentTimerIndex: Int, currentRepetition: Int) {
        _navUiState.value = NavUiState.GoToNextTimer(
            currentTimerIndex = currentTimerIndex,
            currentRepetition = currentRepetition
        )
    }

    fun userSkipToNextTimer(currentTimerIndex: Int, currentRepetition: Int) {
        _navUiState.value = NavUiState.GoToNextTimer(
            currentTimerIndex = currentTimerIndex,
            currentRepetition = currentRepetition
        )
    }

    fun userChangeAlertDialogState(
        isAlertDialogVisible: Boolean, alertDialogType: AlertDialogType?, completion: () -> Unit) {
        _navUiState.value = NavUiState.ChangeAlertDialogState(
            isAlertDialogVisible = isAlertDialogVisible,
            alertDialogType = alertDialogType,
            completion = completion
        )
    }

    fun changeAlertDialogState(
        isAlertDialogVisible: Boolean, alertDialogType: AlertDialogType?, completion: () -> Unit) {
        _dataUiState.update {
            it?.copy(
                isAlertDialogVisible = isAlertDialogVisible,
                alertDialogType = alertDialogType
            )
        }
        completion()
    }

    fun userGoBackToWorkoutList() {
        _navUiState.value = NavUiState.GoBackToCustomWorkoutList
    }

    fun userChangedTimerState(
        timerSecondsRemaining: Int, isTimerActive: Boolean
    ) {
        _navUiState.value = NavUiState.ChangeTimerState(
            timerSecondsRemaining = timerSecondsRemaining,
            isTimerActive = isTimerActive
        )
    }

    fun updateCircularProgressBarProgress(progress: Double) {
        _dataUiState.update {
            it?.copy(
                circularSliderProgress = progress
            )
        }
    }

    fun navStateFired() {
        _navUiState.value = null
    }

    //

    fun saveCurrentTimerData(
        context: Context, currentTimerId: Int, currentTimerName: String?,
        currentRepetition: Int?, currentTimerSecondsRemaining: Int?
    ) {

        //Save the current workout id
        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.CurrentWorkoutId.value,
            newValue = workoutId.toString()
        )

        //Save the current timer id
        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.CurrentTimerIndex.value,
            newValue = currentTimerId.toString()
        )

        //Save the current timer title (used in notification)
        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.CurrentTimerName.value,
            newValue = currentTimerName
        )

        //Save the current repetition
        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.CurrentRepetition.value,
            newValue = currentRepetition?.toString()
        )


        //Save the current timer seconds remaining
        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.CurrentTimerSecondsRemaining.value,
            newValue = currentTimerSecondsRemaining?.toString()
        )
    }

    fun setTimerState(
        timerSecondsRemaining: Int,
        isTimerActive: Boolean
    ) {
        _dataUiState.update {
            it?.copy(
                timerSecondsRemaining = if (isTimerActive) timerSecondsRemaining else it.timerSecondsRemaining,
                isTimerActive = isTimerActive,
                bottomLeftButtonId = if (isTimerActive) pauseIconId else playIconId,
                bottomLeftButtonDescription = if (isTimerActive) pauseIconDescription else playIconDescription,
                circularSliderColor = if (isTimerActive) activeColor else inactiveColor
            )
        }
    }

    fun setNextTimer(context: Context, currentTimerIndex: Int, currentRepetition: Int) {
        customWorkoutModel?.let {

            val totalTimers = it.timers.size
            val totalRepetitions = it.repetition
            val newCurrentTimerIndex: Int
            val newCurrentRepetition: Int
            if (currentTimerIndex == totalTimers - 1) {
                if (currentRepetition == totalRepetitions) {
                    _navUiState.value = NavUiState.GoToEndOfWorkout
                    return
                } else {
                    newCurrentTimerIndex = 0
                    newCurrentRepetition = currentRepetition + 1
                }
            } else {
                newCurrentTimerIndex = currentTimerIndex + 1
                newCurrentRepetition = currentRepetition
            }

            _dataUiState.update { state ->
                customWorkoutModel?.let { workout ->
                    val newTimer = workout.timers[newCurrentTimerIndex]
                    state?.copy(
                        currentTimerId = newCurrentTimerIndex,
                        currentTimerName = newTimer.name,
                        currentRepetition = newCurrentRepetition,
                        isTimerActive = true,
                        maxTimerSeconds = newTimer.duration.toSeconds(),
                        timerSecondsRemaining = newTimer.duration.toSeconds(),
                        circularSliderColor = activeColor,
                        circularSliderProgress = 1.0,
                        bottomLeftButtonId = pauseIconId,
                        bottomLeftButtonDescription = pauseIconDescription
                    )
                }
            }

            PrefsUtils.setNextTimerInPrefs(
                context = context,
                newCurrentTimerIndex = newCurrentTimerIndex,
                newCurrentRepetition = newCurrentRepetition
            )
        }
    }

    fun cancelTimer(context: Context) {
        PrefsUtils.cancelTimerInPrefs(context)
    }

    private fun getCustomWorkoutModels(context: Context): List<CustomWorkoutModel>? {
        val customWorkoutsRawModels =
            PrefsUtils.getStringPref(context, PrefParam.CustomWorkoutList.value)
        val sType = object : TypeToken<List<CustomWorkoutModel>>() {}.type
        val customWorkoutModels: List<CustomWorkoutModel>? =
            Gson().fromJson<List<CustomWorkoutModel>?>(customWorkoutsRawModels, sType)
        return customWorkoutModels
    }

    fun updateMiddleLabelValue(currentTimerSecondsRemaining: Int) {
        val minutesRemaining = (currentTimerSecondsRemaining / 60)
        val secondsRemaining = (currentTimerSecondsRemaining % 60)

        val minutesString = if (minutesRemaining < 10) "0$minutesRemaining" else "$minutesRemaining"
        val secondsString = if (secondsRemaining < 10) "0$secondsRemaining" else "$secondsRemaining"

        _dataUiState.update {
            it?.copy(
                middleTimerStatusValueText = "$minutesString:$secondsString"
            )
        }
    }

}