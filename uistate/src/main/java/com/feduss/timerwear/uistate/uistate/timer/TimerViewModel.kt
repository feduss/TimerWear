package com.feduss.timerwear.uistate.uistate.timer

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.feduss.timerwear.entity.CustomTimerModel
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.entity.enums.Consts
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.entity.enums.WorkoutType
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
import kotlin.math.truncate

class TimerViewModel @AssistedInject constructor(
    @Assisted("workoutId") val workoutId: Int,
    @Assisted("workoutType") val workoutType: WorkoutType,
    @Assisted("currentTimerIndex") val currentTimerIndex: Int?,
    @Assisted("currentRepetition") val currentRepetition: Int?,
    @Assisted("currentTimerSecondsRemaining") val currentTimerSecondsRemaining: Double?
) : ViewModel() {

    //Factory
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("workoutId") workoutId: Int,
            @Assisted("workoutType") workoutType: WorkoutType,
            @Assisted("currentTimerIndex") currentTimerIndex: Int?,
            @Assisted("currentRepetition") currentRepetition: Int?,
            @Assisted("currentTimerSecondsRemaining") currentTimerSecondsRemaining: Double?
        ): TimerViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            workoutId: Int,
            workoutType: WorkoutType,
            currentTimerIndex: Int?,
            currentRepetition: Int?,
            currentTimerSecondsRemaining: Double?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(
                    workoutId,
                    workoutType,
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
            val timerSecondsRemaining: Double,
            val isTimerActive: Boolean,
            val completion: () -> Unit
        ): NavUiState()
    }

    private var _dataUiState = MutableStateFlow<TimerUiState?>(null)
    var dataUiState = _dataUiState.asStateFlow()

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    private var customWorkoutModel: CustomWorkoutModel? = null

    var isSoundEnabled: Boolean = false
    var backgroundModeEnterMillsTimestamp: Long = 0L
    var backgroundModeEnterTimerUUID: UUID? = null
    var isTimerActivePreBackground = false

    //Copies
    private val ambientCountdownTextId = R.string.ambient_countdown_text
    private val alertDialogSkipTitleId = R.string.timer_skip_timer
    private val alertDialogStopTitleId = R.string.timer_stop_timer_question
    private val typTitleId = R.string.timer_typ_title

    //Assets
    private val playIconId = R.drawable.ic_play
    private val playIconDescription = "ic_play"
    private val pauseIconId = R.drawable.ic_pause
    private val pauseIconDescription = "ic_pause"
    private val runIconId = R.drawable.ic_run
    private val runIconDescription = "ic_run"
    private val skipNextIcon = R.drawable.ic_skip_next
    private val skipNextIconDescription = "ic_skip_next"
    private val closeIconId = R.drawable.ic_close
    private val checkIconId = R.drawable.ic_check
    private val typImageId = R.drawable.ic_win
    private val typImageDescription = "ic_win"
    private val countdownIconId = R.drawable.ic_restore
    private val countdownIconDescription = "ic_restore"

    //Colors
    private val activeColorWork = Color.ActiveTimer
    private val activeColorRest = Color.Blue500
    private val activeColorIntermediumRest = Color.Indigo500
    private val inactiveColor = Color.InactiveTimer

    //UiState
    fun loadTimerCountdownUiState(context: Context) {

        isSoundEnabled = PrefsUtils.getSoundPreference(context)

        if (_dataUiState.value != null) return

        customWorkoutModel = TimerUtils.getCustomWorkoutModels(
            context = context,
            workoutType = workoutType
        )?.first { it.id == workoutId }

        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.WorkoutType.value,
            newValue = workoutType.toString()
        )

        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.CurrentWorkoutId.value,
            newValue = workoutId.toString()
        )

        val resumedFromBackground = PrefsUtils.isTimerActive(context = context)
        if (resumedFromBackground) {
            _dataUiState.value = TimerUiState()
            loadTimerUiState(
                context = context,
                resumedFromBackground = true
            )
        } else {
            _dataUiState.value = TimerUiState(
                timerViewUiState = null,
                alertDialogUiState = null,
                timerCountdownUiState = TimerCountdownUiState(
                    workoutName = customWorkoutModel?.name ?: "",
                    countdown = Consts.CountdownTimerSeconds.value.toDouble(),
                    ambientCountDownTextId = ambientCountdownTextId,
                    ambientCountDownIconId = countdownIconId,
                    ambientCountDownIconDescription = countdownIconDescription,
                    isTimerActive = true
                ),
                timerTYPViewUiState = null
            )

            PrefsUtils.setStringPref(
                context = context,
                pref = PrefParam.IsCountdownTimerActive.value,
                newValue = true.toString()
            )
        }
    }

    fun loadTimerUiState(context: Context, resumedFromBackground: Boolean = false) {
        customWorkoutModel?.let { workout ->
            val timerIndex = currentTimerIndex ?: 0
            val repetition = currentRepetition ?: 0

            initTimerUiState(
                workout = workout,
                timerIndex = timerIndex,
                repetition = repetition,
                resumedFromBackground = resumedFromBackground,
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
        resumedFromBackground: Boolean,
        context: Context
    ) {
        val currentTimer = workout.timers[currentTimerIndex ?: 0]

        val needsToShowIntermediumRest = TimerUtils.needsToDisplayIntermediumRest(
            timer = currentTimer,
            repetition = repetition,
            totalRepetitions = workout.repetition,
            frequency = workout.intermediumRestFrequency
        )
        if (!needsToShowIntermediumRest && currentTimer.type == TimerType.IntermediumRest) {
            initTimerUiState(
                workout = workout,
                timerIndex = timerIndex,
                repetition = repetition,
                resumedFromBackground = resumedFromBackground,
                context = context
            )
            return
        }

        val timerDuration = currentTimer.duration.toSeconds().toDouble()
        _dataUiState.update {
            it?.copy(
                timerCountdownUiState = null,
                timerViewUiState = TimerViewUiState(
                    customWorkoutModel = workout,
                    uuid = currentTimer.uuid,
                    currentTimerId = timerIndex,
                    currentRepetition = repetition,
                    currentProgress = getCurrentProgress(
                        timers = workout.timers,
                        totalRepetition = workout.repetition,
                        currentRepetition = repetition,
                        currentTimerIndex = timerIndex,
                        needsToShowIntermediumRest = needsToShowIntermediumRest
                    ),
                    middleTimerStatusValueText = currentTimer.duration.toString(),
                    isTimerActive = true,
                    maxTimerSeconds = timerDuration,
                    timerSecondsRemaining = currentTimerSecondsRemaining
                        ?: timerDuration,
                    timerType = currentTimer.type,
                    bottomLeftButtonId = pauseIconId,
                    bottomLeftButtonDescription = pauseIconDescription,
                    bottomRightButtonId = skipNextIcon,
                    bottomRightButtonDescription = skipNextIconDescription,
                    circularSliderColor = getSliderColor(
                        isTimerActive = true,
                        timerType = currentTimer.type
                    ),
                    ambientIconId = playIconId,
                    ambientIconDescription = playIconDescription,
                    resumedFromBackGround = resumedFromBackground,
                    timeText = currentTimer.name
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

    private fun getSliderColor(isTimerActive: Boolean, timerType: TimerType): Color {
        return if (isTimerActive) {
            when(timerType) {
                TimerType.Work -> activeColorWork
                TimerType.Rest -> activeColorRest
                TimerType.IntermediumRest -> activeColorIntermediumRest
            }
        } else {
            inactiveColor
        }
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

    fun updateMiddleLabelValue(currentTimerSecondsRemaining: Double) {
        val minutesRemaining: Int = (currentTimerSecondsRemaining / 60).toInt()
        val secondsRemaining: Int = (currentTimerSecondsRemaining % 60).toInt()
        val fractionRemaining: Int = ((currentTimerSecondsRemaining - truncate(currentTimerSecondsRemaining)) * 100).toInt()

        val minutesString = if (minutesRemaining < 10) "0$minutesRemaining" else "$minutesRemaining"
        val secondsString = if (secondsRemaining < 10) "0$secondsRemaining" else "$secondsRemaining"
        val fractionRemainingString = if (fractionRemaining < 10) "0$fractionRemaining" else "$fractionRemaining"

        _dataUiState.update {
            it?.copy(
                timerViewUiState = it.timerViewUiState?.copy(
                    middleTimerStatusValueText = "$minutesString:$secondsString:$fractionRemainingString"
                )
            )
        }
    }

    // User action
    fun countdownFinished(context: Context) {
        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.IsCountdownTimerActive.value,
            newValue = false.toString()
        )
        _navUiState.value = NavUiState.TimerStarted
    }

    fun userGoToNextTimer(
        currentTimerIndex: Int,
        currentRepetition: Int
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

    fun setNextTimer(
        context: Context,
        currentTimerIndex: Int,
        currentRepetition: Int
    ) {
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

            handleNewTimer(
                context = context,
                newCurrentTimerIndex = newCurrentTimerIndex,
                newCurrentRepetition = newCurrentRepetition
            )
        }
    }

    private fun handleNewTimer(
        context: Context,
        newCurrentTimerIndex: Int,
        newCurrentRepetition: Int,
        newCurrentDuration: Double? = null
    ) {
        customWorkoutModel?.let {

            val totalRepetitions = it.repetition

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
            if (!needsToShowIntermediumRest && newTimer.type == TimerType.IntermediumRest) {
                setNextTimer(
                    context = context,
                    currentTimerIndex = newCurrentTimerIndex,
                    currentRepetition = newCurrentRepetition
                )
                return
            }

            val newTimerDuration = newTimer.duration.toSeconds().toDouble()
            _dataUiState.update { state ->
                customWorkoutModel?.let { workout ->
                    state?.copy(
                        timerViewUiState = state.timerViewUiState?.copy(
                            uuid = newTimer.uuid,
                            currentTimerId = newTimer.id,
                            currentRepetition = newCurrentRepetition,
                            isTimerActive = true,
                            maxTimerSeconds = newTimerDuration,
                            timerSecondsRemaining = newCurrentDuration ?: newTimerDuration,
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
                            ambientIconId = playIconId,
                            ambientIconDescription = playIconDescription,
                            resumedFromBackGround = false,
                            timeText = newTimer.name
                        )
                    )
                }
            }

            PrefsUtils.setNextTimerInPrefs(
                context = context,
                newCurrentTimerIndex = newCurrentTimerIndex,
                newCurrentRepetition = newCurrentRepetition,
                newCurrentTimerSecondsRemaining = newTimer.duration.toSeconds()
            )

            saveIsTimerActivePref(
                context = context,
                isTimerActive = true
            )
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
            val totalTimers = timers.filter { it.type != TimerType.IntermediumRest }.size
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
        timerSecondsRemaining: Double,
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
        timerSecondsRemaining: Double,
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
                    ambientIconId = if (isTimerActive) playIconId else pauseIconId,
                    ambientIconDescription = if (isTimerActive) playIconDescription else pauseIconDescription,
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
        PrefsUtils.cancelTimerInPrefs(context)
    }

    fun navStateFired() {
        _navUiState.value = null
    }

    // Prefs utils

    private fun saveIsTimerActivePref(context: Context, isTimerActive: Boolean) {
        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.IsTimerActive.value,
            newValue = if (isTimerActive) "true" else "false"
        )
    }

    fun saveCountdownData(context: Context, currentTimerSecondsRemaining: Double) {
        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.CurrentTimerSecondsRemaining.value,
            newValue = currentTimerSecondsRemaining.toString()
        )

        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.CurrentTimerName.value,
            newValue = context.getString(ambientCountdownTextId)
        )
    }

    fun saveCurrentTimerData(
        context: Context,
        currentTimerId: Int,
        currentRepetition: Int?,
        currentTimerSecondsRemaining: Double
    ) {

        PrefsUtils.setStringPref(
            context = context,
            pref = PrefParam.CurrentTimerIndex.value,
            newValue = currentTimerId.toString()
        )

        val currentTimerName: String = customWorkoutModel?.timers?.get(currentTimerId)?.name ?: ""
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

    fun cancelTimer(context: Context) {
        _dataUiState
        PrefsUtils.cancelTimerInPrefs(context)
    }

    fun isAmbientModeEnabled(context: Context): Boolean {
        return PrefsUtils.getAmbientModeState(context)
    }

    //

    private fun getTimeText(
        context: Context,
        timers: List<CustomTimerModel>,
        currentTimerIndex: Int,
        currentRepetition: Int,
        totalRepetitions: Int,
        intermediumRestFrequency: Int?
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
            val totalTimers = timers.filter { it.type != TimerType.IntermediumRest }.size
            val nextTimer = timers[nextTimerIndex]

            val needsToDisplayIntermediumRest = TimerUtils.needsToDisplayIntermediumRest(
                timer = nextTimer,
                repetition = nextRepetition,
                totalRepetitions = totalRepetitions,
                frequency = intermediumRestFrequency
            )

            if (!needsToDisplayIntermediumRest && nextTimer.type == TimerType.IntermediumRest) {
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

    fun updateCountdown(currentTimerSecondsRemaining: Double) {
        _dataUiState.update {
            it?.copy(
                timerCountdownUiState = it.timerCountdownUiState?.copy(
                    countdown = currentTimerSecondsRemaining
                )
            )
        }
    }

    fun setNextTimerTimeText(context: Context) {
        customWorkoutModel?.let { workout ->
            _dataUiState.update {
                it?.copy(
                    timerViewUiState = it.timerViewUiState?.copy(
                        timeText = getTimeText(
                            context = context,
                            timers = workout.timers,
                            currentTimerIndex = it.timerViewUiState.currentTimerId,
                            currentRepetition = it.timerViewUiState.currentRepetition,
                            totalRepetitions = workout.repetition,
                            intermediumRestFrequency = workout.intermediumRestFrequency
                        )
                    )
                )
            }
        }
    }

    fun saveAmbientModeState(context: Context, isEnabled: Boolean) {
        PrefsUtils.saveAmbientModeState(
            context = context,
            isEnabled = isEnabled
        )
    }

    fun getBackgroundAlarmType(context: Context): BackgroundAlarmType {
        val currentTimerIndex = PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.CurrentTimerIndex.value
        )?.toIntOrNull()

        val currentRepetition = PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.CurrentRepetition.value
        )?.toIntOrNull()

        return if (currentTimerIndex == null || currentRepetition == null) {
            BackgroundAlarmType.CountdownTimer
        } else {
            BackgroundAlarmType.ActiveTimer
        }
    }

    fun onEnterBackgroundMode(
        context: Context,
        backgroundAlarmType: BackgroundAlarmType,
        isAmbientMode: Boolean
    ) {
        saveAmbientModeState(
            context = context,
            isEnabled = isAmbientMode
        )
        backgroundModeEnterMillsTimestamp = System.currentTimeMillis()

        when(backgroundAlarmType) {
            BackgroundAlarmType.CountdownTimer -> {
                _dataUiState.update {
                    it?.copy(
                        timerCountdownUiState = it.timerCountdownUiState?.copy(
                            isTimerActive = false
                        )
                    )
                }
            }
            BackgroundAlarmType.ActiveTimer -> {
                val timerViewUiState = _dataUiState.value?.timerViewUiState

                if (timerViewUiState == null) return

                val currentTimer = timerViewUiState.customWorkoutModel.timers[timerViewUiState.currentTimerId]

                backgroundModeEnterTimerUUID = timerViewUiState.uuid

                val newMiddleText = if (isAmbientMode) {
                    currentTimer.name
                } else {
                    timerViewUiState.middleTimerStatusValueText
                }

                _dataUiState.update {
                    isTimerActivePreBackground = it?.timerViewUiState?.isTimerActive == true
                    return@update it?.copy(
                        timerViewUiState = it.timerViewUiState?.copy(
                            middleTimerStatusValueText = newMiddleText,
                            isTimerActive = false
                        )
                    )
                }
            }
        }
    }

    fun onExitBackgroundMode(
        context: Context,
        backgroundAlarmType: BackgroundAlarmType
    ) {

        saveAmbientModeState(
            context = context,
            isEnabled = false
        )

        when(backgroundAlarmType) {
            //Restore countdown state when the user put the app on foreground or exit from ambient
            BackgroundAlarmType.CountdownTimer -> {
                val timerCountdownUiState = _dataUiState.value?.timerCountdownUiState

                if (timerCountdownUiState == null) return

                val currentMillisecondsTimestamp = System.currentTimeMillis()
                val timeElapsed = (currentMillisecondsTimestamp - backgroundModeEnterMillsTimestamp) / 1000
                var newCountdown = timerCountdownUiState.countdown - timeElapsed

                _dataUiState.update {
                    it?.copy(
                        timerCountdownUiState = it.timerCountdownUiState?.copy(
                            countdown = newCountdown,
                            isTimerActive = true
                        )
                    )
                }
            }
            //Restore active timer state or set the new one
            //when the user put the app on foreground or exit from ambient
            BackgroundAlarmType.ActiveTimer -> {
                val savedTimerSecondsRemaining = PrefsUtils.getStringPref(
                    context = context,
                    pref = PrefParam.CurrentTimerSecondsRemaining.value
                )?.toDoubleOrNull()

                val currentTimerIndex = PrefsUtils.getStringPref(
                    context = context,
                    pref = PrefParam.CurrentTimerIndex.value
                )?.toIntOrNull()

                val currentRepetition = PrefsUtils.getStringPref(
                    context = context,
                    pref = PrefParam.CurrentRepetition.value
                )?.toIntOrNull()

                val timerViewUiState = _dataUiState.value?.timerViewUiState

                if (
                    savedTimerSecondsRemaining == null ||
                    currentTimerIndex == null ||
                    currentRepetition == null
                ) {
                    return
                }

                //If the user switches the app on background during countdown
                //and resume it during an active timer
                //we have to init timerViewUiState and then switch to new timer
                if (timerViewUiState == null) {
                    loadTimerUiState(context)
                }

                if (backgroundModeEnterTimerUUID == null || backgroundModeEnterTimerUUID != timerViewUiState?.uuid) {
                    val timerActiveAlarmSetTime = PrefsUtils.getStringPref(
                        context = context,
                        pref = PrefParam.TimerActiveAlarmSetTime.value
                    )?.toLongOrNull()

                    if (timerActiveAlarmSetTime == null) return

                    val currentMillisecondsTimestamp = System.currentTimeMillis()
                    val timeElapsed = (currentMillisecondsTimestamp - timerActiveAlarmSetTime) / 1000.0
                    var newTimerSecondsRemaining = savedTimerSecondsRemaining - timeElapsed

                    handleNewTimer(
                        context = context,
                        newCurrentTimerIndex = currentTimerIndex,
                        newCurrentRepetition = currentRepetition,
                        newCurrentDuration = newTimerSecondsRemaining
                    )
                } else {
                    val currentMillisecondsTimestamp = System.currentTimeMillis()
                    val timeElapsed = (currentMillisecondsTimestamp - backgroundModeEnterMillsTimestamp) / 1000
                    var newTimerSecondsRemaining = savedTimerSecondsRemaining - timeElapsed

                    val timerSecondsRemaining = if (isTimerActivePreBackground) {
                        newTimerSecondsRemaining
                    } else {
                        timerViewUiState?.timerSecondsRemaining ?: 0.0
                    }

                    backgroundModeEnterMillsTimestamp = 0L
                    backgroundModeEnterTimerUUID = null

                    val timerName = timerViewUiState?.customWorkoutModel?.timers[currentTimerIndex]?.name ?: ""

                    _dataUiState.update {
                        return@update it?.copy(
                            timerCountdownUiState = null,
                            timerViewUiState = it.timerViewUiState?.copy(
                                middleTimerStatusValueText = timerName,
                                isTimerActive = isTimerActivePreBackground,
                                timerSecondsRemaining = timerSecondsRemaining
                            )
                        )
                    }
                    updateMiddleLabelValue(timerSecondsRemaining)
                }
            }
        }
    }

    fun onRefreshAmbientMode(context: Context) {

        val timerViewUiState = _dataUiState.value?.timerViewUiState

        //If the user switches the app on ambient during countdown
        //and the timer expires
        //we have to init timerViewUiState and then switch to new timer
        if (timerViewUiState == null) {
            loadTimerUiState(context)
        }

        val newCurrentTimerIndex = PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.CurrentTimerIndex.value
        )?.toIntOrNull()

        val newCurrentRepetition = PrefsUtils.getStringPref(
            context = context,
            pref = PrefParam.CurrentRepetition.value
        )?.toIntOrNull()

        if (
            newCurrentTimerIndex == null ||
            newCurrentRepetition == null ||
            timerViewUiState == null
        ) {
            return
        }

        handleNewTimer(
            context = context,
            newCurrentTimerIndex = newCurrentTimerIndex,
            newCurrentRepetition = newCurrentRepetition
        )

        //If the workout isn't ended
        if (
            newCurrentTimerIndex != -1 &&
            newCurrentRepetition != -1
        ) {

            backgroundModeEnterTimerUUID = timerViewUiState.uuid

            val newTimer = timerViewUiState.customWorkoutModel.timers[newCurrentTimerIndex]

            _dataUiState.update {
                return@update it?.copy(
                    timerCountdownUiState = null,
                    timerViewUiState = it.timerViewUiState?.copy(
                        middleTimerStatusValueText = newTimer.name,
                        isTimerActive = false
                    )
                )
            }
        }
    }
}