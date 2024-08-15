package com.feduss.timerwear.uistate.uistate.add_custom_timer

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.feduss.timerwear.entity.CustomTimerModel
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.TimerPickerModel
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.uistate.GenericButtonCardUiState
import com.feduss.timerwear.uistate.uistate.GenericTextInputUiState
import com.feduss.timerwear.uistate.uistate.TimerPickerInputUiState
import com.feduss.timerwear.uistate.uistate.picker.TimerPickerUiState
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

class AddCustomWorkoutViewModel @AssistedInject constructor(
    @Assisted("workoutType") val workoutType: WorkoutType,
    @Assisted("workoutId") val workoutId: Int?,
) : ViewModel() {

    //DI

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("workoutType") workoutType: WorkoutType,
            @Assisted("workoutId") workoutId: Int?
        ): AddCustomWorkoutViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            workoutType: WorkoutType,
            workoutId: Int?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(
                    workoutType,
                    workoutId
                ) as T
            }
        }
    }

    //

    sealed class NavUiState {
        data object GoBackToCustomWorkoutList: NavUiState()
    }

    private var _dataUiState = MutableStateFlow<AddCustomWorkoutUiState?>(null)
    var dataUiState = _dataUiState.asStateFlow()

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    private var _timerPickerUiState = MutableStateFlow<TimerPickerUiState?>(null)
    val timerPickerUiState = _timerPickerUiState.asStateFlow()

    private var _isAddTimerButtonEnabled = MutableStateFlow(true)
    val isAddTimerButtonEnabled = _isAddTimerButtonEnabled.asStateFlow()

    private var _isConfirmButtonEnabled = MutableStateFlow(false)
    val isConfirmButtonEnabled = _isConfirmButtonEnabled.asStateFlow()

    private var customWorkoutModels = ArrayList<CustomWorkoutModel>()

    // Copies
    val headerTextId = R.string.add_custom_workout_header_text
    private val workoutNameTextFieldTitle = R.string.add_custom_workout_name_text_field_title
    private val workoutNameTextFieldPlaceholder = R.string.add_custom_workout_name_text_field_placeholder
    private val workoutRepetitionsTextFieldTitle = R.string.add_custom_workout_repetitions_text_field_title
    private val workoutRepetitionsTextFieldPlaceholder = R.string.add_custom_workout_repetitions_text_field_placeholder
    private val workoutIntermediumRestTextFieldTitle = R.string.add_custom_workout_intermedium_rest_text_field_title
    private val workoutIntermediumRestTextFieldPlaceholder = R.string.add_custom_workout_intermedium_rest_text_field_placeholder
    private val workoutIntermediumRestFrequencyTextFieldTitle = R.string.add_custom_workout_intermedium_rest_frequency_text_field_title
    private val workoutIntermediumRestFrequencyTextFieldPlaceholder = R.string.add_custom_workout_intermedium_rest_frequency_text_field_placeholder
    val timerSectionTitleId = R.string.add_custom_workout_timer_section_title
    private val timerNameTextFieldTitle = R.string.add_custom_workout_timer_name_text_field_title
    private val timerNameWorkTextFieldPlaceholder = R.string.add_custom_workout_timer_name_work_text_field_placeholder
    private val timerNameWorkEmomTextField = R.string.add_custom_workout_timer_name_work_emom_text_field
    private val timerNameWorkTabataTextField = R.string.add_custom_workout_timer_name_work_tabata_text_field
    private val timerNameRestTabataTextField = R.string.add_custom_workout_timer_name_rest_tabata_text_field
    private val timerNameRestTextFieldPlaceholder = R.string.add_custom_workout_timer_name_rest_text_field_placeholder
    private val timerDurationTextFieldTitle: Int = R.string.add_custom_workout_timer_duration_text_field_title
    private val timerDurationTextFieldPlaceholder = R.string.add_custom_workout_timer_duration_text_field_placeholder
    private val timerTypeTitle = R.string.add_custom_workout_timer_type_text_field_title
    private val removeTimerButtonTextId = R.string.add_custom_workout_remove_timer_button_text
    private val addTimerButtonTextId = R.string.add_custom_workout_add_timer_button_text
    private val addWorkoutButtonTextId = R.string.add_custom_workout_add_workout_button_text

    // Assets
    val addButtonIconId = R.drawable.ic_add
    val addButtonIconDescription = "ic_add"
    val timerIconId = R.drawable.ic_timer
    val timerIconDescription = "ic_timer"
    val arrowDropUpIconId = R.drawable.ic_arrow_drop_up
    val arrowDropUpIconContentDescription = "ic_arrow_drop_up"
    val arrowDropDownIconId = R.drawable.ic_arrow_drop_down
    val arrowDropDownIconContentDescription = "ic_arrow_drop_down"

    fun navStateFired() {
        _navUiState.value = null
    }

    fun loadUiState(context: Context) {

        if (_dataUiState.value != null) return

        customWorkoutModels = getCustomWorkoutModels(context = context)?.let { ArrayList(it) } ?: ArrayList()

        when (workoutType) {
            WorkoutType.CustomWorkout -> {
                setCustomWorkoutUiState(context)
            }
            WorkoutType.Emom -> {
                setEmomUiState(context)
            }
            WorkoutType.Tabata -> {
                setTabataUiState(context)
            }
        }
    }

    private fun setCustomWorkoutUiState(context: Context) {
        var workoutTitle = ""
        var workoutRepetitions = ""
        var workoutIntermediumRest: TimerPickerModel? = null
        var workoutIntermediumRestFrequency = ""
        var timers: List<CustomTimerUiState> = listOf()
        if (workoutId != null) {
            val customWorkout = customWorkoutModels.first { it.id == workoutId }

            workoutTitle = customWorkout.name
            workoutRepetitions = customWorkout.repetition.toString()
            workoutIntermediumRest = customWorkout.intermediumRest
            workoutIntermediumRestFrequency = customWorkout.intermediumRestFrequency.toString()
            timers = customWorkout.timers
                .filter { it.type != TimerType.IntermediumRest }
                .map {
                    getNewCustomTimerUiState(
                        context = context,
                        id = it.id,
                        name = it.name,
                        duration = it.duration,
                        type = it.type
                    )
                }
        }

        _dataUiState.value = AddCustomWorkoutUiState(
            titleUiState = getTitleUiState(workoutTitle),
            repetitionsUiState = getRepetitionsUiState(workoutRepetitions),
            intermediumRestUiState = getIntermediumRestUiState(workoutIntermediumRest),
            intermediumRestFrequencyUiState = getIntermediumRestFrequencyUiState(
                workoutIntermediumRestFrequency
            ),
            customTimerUiStates = timers,
            addTimerButtonUiState = getAddTimerButtonUiState(),
            addWorkoutConfirmButtonUiState = getAddWorkoutConfirmButtonUiState(),
            bottomValidationErrors = listOf()
        )
    }

    private fun setEmomUiState(context: Context) {
        var workoutTitle = ""
        var workoutRepetitions = ""
        var timers: List<CustomTimerUiState>
        if (workoutId != null) {
            val customWorkout = customWorkoutModels.first { it.id == workoutId }

            workoutTitle = customWorkout.name
            workoutRepetitions = customWorkout.repetition.toString()
            val rawTimer = customWorkout.timers.first()
            timers = listOf(
                getNewCustomTimerUiState(
                    context = context,
                    id = rawTimer.id,
                    name = rawTimer.name,
                    duration = rawTimer.duration,
                    type = rawTimer.type
                )
            )


        } else {
            timers = listOf(
                getNewCustomTimerUiState(
                    context = context,
                    id = 0,
                    name = context.getString(timerNameWorkEmomTextField),
                    duration = TimerPickerModel(1, 0),
                    type = TimerType.Work
                )
            )
        }

        _dataUiState.value = AddCustomWorkoutUiState(
            titleUiState = getTitleUiState(workoutTitle),
            repetitionsUiState = getRepetitionsUiState(workoutRepetitions),
            customTimerUiStates = timers,
            addWorkoutConfirmButtonUiState = getAddWorkoutConfirmButtonUiState(),
            bottomValidationErrors = listOf()
        )
    }

    private fun setTabataUiState(context: Context) {
        var workoutTitle = ""
        var workoutRepetitions = ""
        var timers: List<CustomTimerUiState>
        if (workoutId != null) {
            val customWorkout = customWorkoutModels.first { it.id == workoutId }

            workoutTitle = customWorkout.name
            workoutRepetitions = customWorkout.repetition.toString()
            timers = customWorkout.timers
                .map {
                    getNewCustomTimerUiState(
                        context = context,
                        id = it.id,
                        name = it.name,
                        duration = it.duration,
                        type = it.type
                    )
                }


        } else {
            timers = listOf(
                getNewCustomTimerUiState(
                    context = context,
                    id = 0,
                    name = context.getString(timerNameWorkTabataTextField),
                    duration = TimerPickerModel(0, 30),
                    type = TimerType.Work
                ),
                getNewCustomTimerUiState(
                    context = context,
                    id = 1,
                    name = context.getString(timerNameRestTabataTextField),
                    duration = TimerPickerModel(0, 30),
                    type = TimerType.Rest
                )
            )
        }

        _dataUiState.value = AddCustomWorkoutUiState(
            titleUiState = getTitleUiState(workoutTitle),
            repetitionsUiState = getRepetitionsUiState(workoutRepetitions),
            customTimerUiStates = timers,
            addWorkoutConfirmButtonUiState = getAddWorkoutConfirmButtonUiState(),
            bottomValidationErrors = listOf()
        )
    }

    private fun getTitleUiState(workoutTitle: String) = GenericTextInputUiState(
        value = workoutTitle,
        titleId = workoutNameTextFieldTitle,
        placeholderId = workoutNameTextFieldPlaceholder,
        keyboardType = KeyboardType.Text
    )

    private fun getRepetitionsUiState(workoutRepetitions: String) = GenericTextInputUiState(
        value = workoutRepetitions,
        titleId = workoutRepetitionsTextFieldTitle,
        placeholderId = workoutRepetitionsTextFieldPlaceholder,
        keyboardType = KeyboardType.Number
    )

    private fun getIntermediumRestUiState(workoutIntermediumRest: TimerPickerModel?) =
        TimerPickerInputUiState(
            value = workoutIntermediumRest,
            titleId = workoutIntermediumRestTextFieldTitle,
            placeholderId = workoutIntermediumRestTextFieldPlaceholder
        )

    private fun getIntermediumRestFrequencyUiState(workoutIntermediumRestFrequency: String) =
        GenericTextInputUiState(
            value = workoutIntermediumRestFrequency,
            titleId = workoutIntermediumRestFrequencyTextFieldTitle,
            placeholderId = workoutIntermediumRestFrequencyTextFieldPlaceholder,
            keyboardType = KeyboardType.Number
        )

    private fun getAddTimerButtonUiState() = GenericButtonCardUiState(
        leftIconId = addButtonIconId,
        leftIconDescription = addButtonIconDescription,
        leftIconTintColor = Color.White,
        textId = addTimerButtonTextId
    )

    private fun getAddWorkoutConfirmButtonUiState() = GenericButtonCardUiState(
        leftIconId = addButtonIconId,
        leftIconDescription = addButtonIconDescription,
        leftIconTintColor = Color.White,
        textId = addWorkoutButtonTextId,
    )

    fun userHasEditedWorkoutTitle(context: Context, newValue: String) {
        _dataUiState.update {
            it?.copy(
                titleUiState = it.titleUiState.copy(
                    value = newValue,
                    errorTextId = getWorkoutTitleErrorId(newValue)
                )
            )
        }
        refreshConfirmButtonVisibility(context = context)
    }

    fun userHasEditedWorkoutRepetition(context: Context, newValue: String) {
        _dataUiState.update {
            it?.copy(
                repetitionsUiState = it.repetitionsUiState.copy(
                    value = newValue,
                    errorTextId = getWorkoutRepetitionsErrorId(newValue)
                ),
                intermediumRestUiState =
                if (newValue.isNotEmpty() && newValue.toInt() == 1)
                    it.intermediumRestUiState?.copy(
                        value = null,
                        errorTextId = null
                    )
                else it.intermediumRestUiState
            )
        }
        refreshConfirmButtonVisibility(context = context)
    }

    fun userHasOpenedIntermediumRestPicker(context: Context, titleId: Int, newModel: TimerPickerModel?) {
        _timerPickerUiState.value = TimerPickerUiState(
            titleId = titleId,
            initialMinutesValue = newModel?.minutes ?: 0,
            initialSecondsValue = newModel?.seconds ?: 0,
            onValueChanged = { newValue ->
                _dataUiState.update {
                    it?.copy(
                        intermediumRestUiState = it.intermediumRestUiState?.copy(
                            value = newValue,
                            errorTextId = getWorkoutIntermediumRestErrorId(newValue)
                        )
                    )
                }
                _timerPickerUiState.value = null
                refreshConfirmButtonVisibility(context = context)
            }
        )
    }

    fun userHasDismissedTimerPicker() {
        _timerPickerUiState.value = null
    }

    fun userHasEditedWorkoutIntermediumRestFrequency(context: Context, newValue: String) {
        _dataUiState.update {
            it?.copy(
                intermediumRestFrequencyUiState = it.intermediumRestFrequencyUiState?.copy(
                    value = newValue,
                    errorTextId = getWorkoutIntermediumRestFrequencyErrorId(newValue)
                )
            )
        }
        refreshConfirmButtonVisibility(context = context)
    }

    fun userHasAddedNewTimer(context: Context) {
        _dataUiState.update {
            val id = it?.customTimerUiStates?.count() ?: 0
            it?.copy(
                customTimerUiStates = it.customTimerUiStates?.plus(
                    getNewCustomTimerUiState(
                        context = context,
                        id = id
                    )
                )
            )
        }
        refreshConfirmButtonVisibility(context = context)
    }

    private fun getNewCustomTimerUiState(
        context: Context,
        id: Int,
        name: String? = null,
        duration: TimerPickerModel? = null,
        type: TimerType = if (id % 2 == 0) TimerType.Work else TimerType.Rest
    ): CustomTimerUiState {
        val defaultName = when (type) {
            TimerType.Work -> {
                context.getString(timerNameWorkTextFieldPlaceholder)
            }
            TimerType.Rest -> {
                context.getString(timerNameRestTextFieldPlaceholder)
            }
            else -> {
                ""
            }
        }

        val defaultDuration = TimerPickerModel(minutes = 0, seconds = 30)
        val lastSameTypeTimer =
            _dataUiState.value?.customTimerUiStates?.lastOrNull { it.typeUiState.value == type }

        val lastSameTypeTimerDuration = lastSameTypeTimer?.durationUiState?.value

        return CustomTimerUiState(
            leftIconId = timerIconId,
            leftIconDescription = timerIconDescription,
            leftIconTintColor = Color.White,
            rightIconId = arrowDropDownIconId,
            rightIconDescription = arrowDropDownIconContentDescription,
            rightIconTintColor = Color.White,
            isExpanded = false,
            id = id,
            nameUiState = GenericTextInputUiState(
                value = name ?: defaultName,
                titleId = timerNameTextFieldTitle,
                placeholderId = timerNameWorkTextFieldPlaceholder,
                keyboardType = KeyboardType.Text,
            ),
            isNameReadOnly = workoutType != WorkoutType.CustomWorkout,
            durationUiState = TimerPickerInputUiState(
                value = duration ?: lastSameTypeTimerDuration ?: defaultDuration,
                titleId = timerDurationTextFieldTitle,
                placeholderId = timerDurationTextFieldPlaceholder
            ),
            typeUiState = CustomTimerTypeUiState(
                value = type,
                titleId = timerTypeTitle,
                isReadOnly = workoutType != WorkoutType.CustomWorkout
            ),
            removeButtonTextId = if (workoutType == WorkoutType.CustomWorkout) removeTimerButtonTextId else null
        )
    }

    fun userHasClickedOnTimer(id: Int) {
        _dataUiState.update {
            it?.copy(
                customTimerUiStates = it.customTimerUiStates?.mapIndexed { index, timer ->
                    if (index == id) {
                        val isExpanded = !timer.isExpanded
                        return@mapIndexed timer.copy(
                            rightIconId = if (isExpanded) arrowDropUpIconId else arrowDropDownIconId,
                            rightIconDescription = if (isExpanded) arrowDropUpIconContentDescription else arrowDropDownIconContentDescription,
                            isExpanded = isExpanded
                        )
                    } else {
                        return@mapIndexed timer
                    }
                }
            )
        }
    }

    fun userHasEditedTimerTitle(context: Context, id: Int, newTitle: String) {
        _dataUiState.update {
            it?.copy(
                customTimerUiStates = it.customTimerUiStates?.mapIndexed { index, timer ->
                    if (index == id) {
                        val errorTextId = getTimerNameErrorId(newTitle)
                        return@mapIndexed timer.copy(
                            nameUiState = timer.nameUiState.copy(
                                value = newTitle,
                                errorTextId = errorTextId
                            ),
                            isValid = errorTextId == null && timer.durationUiState.errorTextId == null
                        )
                    } else {
                        return@mapIndexed timer
                    }
                }
            )
        }
        refreshConfirmButtonVisibility(context = context)
        refreshAddTimerButtonState()
    }

    fun userHasOpenedTimerDurationPicker(context: Context, id: Int, titleId: Int, newModel: TimerPickerModel?) {
        _timerPickerUiState.value = TimerPickerUiState(
            titleId = titleId,
            initialMinutesValue = newModel?.minutes ?: 0,
            initialSecondsValue = newModel?.seconds ?: 0,
            onValueChanged = { newValue ->
                _dataUiState.update {
                    it?.copy(
                        customTimerUiStates = it.customTimerUiStates?.mapIndexed { index, timer ->
                            if (index == id) {
                                val errorTextId = getTimerDurationErrorId(newValue)
                                return@mapIndexed timer.copy(
                                    durationUiState = timer.durationUiState.copy(
                                        value = newValue,
                                        errorTextId = errorTextId
                                    ),
                                    isValid = timer.nameUiState.errorTextId == null && errorTextId == null
                                )
                            } else {
                                return@mapIndexed timer
                            }
                        }
                    )
                }
                _timerPickerUiState.value = null
                refreshConfirmButtonVisibility(context = context)
                refreshAddTimerButtonState()
            }
        )
    }

    fun userHasClickedWorkoutType(context: Context, id: Int, type: TimerType, isSelected: Boolean) {
        _dataUiState.update {
            it?.copy(
                customTimerUiStates = it.customTimerUiStates?.mapIndexed { index, timer ->
                    if (index == id) {
                        return@mapIndexed timer.copy(
                            typeUiState = timer.typeUiState.copy(
                                value = if (isSelected) type else timer.typeUiState.value
                            )
                        )
                    } else {
                        return@mapIndexed timer
                    }
                }
            )
        }
        refreshConfirmButtonVisibility(context = context)
    }

    fun userHasRemovedTimer(context: Context, id: Int) {

        _dataUiState.update { state ->
            state?.copy(
                customTimerUiStates = state.customTimerUiStates?.filter { it.id != id }
            )
        }
        refreshConfirmButtonVisibility(context = context)
    }

    private fun refreshConfirmButtonVisibility(context: Context) {
        val isCustomWorkoutValid = isCustomWorkoutValid()
        val validationErrors = getBottomValidationErrors(
            context = context,
            isCustomWorkoutValid = isCustomWorkoutValid
        )
        _dataUiState.update {
            it?.copy(
                bottomValidationErrors = validationErrors
            )
        }

        _isConfirmButtonEnabled.value = isCustomWorkoutValid
    }

    private fun refreshAddTimerButtonState() {
        _isAddTimerButtonEnabled.value =
            _dataUiState.value?.customTimerUiStates?.none { !it.isValid } == true
    }

    // Validators


    private fun isWorkoutTitleValid(newTitle: String) = isTimerNameValid(newTitle)

    private fun areWorkoutRepetitionsValid(repetitions: String) = repetitions.isNotEmpty() && repetitions.toInt() > 0

    private fun isWorkoutIntermediumRestValid(
        repetitionsValue: String,
        intermediumRest: TimerPickerModel?
    ): Boolean {
        if (repetitionsValue.isEmpty()) return false
        return (repetitionsValue.toInt() == 1 && intermediumRest == null) || (repetitionsValue.toInt() > 1 && intermediumRest != null)
    }

    private fun isWorkoutIntermediumRestFrequencyValid(
        intermediumRest: TimerPickerModel?,
        isWorkoutIntermediumRestValid: Boolean,
        frequency: String?
    ): Boolean {
        if (frequency.isNullOrEmpty()) {
            if (intermediumRest == null && isWorkoutIntermediumRestValid) return true
            if (intermediumRest != null && isWorkoutIntermediumRestValid) return false
            return false
        } else if (intermediumRest != null && intermediumRest.toSeconds() > 0) {
            return frequency.toInt() > 0
        } else {
            return true
        }

    }


    private fun isTimerNameValid(name: String) = name.trim().isNotEmpty()

    private fun isTimerDurationValid(duration: TimerPickerModel?): Boolean {
        if (duration == null) return false
        return duration.minutes > 0 || duration.seconds > 0
    }

    private fun isCustomWorkoutValid(): Boolean {
        val state = _dataUiState.value ?: return false

        val isCustomWorkoutValid = when (workoutType) {
            WorkoutType.CustomWorkout -> isCustomWorkoutValid(state)
            WorkoutType.Emom -> isEmomValid(state)
            WorkoutType.Tabata -> isTabataValid(state)
        }
        return isCustomWorkoutValid
    }

    private fun isCustomWorkoutValid(state: AddCustomWorkoutUiState): Boolean {
        val isWorkoutTitleValid = isWorkoutTitleValid(state.titleUiState.value)
        val areWorkoutRepetitionsValid = areWorkoutRepetitionsValid(state.repetitionsUiState.value)
        val isWorkoutIntermediumRestValid = isWorkoutIntermediumRestValid(
            repetitionsValue = state.repetitionsUiState.value,
            intermediumRest = state.intermediumRestUiState?.value
        )
        val isWorkoutIntermediumRestFrequencyValid = isWorkoutIntermediumRestFrequencyValid(
            intermediumRest = state.intermediumRestUiState?.value,
            isWorkoutIntermediumRestValid = isWorkoutIntermediumRestValid,
            frequency = state.intermediumRestFrequencyUiState?.value
        )

        val timers = state.customTimerUiStates
        val areTimersValid = !timers.isNullOrEmpty() && timers.map {
            val isTimerNameValid = isTimerNameValid(it.nameUiState.value)
            val isTimerDurationValid = isTimerDurationValid(it.durationUiState.value)
            return@map isTimerNameValid && isTimerDurationValid
        }.all { it }

        val isCustomWorkoutValid = isWorkoutTitleValid && areWorkoutRepetitionsValid &&
                isWorkoutIntermediumRestValid && isWorkoutIntermediumRestFrequencyValid && areTimersValid
        return isCustomWorkoutValid
    }

    private fun isEmomValid(state: AddCustomWorkoutUiState): Boolean {
        val isWorkoutTitleValid = isWorkoutTitleValid(state.titleUiState.value)
        val areWorkoutRepetitionsValid = areWorkoutRepetitionsValid(state.repetitionsUiState.value)

        val timers = state.customTimerUiStates
        val areTimersValid = !timers.isNullOrEmpty() && timers.map {
            val isTimerNameValid = isTimerNameValid(it.nameUiState.value)
            val isTimerDurationValid = isTimerDurationValid(it.durationUiState.value)
            return@map isTimerNameValid && isTimerDurationValid
        }.all { it }

        val isCustomWorkoutValid = isWorkoutTitleValid && areWorkoutRepetitionsValid && areTimersValid
        return isCustomWorkoutValid
    }

    private fun isTabataValid(state: AddCustomWorkoutUiState): Boolean {
        val isWorkoutTitleValid = isWorkoutTitleValid(state.titleUiState.value)
        val areWorkoutRepetitionsValid = areWorkoutRepetitionsValid(state.repetitionsUiState.value)

        val timers = state.customTimerUiStates
        val areTimersValid = !timers.isNullOrEmpty() && timers.map {
            val isTimerNameValid = isTimerNameValid(it.nameUiState.value)
            val isTimerDurationValid = isTimerDurationValid(it.durationUiState.value)
            return@map isTimerNameValid && isTimerDurationValid
        }.all { it }

        val isCustomWorkoutValid = isWorkoutTitleValid && areWorkoutRepetitionsValid && areTimersValid
        return isCustomWorkoutValid
    }

    // Error ids

    private fun getBottomValidationErrors(context: Context, isCustomWorkoutValid: Boolean): List<String> {

        val validationErrors = ArrayList<String>()
        val state = dataUiState.value

        if (state?.customTimerUiStates.isNullOrEmpty()) {
            validationErrors.add(context.getString(R.string.add_custom_workout_timers_error))
        }

        if (validationErrors.isEmpty() && !isCustomWorkoutValid) {
            validationErrors.add(context.getString(R.string.add_custom_workout_generic_error))
        }

        return validationErrors
    }

    private fun getWorkoutTitleErrorId(newTitle: String): Int? {
        val isValid = isWorkoutTitleValid(newTitle)
        return if (isValid) null else R.string.add_custom_workout_title_error
    }

    private fun getWorkoutRepetitionsErrorId(repetitions: String): Int? {
        val isValid = areWorkoutRepetitionsValid(repetitions)
        return if (isValid) null else R.string.add_custom_workout_repetitions_error
    }

    private fun getWorkoutIntermediumRestErrorId(intermediumRest: TimerPickerModel): Int? {
        val state = dataUiState.value
        val repetitionsValue = state?.repetitionsUiState?.value

        if (repetitionsValue.isNullOrEmpty()) return null

        val isValid = isWorkoutIntermediumRestValid(repetitionsValue, intermediumRest)

        return if (isValid) null else R.string.add_custom_workout_intermedium_rest_error
    }

    private fun getWorkoutIntermediumRestFrequencyErrorId(value: String): Int? {
        val state = dataUiState.value
        val repetitionsValue = state?.repetitionsUiState?.value

        if (repetitionsValue.isNullOrEmpty()) return null

        val intermediumRest = state.intermediumRestUiState?.value
        val isIntermediumRestValid = isWorkoutIntermediumRestValid(repetitionsValue, intermediumRest)
        val isValid = isWorkoutIntermediumRestFrequencyValid(
            intermediumRest = intermediumRest,
            isWorkoutIntermediumRestValid = isIntermediumRestValid,
            frequency = value
        )

        return if (isValid) null else R.string.add_custom_workout_intermedium_rest_frequency_error
    }

    private fun getTimerNameErrorId(name: String): Int? {
        val isValid = isTimerNameValid(name)
        return if (isValid) null else R.string.add_custom_workout_timer_name_error
    }

    private fun getTimerDurationErrorId(duration: TimerPickerModel): Int? {
        val isValid = isTimerDurationValid(duration)
        return if (isValid) null else R.string.add_custom_workout_timer_duration_error
    }

    fun confirmButtonClicked(context: Context) {
        val maxId = customWorkoutModels.maxOfOrNull { it.id }
        val state = _dataUiState.value ?: return
        val intermediumRestFrequencyRaw = state.intermediumRestFrequencyUiState?.value

        val customTimerUiStates = state.customTimerUiStates ?: listOf()
        var newCustomWorkoutModel = CustomWorkoutModel(
            id = workoutId ?: (maxId?.plus(1)) ?: 0,
            name = state.titleUiState.value,
            repetition = state.repetitionsUiState.value.toInt(),
            intermediumRest = state.intermediumRestUiState?.value,
            intermediumRestFrequency = intermediumRestFrequencyRaw?.toIntOrNull(),
            timers = customTimerUiStates.mapNotNull {
                val duration = it.durationUiState.value ?: return@mapNotNull null
                return@mapNotNull CustomTimerModel(
                    id = it.id,
                    name = it.nameUiState.value,
                    duration = duration,
                    type =  it.typeUiState.value
                )
            }
        )

        val intermediumRestTimer = getIntermediumRestTimer(
            context = context,
            intermediumRest = state.intermediumRestUiState?.value,
            id = customTimerUiStates.size
        )

        if (intermediumRestTimer != null) {
            newCustomWorkoutModel = newCustomWorkoutModel.copy(
                timers = newCustomWorkoutModel.timers + intermediumRestTimer
            )
        }

        if (workoutId != null) {
            val index = customWorkoutModels.indexOfFirst { it.id == workoutId }
            customWorkoutModels[index] = newCustomWorkoutModel
        } else {
            customWorkoutModels.add(newCustomWorkoutModel)
        }

        val json = Gson().toJson(customWorkoutModels)
        PrefsUtils.setStringPref(context, workoutType.getPrefName(), json)

        _navUiState.value = NavUiState.GoBackToCustomWorkoutList
    }

    private fun getIntermediumRestTimer(
        context: Context,
        intermediumRest: TimerPickerModel?,
        id: Int
    ): CustomTimerModel? {
        return if (intermediumRest != null && intermediumRest.toSeconds() > 0) {
            CustomTimerModel(
                id = id,
                name = context.getString(R.string.add_custom_workout_intermedium_rest_text_field_title),
                duration = intermediumRest,
                type = TimerType.IntermediumRest
            )
        } else {
            null
        }
    }

    private fun getCustomWorkoutModels(context: Context): List<CustomWorkoutModel>? {
        val customWorkoutsRawModels =
            PrefsUtils.getStringPref(context, workoutType.getPrefName())
        val sType = object : TypeToken<List<CustomWorkoutModel>>() {}.type
        val customWorkoutModels: List<CustomWorkoutModel>? =
            Gson().fromJson<List<CustomWorkoutModel>?>(customWorkoutsRawModels, sType)
        return customWorkoutModels
    }
}

