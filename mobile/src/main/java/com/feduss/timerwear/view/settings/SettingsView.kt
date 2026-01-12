package com.feduss.timerwear.view.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.feduss.timerwear.BuildConfig
import com.feduss.timerwear.extension.infiniteMarquee
import com.feduss.timerwear.uistate.extension.Purple700
import com.feduss.timerwear.uistate.uistate.picker.TimerPickerUiState
import com.feduss.timerwear.uistate.uistate.settings.SettingsViewModel
import com.feduss.timerwear.view.component.card.GenericOtherInputCard
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.feduss.timerwear.view.component.list.LazyColumnWithScrollbar
import com.feduss.timerwear.view.component.picker.TimerPicker

@Composable
fun SettingsView(
    context: Context,
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController,
    columnState: LazyListState,
    onEmailFeedbackTapped: () -> Unit
) {

    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()
    val timerPickerState by viewModel.timerPickerUiState.collectAsState()

    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE

    LaunchedEffect(Unit) {
        viewModel.loadUiState(context)
    }

    navUiState?.let { state ->
        when (state) {
            is SettingsViewModel.NavUiState.GoToEmail -> {
                onEmailFeedbackTapped()
            }
        }
        viewModel.firedNavState()
    }

    timerPickerState?.let {
        TimerPicker(
            titleId = it.titleId,
            initialMinutesValue = it.initialMinutesValue,
            initialSecondsValue = it.initialSecondsValue,
            onDismiss = it.onValueChanged
        )
    }

    dataUiState?.let { state ->

         LazyColumnWithScrollbar(
            state = columnState,
        ) {
            item {
                LeftIconTextHeader(
                    title = stringResource(state.headerTextId)
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        0.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    Checkbox(
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.primary
                        ),
                        checked = state.isSoundEnabled,
                        onCheckedChange = {
                            viewModel.saveSoundPreference(
                                context = context,
                                isSoundEnabled = it
                            )
                        }
                    )
                    Text(
                        modifier = Modifier.infiniteMarquee(),
                        text = stringResource(id = state.soundCheckboxTextId),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Left
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            val timerWarningSecondsUiState = state.timerWarningSecondsUiState

            item {
                GenericOtherInputCard(
                    titleId = timerWarningSecondsUiState.titleId,
                    placeholderId = timerWarningSecondsUiState.placeholderId,
                    value = timerWarningSecondsUiState.value?.toSecondsString() ?: "",
                    errorTextId = null,
                    onCardClicked = {
                        viewModel.userHasOpenedTimerWarningSecondsPicker(
                            context = context,
                            titleId = timerWarningSecondsUiState.titleId,
                            newModel = timerWarningSecondsUiState.value
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(state.feedbackTextId),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        modifier = Modifier.clickable {
                            viewModel.userHasTappedEmail()
                        },
                        text = state.feedbackEmail,
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        color = Color.Purple700
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }


            item {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    text = stringResource(state.appVersionTextId, versionName, versionCode),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

private fun handleBackButton(
    timerPickerState: TimerPickerUiState?,
    viewModel: SettingsViewModel,
    navController: NavController
) {
    if (timerPickerState != null) {
        viewModel.userHasDismissedTimerPicker()
    } else {
        navController.popBackStack()
    }
}