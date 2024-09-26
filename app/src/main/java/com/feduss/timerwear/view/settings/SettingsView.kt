package com.feduss.timerwear.view.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Text
import com.feduss.timerwear.BuildConfig
import com.feduss.timerwear.extension.infiniteMarquee
import com.feduss.timerwear.uistate.extension.PurpleCustom
import com.feduss.timerwear.uistate.uistate.settings.SettingsViewModel
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@OptIn(ExperimentalHorologistApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    context: Context,
    viewModel: SettingsViewModel = hiltViewModel(),
    columnState: ScalingLazyColumnState,
    onEmailFeedbackTapped: () -> Unit
) {

    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()

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

    dataUiState?.let { state ->

        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            columnState = columnState
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                        Checkbox(
                            checked = state.isSoundEnabled,
                            onCheckedChange = {
                                viewModel.saveSoundPreference(
                                    context = context,
                                    isSoundEnabled = it
                                )
                            }
                        )
                    }
                    Text(
                        modifier = Modifier.infiniteMarquee,
                        text = stringResource(id = state.soundCheckboxTextId),
                        color = Color.White,
                        textAlign = TextAlign.Left,
                        fontSize = TextUnit(12f, TextUnitType.Sp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(state.feedbackTextId),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        modifier = Modifier.clickable {
                            viewModel.userHasTappedEmail()
                        },
                        text = state.feedbackEmail,
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        color = Color.PurpleCustom
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }


            item {
                Text(
                    text = stringResource(state.appVersionTextId, versionName, versionCode),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}