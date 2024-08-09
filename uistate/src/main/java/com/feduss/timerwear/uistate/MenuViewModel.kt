package com.feduss.timerwear.uistate

import android.content.Context
import androidx.lifecycle.ViewModel
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.utils.PrefsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor() : ViewModel() {

    sealed class NavUiState {
        data class GoToCustomWorkout(val isTimerActive: Boolean): NavUiState()
        data class GoToEmom(val isTimerActive: Boolean): NavUiState()
        data class GoToTabata(val isTimerActive: Boolean): NavUiState()
    }

    private var _navUiState = MutableStateFlow<NavUiState?>(null)
    val navUiState = _navUiState.asStateFlow()

    fun loadActiveTimer(context: Context) {
        if (PrefsUtils.isTimerActive(context)) {
            when(PrefsUtils.getTimerType(context)) {
                TimerType.CustomWorkout -> _navUiState.value = NavUiState.GoToCustomWorkout(true)
                TimerType.Emom -> _navUiState.value = NavUiState.GoToEmom(true)
                TimerType.Tabata -> _navUiState.value = NavUiState.GoToTabata(true)
                null -> return
            }
        }
    }

    fun userClickedOnCustomWorkout() {
        _navUiState.value = NavUiState.GoToCustomWorkout(false)
    }

    fun userClickedOnEmom() {
        _navUiState.value = NavUiState.GoToEmom(false)
    }

    fun userClickedOnTabata() {
        _navUiState.value = NavUiState.GoToTabata(false)
    }

    fun navStateFired() {
        _navUiState.value = null
    }
}