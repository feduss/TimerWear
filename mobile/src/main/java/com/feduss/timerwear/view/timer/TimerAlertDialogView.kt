package com.feduss.timerwear.view.timer

import androidx.compose.runtime.Composable
import com.feduss.timerwear.entity.enums.AlertDialogType
import com.feduss.timerwear.uistate.uistate.timer.TimerAlertDialogUiState
import com.feduss.timerwear.uistate.uistate.timer.TimerViewModel
import com.feduss.timerwear.view.component.dialog.CustomAlertDialog

@Composable
fun TimerAlertDialogView(
    alertDialogUiState: TimerAlertDialogUiState,
    viewModel: TimerViewModel,
    currentTimerIndex: Int,
    currentRepetition: Int,
    userGoBack: () -> Unit
) {
    when (alertDialogUiState.alertDialogType) {
        AlertDialogType.SkipTimer -> {
            CustomAlertDialog(
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
            CustomAlertDialog(
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