package com.feduss.timerwear.view.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import com.feduss.timerwear.BuildConfig
import com.feduss.timerwear.uistate.extension.PurpleCustom
import com.feduss.timerwear.uistate.settings.SettingsViewModel
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun SettingsView(
    viewModel: SettingsViewModel = hiltViewModel(),
    columnState: ScalingLazyColumnState,
    onEmailFeedbackTapped: () -> Unit
) {

    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        columnState = columnState
    ) {
        item {
            LeftIconTextHeader(
                title = stringResource(viewModel.headerTextId)
            )
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
                    text = stringResource(viewModel.feedbackTextId),
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.clickable {
                          onEmailFeedbackTapped()
                    },
                    text = viewModel.feedbackEmail,
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
                text = stringResource(viewModel.appVersionTextId, versionName, versionCode),
                textAlign = TextAlign.Center
            )
        }
    }
}