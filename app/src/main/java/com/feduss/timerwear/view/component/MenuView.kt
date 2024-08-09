package com.feduss.timerwear.view.component

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.view.component.card.GenericRoundedCard
import com.feduss.timerwear.uistate.MenuViewModel
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.Purple200
import com.feduss.timerwear.uistate.extension.Purple700
import com.feduss.timerwear.uistate.extension.Teal200
import com.feduss.timerwear.view.MainActivity

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MenuView(
    context: Context,
    columnState: ScalingLazyColumnState,
    navController: NavController,
    viewModel: MenuViewModel = hiltViewModel()
) {

    val navUiState by viewModel.navUiState.collectAsState()

    navUiState?.let {
        when(it) {
            is MenuViewModel.NavUiState.GoToCustomWorkout -> {
                goToCustomWorkoutList(
                    navController = navController
                )
            }
            is MenuViewModel.NavUiState.GoToEmom -> TODO()
            is MenuViewModel.NavUiState.GoToTabata -> TODO()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadActiveTimer(context)
    }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState
    ) {
        item {
            LeftIconTextHeader(
                title = "TimerWear"
            )
        }

        item {
            GenericRoundedCard(
                leftText = stringResource(R.string.main_page_custom_workout_button),
                leftIconId = R.drawable.ic_timer,
                leftIconContentDescription = "ic_timer",
                leftIconTintColor = Color.Purple200,
                onCardClick = {
                    viewModel.userClickedOnCustomWorkout()
                }
            )
        }

        item {
            GenericRoundedCard(
                leftText = stringResource(R.string.main_page_emom_timer_button),
                leftIconId = R.drawable.ic_emom,
                leftIconContentDescription = "ic_emom",
                leftIconTintColor = Color.Teal200,
                onCardClick = {
                    viewModel.userClickedOnEmom()
                }
            )
        }

        item {
            GenericRoundedCard(
                leftText = stringResource(R.string.main_page_tabata_timer_button),
                leftIconId = R.drawable.ic_tabata,
                leftIconContentDescription = "ic_tabata",
                leftIconTintColor = Color.Red,
                onCardClick = {
                    viewModel.userClickedOnTabata()
                }
            )
        }

        item {
            GenericRoundedCard(
                leftText = stringResource(R.string.main_page_settings_button),
                leftIconId = R.drawable.ic_settings,
                leftIconContentDescription = "ic_settings",
                leftIconTintColor = Color.White,
                onCardClick = {
                    //navController.navigate(Section.SettingsView.baseRoute)
                }
            )
        }
    }
}

fun goToCustomWorkoutList(navController: NavController) {
    navController.navigate(Section.CustomWorkout.baseRoute)
}
