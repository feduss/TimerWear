package com.feduss.timerwear.uistate.uistate.timer

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.feduss.timerwear.entity.CustomTimerModel
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.entity.enums.CustomTimerType
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.ActiveTimer
import com.feduss.timerwear.uistate.extension.Blue500
import com.feduss.timerwear.uistate.extension.InactiveTimer
import com.feduss.timerwear.uistate.extension.Indigo500
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import com.feduss.timerwear.utils.TimerUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class TimerViewModel @AssistedInject constructor(
    @Assisted("workoutId") val workoutId: Int,
    @Assisted("timerType") val timerType: TimerType,
    @Assisted("currentTimerIndex") val currentTimerIndex: Int?,
    @Assisted("currentRepetition") val currentRepetition: Int?,
    @Assisted("currentTimerSecondsRemaining") val currentTimerSecondsRemaining: Int?
) : ViewModel() {

    //Factory
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("workoutId") workoutId: Int,
            @Assisted("timerType") timerType: TimerType,
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
            timerType: TimerType,
            currentTimerIndex: Int?,
            currentRepetition: Int?,
            currentTimerSecondsRemaining: Int?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(
                    workoutId,
                    timerType,
                    currentTimerIndex,
                    currentRepetition,
                    currentTimerSecondsRemaining
                ) as T
            }
        }
    }

    sealed class NavUiState {
        data object TimerStarted: NavUiState()
        data object GoToEndOfWorkout: NavUiState()
        data object GoBackToCustomWorkoutList: NavUiState()
        data class GoToNextTimer(
            val currentTimerIndex: Int,
            val currentRepetition: Int
        ): NavUiState()
        data class SkipToNextTimer(
            val currentTimerIndex: Int,
            val currentRepetition: Int
        ): NavUiState()
        data class ChangeAlertDialogState(
            val isAlertDialogVisible: Boolean,
            val alertDialogType: AlertDialogType?,
            val completion: () -> Unit
        ): NavUiState()
        data class ChangeTimerState(
            val timerSecondsRemaining: Int,
            val isTimerActive: Boolean,
            val completion: () -> Unit
        ): NavUiState()
    }

    private var _dataUiState = MutableStateFlow<TimerUiState?>(null)
    var dataUiState = _dataUiState.asStateFlow()

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    private var customWorkoutModel: CustomWorkoutModel? = null

    //Copies
    private val preCountdownTextId = R.string.timer_pre_countdown
    private val postCountdownTextId = R.string.timer_post_countdown
    private val alertDialogSkipTitleId = R.string.timer_skip_timer
    private val alertDialogStopTitleId = R.string.timer_stop_timer_question
    private val typTitleId = R.string.timer_typ_title
    private val checkboxTextId = R.string.timer_checkbox_text

    //Assets
    private val playIconId = R.drawable.ic_play
    private val playIconDescription = "ic_play"
    private val pauseIconId = R.drawable.ic_pause
    private val pauseIconDescription = "ic_pause"
    private val skipNextIcon = R.drawable.ic_skip_next
    private val skipNextIconDescription = "ic_skip_next"
    private val closeIconId = R.drawable.ic_close
    private val checkIconId = R.drawable.ic_check
    private val typImageId = R.drawable.ic_win;
    private val typImageDescription = "ic_win"

    //Colors
    private val activeColorWork = Color.ActiveTimer
    private val activeColorRest = Color.Blue500
    private val activeColorIntermediumRest = Color.Indigo500
    private val inactiveColor = Color.InactiveTimer

    //State
    fun loadTimerCountdownUiState(context: Context) {

        if (_dataUiState.value != null) return

        customWorkoutModel = TimerUtils.getCustomWorkoutModels(context = context)?.first { it.id == workoutId }

        if (PrefsUtils.isTimerActive(context = context)) {
            _dataUiState.value = TimerUiState()
            loadTimerUiState(context = context)
        } else {
            _dataUiState.value = TimerUiState(
                timerViewUiState = null,
                alertDialogUiState = null,
                timerCountdownUiState = TimerCountdownUiState(
                    workoutName = customWorkoutModel?.name ?: "",
                    preCountdownTextId = preCountdownTextId,
                    preCountdownDuration = 1,
                    countdown = 3,
                    postCountdownTextId = postCountdownTextId,
                    postCountdownSeconds = 1
                ),
                timerTYPViewUiState = null
            )
        }
    }

    fun loadTimerUiState(context: Context) {
        customWorkoutModel?.let { workout ->
            val timerIndex = currentTimerIndex ?: 0
            val repetition = currentRepetition ?: 0

            initTimerUiState(
                workout = workout,
                timerIndex = timerIndex,
                repetition = repetition,
                context = context
            )
        }

        saveIsTimerActivePref(
            context = context,
            isTimerActive = true
        )
    }

    private fun initTimerUiState(
        workout: CustomWorkoutModel,
        timerIndex: Int,
        repetition: Int,
        context: Context
    ) {
        val currentTimer = workout.timers[currentTimerIndex ?: 0]

        val needsToShowIntermediumRest = TimerUtils.needsToDisplayIntermediumRest(
            timer = currentTimer,
            repetition = repetition,
            totalRepetitions = workout.repetition,
            frequency = workout.intermediumRestFrequency
        )
        if (!needsToShowIntermediumRest && currentTimer.type == CustomTimerType.IntermediumRest) {
            initTimerUiState(
                workout = workout,
                timerIndex = timerIndex,
                repetition = repetition,
                context = context
            )
            return
        }

        _dataUiState.update {
            it?.copy(
                timerCountdownUiState = null,
                timerViewUiState = TimerViewUiState(
                    customWorkoutModel = workout,
                    uuid = currentTimer.uuid,
                    currentTimerId = timerIndex,
                    currentTimerName = currentTimer.name,
                    currentRepetition = repetition,
                    currentProgress = getCurrentProgress(
                        timers = workout.timers,
                        totalRepetition = workout.repetition,
                        currentRepetition = repetition,
                        currentTimerIndex = timerIndex,
                        needsToShowIntermediumRest = needsToShowIntermediumRest
                    ),
                    middleTimerStatusValueText = currentTimer.duration.toString(),
                    checkboxTextId = checkboxTextId,
                    isCheckboxSelected = getKeepScreenOnPref(context),
                    isTimerActive = true,
                    maxTimerSeconds = currentTimer.duration.toSeconds(),
                    timerSecondsRemaining = currentTimerSecondsRemaining
                        ?: currentTimer.duration.toSeconds(),
                    timerType = currentTimer.type,
                    bottomLeftButtonId = pauseIconId,
                    bottomLeftButtonDescription = pauseIconDescription,
                    bottomRightButtonId = skipNextIcon,
                    bottomRightButtonDescription = skipNextIconDescription,
                    circularSliderColor = getSliderColor(
                        isTimerActive = true,
                        timerType = currentTimer.type
                    ),
                    timeText = getTimeText(
                        context = context,
                        timers = workout.timers,
                        currentTimerIndex = timerIndex,
                        currentRepetition = repetition,
                        totalRepetitions = workout.repetition,
                        intermediumRestFrequency = workout.intermediumRestFrequency
                    )
                ),
                alertDialogUiState = TimerAlertDialogUiState(
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
                    alertDialogType = AlertDialogType.StopTimer
                )
            )
        }
    }

    private fun getSliderColor(isTimerActive: Boolean, timerType: CustomTimerType): Color {
        return if (isTimerActive) {
            when(timerType) {
                CustomTimerType.Work -> activeColorWork
                CustomTimerType.Rest -> activeColorRest
                CustomTimerType.IntermediumRest -> activeColorIntermediumRest
            }
        } else {
            inactiveColor
        }
    }

    // User action
    fun countdownFinished() {
        _navUiState.value = NavUiState.TimerStarted
    }

    fun userGoToNextTimer(
        currentTimerIndex: Int,
        currentRepetition: Int,
    ) {
        _navUiState.value = NavUiState.GoToNextTimer(
            currentTimerIndex = currentTimerIndex,
            currentRepetition = currentRepetition
        )
    }

    fun userSkipToNextTimer(
        currentTimerIndex: Int,
        currentRepetition: Int,
    ) {
        _navUiState.value = NavUiState.SkipToNextTimer(
            currentTimerIndex = currentTimerIndex,
            currentRepetition = currentRepetition
        )
    }

    fun setNextTimer(context: Context, currentTimerIndex: Int, currentRepetition: Int) {
        customWorkoutModel?.let {

            val totalTimers = it.timers.size
            val totalRepetitions = it.repetition
            val newCurrentTimerIndex: Int
            val newCurrentRepetition: Int
            val pair = TimerUtils.getNextTimerIndexAndRepetition(
                totalTimers = totalTimers,
                totalRepetitions = totalRepetitions,
                currentTimerIndex = currentTimerIndex,
                currentRepetition = currentRepetition
            )
            newCurrentTimerIndex = pair.first
            newCurrentRepetition = pair.second

            if (newCurrentTimerIndex == -1 && newCurrentRepetition == -1) {
                _navUiState.value = NavUiState.GoToEndOfWorkout
                return
            }

            val newTimer = it.timers[newCurrentTimerIndex].copy(
                uuid = UUID.randomUUID()
            )
            val needsToShowIntermediumRest = TimerUtils.needsToDisplayIntermediumRest(
                timer = newTimer,
                repetition = newCurrentRepetition,
                totalRepetitions = totalRepetitions,
                frequency = it.intermediumRestFrequency
            )
            if (!needsToShowIntermediumRest && newTimer.type == CustomTimerType.IntermediumRest) {
                setNextTimer(
                    context = context,
                    currentTimerIndex = newCurrentTimerIndex,
                    currentRepetition = newCurrentRepetition
                )
                return
            }

            _dataUiState.update { state ->
                customWorkoutModel?.let { workout ->
                    state?.copy(
                        timerViewUiState = state.timerViewUiState?.copy(
                            uuid = newTimer.uuid,
                            currentTimerId = newTimer.id,
                            currentTimerName = newTimer.name,
                            currentRepetition = newCurrentRepetition,
                            isTimerActive = true,
                            maxTimerSeconds = newTimer.duration.toSeconds(),
                            timerSecondsRemaining = newTimer.duration.toSeconds(),
                            timerType = newTimer.type,
                            circularSliderColor = getSliderColor(
                                isTimerActive = true,
                                timerType = newTimer.type
                            ),
                            circularSliderProgress = 1.0,
                            currentProgress = getCurrentProgress(
                                timers = workout.timers,
                                totalRepetition = workout.repetition,
                                currentRepetition = newCurrentRepetition,
                                currentTimerIndex = newCurrentTimerIndex,
                                needsToShowIntermediumRest = needsToShowIntermediumRest
                            ),
                            bottomLeftButtonId = pauseIconId,
                            bottomLeftButtonDescription = pauseIconDescription,
                            timeText = getTimeText(
                                context = context,
                                timers = it.timers,
                                currentTimerIndex = newCurrentTimerIndex,
                                currentRepetition = newCurrentRepetition,
                                totalRepetitions = totalRepetitions,
                                intermediumRestFrequency = it.intermediumRestFrequency
                            )
                        )
                    )
                }
            }

            if (!PrefsUtils.isAppInBackground(context)) {
                //Log.e("TEST123: ", "app non in background: newCurrentTimerIndex $newCurrentTimerIndex, newCurrentRepetition: $newCurrentRepetition")
                PrefsUtils.setNextTimerInPrefs(
                    context = context,
                    newCurrentTimerIndex = newCurrentTimerIndex,
                    newCurrentRepetition = newCurrentRepetition
                )
            }
        }
    }

    private fun getCurrentProgress(
        timers: List<CustomTimerModel>,
        totalRepetition: Int,
        currentRepetition: Int,
        currentTimerIndex: Int,
        needsToShowIntermediumRest: Boolean
    ): String {
        return if (needsToShowIntermediumRest) "" else {
            val totalTimers = timers.filter { it.type != CustomTimerType.IntermediumRest }.size
            "${(currentRepetition * totalTimers) + (currentTimerIndex + 1)}/${totalTimers * totalRepetition}"
        }
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
                alertDialogUiState = it.alertDialogUiState?.copy(
                    isAlertDialogVisible = isAlertDialogVisible,
                    alertDialogType = alertDialogType
                )
            )
        }
        completion()
    }

    fun userGoBackToWorkoutList() {
        _navUiState.value = NavUiState.GoBackToCustomWorkoutList
    }

    fun userChangedTimerState(
        timerSecondsRemaining: Int,
        isTimerActive: Boolean,
        completion: () -> Unit
    ) {
        _navUiState.value = NavUiState.ChangeTimerState(
            timerSecondsRemaining = timerSecondsRemaining,
            isTimerActive = isTimerActive,
            completion = completion
        )
    }

    fun setTimerState(
        context: Context,
        timerSecondsRemaining: Int,
        isTimerActive: Boolean,
        completion: () -> Unit
    ) {
        _dataUiState.update {
            it?.copy(
                timerViewUiState = it.timerViewUiState?.copy(
                    timerSecondsRemaining = timerSecondsRemaining,
                    isTimerActive = isTimerActive,
                    bottomLeftButtonId = if (isTimerActive) pauseIconId else playIconId,
                    bottomLeftButtonDescription = if (isTimerActive) pauseIconDescription else playIconDescription,
                    circularSliderColor = getSliderColor(
                        isTimerActive = isTimerActive,
                        timerType = it.timerViewUiState.timerType
                    )
                )
            )
        }
        saveIsTimerActivePref(
            context = context,
            isTimerActive = isTimerActive
        )

        completion()
    }

    private fun saveIsTimerActivePref(context: Context, isTimerActive: Boolean) {
        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.IsTimerActive.value,
            newValue = if (isTimerActive) "true" else "false"
        )

        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.TimerType.value,
            newValue = timerType.toString()
        )

        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.CurrentWorkoutId.value,
            newValue = workoutId.toString()
        )
    }

    fun setTYPState(context: Context) {
        _dataUiState.update {
            it?.copy(
                timerViewUiState = null,
                alertDialogUiState = null,
                timerTYPViewUiState = TimerTYPViewUiState(
                    imageId = typImageId,
                    imageDescription = typImageDescription,
                    titleId = typTitleId
                )
            )
        }
        saveIsTimerActivePref(
            context = context,
            isTimerActive = false
        )
    }

    fun updateCircularProgressBarProgress(progress: Double) {
        _dataUiState.update {
            it?.copy(
                timerViewUiState = it.timerViewUiState?.copy(
                    circularSliderProgress = progress
                )
            )
        }
    }

    fun navStateFired() {
        _navUiState.value = null
    }

    // Prefs utils

    fun saveCurrentTimerData(
        context: Context, currentTimerId: Int, currentTimerName: String?,
        currentRepetition: Int?, currentTimerSecondsRemaining: Int?
    ) {

        if (!PrefsUtils.isAppInBackground(context)) {
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
    }

    fun cancelTimer(context: Context) {
        _dataUiState
        PrefsUtils.cancelTimerInPrefs(context)
    }

    fun saveKeepScreenOnPref(context: Context, keepScreenOn: Boolean) {
        _dataUiState.update {
            it?.copy(
                timerViewUiState = it.timerViewUiState?.copy(
                    isCheckboxSelected = keepScreenOn
                )
            )
        }

        PrefsUtils.setStringPref(
            context = context,
            PrefParam.KeepScreenOn.value,
            keepScreenOn.toString()
        )
    }

    private fun getKeepScreenOnPref(context: Context): Boolean {
        return PrefsUtils.getStringPref(
            context = context,
            PrefParam.KeepScreenOn.value
        ) == "true"
    }

    //

    private fun getTimeText(
        context: Context,
        timers: List<CustomTimerModel>,
        currentTimerIndex: Int,
        currentRepetition: Int,
        totalRepetitions: Int,
        intermediumRestFrequency: Int
    ): String {

        val pair = TimerUtils.getNextTimerIndexAndRepetition(
            totalTimers = timers.size,
            totalRepetitions = totalRepetitions,
            currentTimerIndex = currentTimerIndex,
            currentRepetition = currentRepetition
        )

        val nextTimerIndex = pair.first
        val nextRepetition = pair.second

        var timeText = "Next: "

        // end of workout
        if (nextTimerIndex == -1 && nextRepetition == -1) {
            timeText += context.getString(R.string.timer_end_timetext_title)
        } else {
            val totalTimers = timers.filter { it.type != CustomTimerType.IntermediumRest }.size
            val nextTimer = timers[nextTimerIndex]

            val needsToDisplayIntermediumRest = TimerUtils.needsToDisplayIntermediumRest(
                timer = nextTimer,
                repetition = nextRepetition,
                totalRepetitions = totalRepetitions,
                frequency = intermediumRestFrequency
            )

            if (!needsToDisplayIntermediumRest && nextTimer.type == CustomTimerType.IntermediumRest) {
                return getTimeText(
                    context = context,
                    timers = timers,
                    currentTimerIndex = nextTimerIndex,
                    currentRepetition = nextRepetition,
                    totalRepetitions = totalRepetitions,
                    intermediumRestFrequency = intermediumRestFrequency
                )
            }

            if (needsToDisplayIntermediumRest) {
                timeText += nextTimer.name
            } else if (nextTimerIndex == totalTimers - 1) {
                if (nextRepetition < totalRepetitions) {
                    timeText += nextTimer.name
                } else {
                    timeText += "Error"
                }
            } else {
                timeText += nextTimer.name
            }
        }

        return timeText
    }

    fun updateMiddleLabelValue(currentTimerSecondsRemaining: Int) {
        val minutesRemaining = (currentTimerSecondsRemaining / 60)
        val secondsRemaining = (currentTimerSecondsRemaining % 60)

        val minutesString = if (minutesRemaining < 10) "0$minutesRemaining" else "$minutesRemaining"
        val secondsString = if (secondsRemaining < 10) "0$secondsRemaining" else "$secondsRemaining"

        _dataUiState.update {
            it?.copy(
                timerViewUiState = it.timerViewUiState?.copy(
                    middleTimerStatusValueText = "$minutesString:$secondsString"
                )
            )
        }
    }
}