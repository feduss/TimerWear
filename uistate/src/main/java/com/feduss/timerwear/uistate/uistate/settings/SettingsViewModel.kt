package com.feduss.timerwear.uistate.uistate.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.feduss.timerwear.entity.TimerPickerModel
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.uistate.TimerPickerInputUiState
import com.feduss.timerwear.uistate.uistate.picker.TimerPickerUiState
import com.feduss.timerwear.utils.PrefParam
import com.feduss.timerwear.utils.PrefsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(): ViewModel() {

    sealed class NavUiState {
        data object GoToEmail: NavUiState()
    }

    private var _dataUiState = MutableStateFlow<SettingsUiState?>(null)
    var dataUiState = _dataUiState.asStateFlow()

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    private var _timerPickerUiState = MutableStateFlow<TimerPickerUiState?>(null)
    val timerPickerUiState = _timerPickerUiState.asStateFlow()

    private val feedbackEmail = "feduss96@gmail.com"

    // Copies
    private val headerTextId = R.string.settings_title
    private val feedbackTextId = R.string.settings_feedback_title
    private val appVersionTextId = R.string.settings_app_version
    private val soundCheckboxTextId = R.string.add_custom_workout_sound_checkbox_text
    private val timerWarningTextFieldSecondsTitle = R.string.settings_timer_warning_seconds_textfield_title
    private val timerWarningSecondsTextFieldPlaceholder = timerWarningTextFieldSecondsTitle

    fun loadUiState(context: Context) {

        if (_dataUiState.value != null) return

        val timerWarningSeconds = PrefsUtils.getTimerWarningSeconds(context)

        _dataUiState.value = SettingsUiState(
            headerTextId = headerTextId,
            isSoundEnabled = PrefsUtils.getSoundPreference(context),
            appVersionTextId = appVersionTextId,
            soundCheckboxTextId = soundCheckboxTextId,
            timerWarningSecondsUiState = TimerPickerInputUiState(
                value = TimerPickerModel(minutes = 0, seconds = timerWarningSeconds),
                titleId = timerWarningTextFieldSecondsTitle,
                placeholderId = timerWarningSecondsTextFieldPlaceholder
            ),
            feedbackTextId = feedbackTextId,
            feedbackEmail = feedbackEmail
        )
    }

    fun saveSoundPreference(context: Context, isSoundEnabled: Boolean) {
        PrefsUtils.setStringPref(context, PrefParam.IsSoundEnabled.value, isSoundEnabled.toString())

        _dataUiState.update {
            it?.copy(
                isSoundEnabled = isSoundEnabled
            )
        }
    }

    fun saveTimerWarningSecondsPreference(context: Context, seconds: Int) {
        PrefsUtils.setStringPref(context, PrefParam.TimerWarningSeconds.value, seconds.toString())
    }

    fun userHasOpenedTimerWarningSecondsPicker(context: Context, titleId: Int, newModel: TimerPickerModel?) {

        val seconds = newModel?.seconds ?: 3

        _timerPickerUiState.value = TimerPickerUiState(
            titleId = titleId,
            initialMinutesValue = null,
            maxMinutesOptions = null,
            initialSecondsValue = seconds,
            maxSecondsOptions = 5 + 1,
            onValueChanged = { newValue ->
                _dataUiState.update {
                    it?.copy(
                        timerWarningSecondsUiState = it.timerWarningSecondsUiState.copy(
                            value = newValue,
                            errorTextId = -1
                        )
                    )
                }
                _timerPickerUiState.value = null
                saveTimerWarningSecondsPreference(context = context, seconds = seconds)
            }
        )
    }

    fun userHasTappedEmail() {
        _navUiState.value = NavUiState.GoToEmail
    }

    fun userHasDismissedTimerPicker() {
        _timerPickerUiState.value = null
    }

    fun firedNavState() {
        _navUiState.value = null
    }
}