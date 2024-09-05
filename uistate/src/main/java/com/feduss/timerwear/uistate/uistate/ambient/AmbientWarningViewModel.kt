package com.feduss.timerwear.uistate.uistate.ambient

import androidx.lifecycle.ViewModel
import com.feduss.timerwear.uistate.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AmbientWarningViewModel @Inject constructor(): ViewModel() {

    sealed class NavUiState {
        data object GoToTimer: NavUiState()
        data object GoToDisplaySettings: NavUiState()
    }

    private var _dataUiState = MutableStateFlow<AmbientWarningUiState?>(null)
    var dataUiState = _dataUiState.asStateFlow()

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    // Copies
    val titleId = R.string.ambient_warning_title
    val subTitleId = R.string.ambient_warning_subtitle

    // Assets
    val settingsIconId = R.drawable.ic_settings
    val settingsIconDescription = "ic_settings"
    val closeIconId = R.drawable.ic_close
    val closeIconDescription = "ic_close"

    init {
        _dataUiState.value = AmbientWarningUiState(
            titleId = titleId,
            subTitleId = subTitleId,
            positiveIconId = settingsIconId,
            positiveIconDescription = settingsIconDescription,
            negativeIconId = closeIconId,
            negativeIconDescription = closeIconDescription
        )
    }

    fun positiveButtonPressed() {
        _navUiState.value = NavUiState.GoToDisplaySettings
    }

    fun negativeButtonPressed() {
        _navUiState.value = NavUiState.GoToTimer
    }

    fun navStateFired() {
        _navUiState.value = null
    }

}