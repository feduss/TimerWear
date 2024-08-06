package com.feduss.timerwear.uistate.uistate.add_custom_timer

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.feduss.timerwear.entity.CustomTimerModel
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.TimerPickerModel
import com.feduss.timerwear.entity.enums.CustomTimerType
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.uistate.GenericButtonCardUiState
import com.feduss.timerwear.uistate.uistate.GenericTextInputUiState
import com.feduss.timerwear.uistate.uistate.TimerPickerInputUiState
import com.feduss.timerwear.uistate.uistate.picker.TimerPickerUiState
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

class AddCustomWorkoutViewModel @AssistedInject constructor(
    @Assisted("workoutId") val workoutId: String,
) : ViewModel() {

    //DI

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("workoutId") workoutId: String
        ): AddCustomWorkoutViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            workoutId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(workoutId) as T
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
    val timerSectionTitleId = R.string.add_custom_workout_timer_section_title
    private val timerNameTextFieldTitle = R.string.add_custom_workout_timer_name_text_field_title
    private val timerNameTextFieldPlaceholder = R.string.add_custom_workout_timer_name_text_field_placeholder
    private val timerDurationTextFieldTitle: Int = R.string.add_custom_workout_timer_duration_text_field_title
    private val timerDurationTextFieldPlaceholder = R.string.add_custom_workout_timer_duration_text_field_placeholder
    private val timerTypeTitle = R.string.add_custom_workout_timer_type_text_field_title
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

        var workoutTitle = ""
        var workoutRepetitions = ""
        var workoutIntermediumRest: TimerPickerModel? = null
        var timers: List<CustomTimerUiState> = listOf()
        if (workoutId.isNotEmpty()) {
            val id = workoutId.toInt()
            var customWorkout = customWorkoutModels.first { it.id == id }

            workoutTitle = customWorkout.name
            workoutRepetitions = customWorkout.repetition.toString()
            workoutIntermediumRest = customWorkout.intermediumRest
            timers = customWorkout.timers.map {
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
            titleUiState = GenericTextInputUiState(
                value = workoutTitle,
                titleId = workoutNameTextFieldTitle,
                placeholderId = workoutNameTextFieldPlaceholder,
                keyboardType = KeyboardType.Text
            ),
            repetitionsUiState = GenericTextInputUiState(
                value = workoutRepetitions,
                titleId = workoutRepetitionsTextFieldTitle,
                placeholderId = workoutRepetitionsTextFieldPlaceholder,
                keyboardType = KeyboardType.Number
            ),
            intermediumRestUiState = TimerPickerInputUiState(
                value = workoutIntermediumRest,
                titleId = workoutIntermediumRestTextFieldTitle,
                placeholderId = workoutIntermediumRestTextFieldPlaceholder
            ),
            customTimerUiStates = timers,
            addTimerButtonUiState = GenericButtonCardUiState(
                leftIconId = addButtonIconId,
                leftIconDescription = addButtonIconDescription,
                leftIconTintColor = Color.White,
                textId = addTimerButtonTextId
            ),
            addWorkoutConfirmButtonUiState = GenericButtonCardUiState(
                leftIconId = addButtonIconId,
                leftIconDescription = addButtonIconDescription,
                leftIconTintColor = Color.White,
                textId = addWorkoutButtonTextId,
            ),
            bottomValidationErrors = listOf()
        )
    }

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
                if (newValue.isEmpty() || newValue.toInt() == 0)
                    it.intermediumRestUiState.copy(
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
                        intermediumRestUiState = it.intermediumRestUiState.copy(
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

    fun userHasAddedNewTimer(context: Context) {
        _dataUiState.update {
            val id = it?.customTimerUiStates?.count() ?: 0
            it?.copy(
                customTimerUiStates = it.customTimerUiStates + getNewCustomTimerUiState(
                    context = context,
                    id = id
                )
            )
        }
        refreshConfirmButtonVisibility(context = context)
    }

    private fun getNewCustomTimerUiState(
        context: Context,
        id: Int,
        name: String = context.getString(timerNameTextFieldPlaceholder),
        duration: TimerPickerModel = TimerPickerModel(minutes = 0, seconds = 30),
        type: CustomTimerType = if (id % 2 == 0) CustomTimerType.Work else CustomTimerType.Rest
    ) = CustomTimerUiState(
        leftIconId = timerIconId,
        leftIconDescription = timerIconDescription,
        leftIconTintColor = Color.White,
        rightIconId = arrowDropDownIconId,
        rightIconDescription = arrowDropDownIconContentDescription,
        rightIconTintColor = Color.White,
        isExpanded = false,
        id = id,
        nameUiState = GenericTextInputUiState(
            value = name,
            titleId = timerNameTextFieldTitle,
            placeholderId = timerNameTextFieldPlaceholder,
            keyboardType = KeyboardType.Text,
        ),
        durationUiState = TimerPickerInputUiState(
            value = duration,
            titleId = timerDurationTextFieldTitle,
            placeholderId = timerDurationTextFieldPlaceholder
        ),
        typeUiState = CustomTimerTypeUiState(
            value = type,
            titleId = timerTypeTitle
        )
    )

    fun userHasClickedOnTimer(id: Int) {
        _dataUiState.update {
            it?.copy(
                customTimerUiStates = it.customTimerUiStates.mapIndexed { index, timer ->
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
                customTimerUiStates = it.customTimerUiStates.mapIndexed { index, timer ->
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
                        customTimerUiStates = it.customTimerUiStates.mapIndexed { index, timer ->
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

    fun userHasClickedWorkoutType(context: Context, id: Int, type: CustomTimerType, isSelected: Boolean) {
        _dataUiState.update {
            it?.copy(
                customTimerUiStates = it.customTimerUiStates.mapIndexed { index, timer ->
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

    private fun areWorkoutRepetitionsValid(repetitions: String) = repetitions.isNotEmpty()

    private fun isWorkoutIntermediumRestValid(
        repetitionsValue: String,
        intermediumRest: TimerPickerModel?
    ): Boolean {
        if (repetitionsValue.isEmpty()) return false
        return (repetitionsValue.toInt() == 0 && intermediumRest == null) || (repetitionsValue.toInt() > 1 && intermediumRest != null)
    }

    private fun isTimerNameValid(name: String) = name.trim().isNotEmpty()

    private fun isTimerDurationValid(duration: TimerPickerModel?): Boolean {
        if (duration == null) return false
        return duration.minutes > 0 || duration.seconds > 0
    }

    private fun isCustomWorkoutValid(): Boolean {
        val state = _dataUiState.value ?: return false

        val isWorkoutTitleValid = isWorkoutTitleValid(state.titleUiState.value)
        val areWorkoutRepetitionsValid = areWorkoutRepetitionsValid(state.repetitionsUiState.value)
        val isWorkoutIntermediumRestValid = isWorkoutIntermediumRestValid(
            repetitionsValue = state.repetitionsUiState.value,
            intermediumRest = state.intermediumRestUiState.value
        )

        val timers = state.customTimerUiStates
        val areTimersValid = timers.isNotEmpty() && timers.map {
            val isTimerNameValid = isTimerNameValid(it.nameUiState.value)
            val isTimerDurationValid = isTimerDurationValid(it.durationUiState.value)
            return@map isTimerNameValid && isTimerDurationValid
        }.all { it }

        val isCustomWorkoutValid = isWorkoutTitleValid && areWorkoutRepetitionsValid &&
                isWorkoutIntermediumRestValid && areTimersValid
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

        val newCustomWorkoutModel = CustomWorkoutModel(
            id = maxId ?: 0,
            name = state.titleUiState.value,
            repetition = state.repetitionsUiState.value.toInt(),
            intermediumRest = state.intermediumRestUiState.value,
            timers = state.customTimerUiStates.mapNotNull {
                val duration = it.durationUiState.value ?: return@mapNotNull null
                return@mapNotNull CustomTimerModel(
                    id = it.id,
                    name = it.nameUiState.value,
                    duration = duration,
                    type =  it.typeUiState.value
                )
            }
        )

        if (workoutId.isNotEmpty()) {
            val id = workoutId.toInt()
            val index = customWorkoutModels.indexOfFirst { it.id == id }
            customWorkoutModels[index] = newCustomWorkoutModel
        } else {
            customWorkoutModels.add(newCustomWorkoutModel)
        }

        val json = Gson().toJson(customWorkoutModels)
        PrefsUtils.setStringPref(context, PrefParam.CustomWorkoutList.value, json)

        _navUiState.value = NavUiState.GoBackToCustomWorkoutList
    }

    private fun getCustomWorkoutModels(context: Context): List<CustomWorkoutModel>? {
        val customWorkoutsRawModels =
            PrefsUtils.getStringPref(context, PrefParam.CustomWorkoutList.value)
        val sType = object : TypeToken<List<CustomWorkoutModel>>() {}.type
        val customWorkoutModels: List<CustomWorkoutModel>? =
            Gson().fromJson<List<CustomWorkoutModel>?>(customWorkoutsRawModels, sType)
        return customWorkoutModels
    }
}

