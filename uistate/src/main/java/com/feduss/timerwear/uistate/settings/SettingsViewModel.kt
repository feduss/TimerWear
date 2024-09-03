package com.feduss.timerwear.uistate.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.uistate.settings.SettingsUiState
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

    private val feedbackEmail = "feduss96@gmail.com"

    // Copies
    private val headerTextId = R.string.settings_title
    private val feedbackTextId = R.string.settings_feedback_title
    private val appVersionTextId = R.string.settings_app_version
    private val soundCheckboxTextId = R.string.add_custom_workout_sound_checkbox_text

    fun loadUiState(context: Context) {

        if (_dataUiState.value != null) return

        _dataUiState.value = SettingsUiState(
            headerTextId = headerTextId,
            isSoundEnabled = PrefsUtils.getSoundPreference(context),
            appVersionTextId = appVersionTextId,
            soundCheckboxTextId = soundCheckboxTextId,
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

    fun userHasTappedEmail() {
        _navUiState.value = NavUiState.GoToEmail
    }

    fun firedNavState() {
        _navUiState.value = null
    }
}