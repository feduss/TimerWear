package com.feduss.timerwear.view.timer

import androidx.compose.runtime.Composable
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.uistate.uistate.timer.TimerAlertDialogUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.view.dialog.AlertDialog

@Composable
fun TimerAlertDialogView(
    alertDialogUiState: TimerAlertDialogUiState,
    viewModel: TimerViewModel,
    currentTimerIndex: Int,
    currentRepetition: Int,
    onTimerSet: (String) -> Unit,
    userGoBack: () -> Unit
) {
    onTimerSet("")

    when (alertDialogUiState.alertDialogType) {
        AlertDialogType.SkipTimer -> {
            AlertDialog(
                titleId = alertDialogUiState.alertDialogSkipTitleId,
                negativeButtonIconId = alertDialogUiState.alertDialogSkipNegativeIconId,
                negativeButtonIconDesc = alertDialogUiState.alertDialogSkipNegativeIconDescription,
                negativeButtonClicked = userGoBack,
                positiveButtonIconId = alertDialogUiState.alertDialogSkipPositiveIconId,
                positiveButtonIconDesc = alertDialogUiState.alertDialogSkipPositiveIconDescription,
                positiveButtonClicked = {
                    viewModel.userChangeAlertDialogState(
                        isAlertDialogVisible = false,
                        alertDialogType = null,
                        completion = {
                            viewModel.userSkipToNextTimer(
                                currentTimerIndex = currentTimerIndex,
                                currentRepetition = currentRepetition
                            )
                        }
                    )
                }
            )
        }

        AlertDialogType.StopTimer -> {
            AlertDialog(
                titleId = alertDialogUiState.alertDialogStopTitleId,
                negativeButtonIconId = alertDialogUiState.alertDialogStopNegativeIconId,
                negativeButtonIconDesc = alertDialogUiState.alertDialogStopNegativeIconDescription,
                negativeButtonClicked = userGoBack,
                positiveButtonIconId = alertDialogUiState.alertDialogStopPositiveIconId,
                positiveButtonIconDesc = alertDialogUiState.alertDialogStopPositiveIconDescription,
                positiveButtonClicked = {
                    viewModel.userChangeAlertDialogState(
                        isAlertDialogVisible = false,
                        alertDialogType = null,
                        completion = {
                            viewModel.userGoBackToWorkoutList()
                        }
                    )
                }
            )
        }

        null -> {}
    }
}