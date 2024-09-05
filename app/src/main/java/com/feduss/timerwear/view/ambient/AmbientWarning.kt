package com.feduss.timerwear.view.ambient

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import com.feduss.timerwear.lifecycle.OnLifecycleEvent
import com.feduss.timerwear.uistate.uistate.ambient.AmbientWarningViewModel
import com.feduss.timerwear.utils.AmbientUtils
import com.feduss.timerwear.view.dialog.AlertDialog

@Composable
fun AmbientWarning(
    context: Context,
    viewModel: AmbientWarningViewModel,
    onGoToDisplaySettings: () -> Unit,
    onGoToTimer: () -> Unit
) {
    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (AmbientUtils.isAmbientDisplayOn(context)) {
                    onGoToTimer()
                }
            }
            else -> { }
        }
    }

    navUiState?.let {
        viewModel.navStateFired()
        when(it) {
            AmbientWarningViewModel.NavUiState.GoToDisplaySettings -> {
                onGoToDisplaySettings()
            }
            AmbientWarningViewModel.NavUiState.GoToTimer -> {
                onGoToTimer()
            }
        }
    }

    dataUiState?.let { state ->

        AlertDialog(
            titleId = state.titleId,
            subtitle = state.subTitleId,
            negativeButtonIconId = state.negativeIconId,
            negativeButtonIconDesc = state.negativeIconDescription,
            negativeButtonClicked = {
                viewModel.negativeButtonPressed()
            },
            positiveButtonIconId = state.positiveIconId,
            positiveButtonIconDesc = state.positiveIconDescription,
            positiveButtonClicked = {
                viewModel.positiveButtonPressed()
            }
        )
    }
}