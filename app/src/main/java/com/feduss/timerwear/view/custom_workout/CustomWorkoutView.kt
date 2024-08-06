package com.feduss.timerwear.view.custom_workout

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.items
import com.feduss.timerwear.entity.CustomWorkoutModel
import com.feduss.timerwear.entity.enums.Params
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.uistate.uistate.custom_timer.CustomWorkoutViewModel
import com.feduss.timerwear.view.component.card.GenericRoundedCard
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun CustomWorkoutView(
    context: Context,
    viewModel: CustomWorkoutViewModel = hiltViewModel(),
    navController: NavController,
    columnState: ScalingLazyColumnState
) {

    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()

    val coroutineScope = rememberCoroutineScope { Dispatchers.Main }

    LaunchedEffect(coroutineScope) {
        coroutineScope.launch {
            viewModel.getCustomTimerList(context = context)
        }
    }

    navUiState?.let {
        when (it) {
            is CustomWorkoutViewModel.NavUiState.AddCustomTimerClicked -> {
                goToAddCustomWorkoutPage(
                    navController = navController
                )
            }
            is CustomWorkoutViewModel.NavUiState.EditCustomTimerClicked ->
                goToAddCustomWorkoutPage(
                    navController = navController,
                    workoutId = it.id
                )
            is CustomWorkoutViewModel.NavUiState.ExistingCustomTimerClicked -> TODO()
        }
        viewModel.navStateFired()
    }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState
    ) {
        item {
            LeftIconTextHeader(
                title = stringResource(id = viewModel.headerTextId)
            )
        }

        dataUiState?.let { state ->

            state.customWorkouts?.let { customWorkoutState ->
                items(
                    items = customWorkoutState,
                    key = { customWorkoutState.map { it.timerId } }
                ) {
                    GenericRoundedCard(
                        leftIconId = it.leftIconId,
                        leftIconContentDescription = it.leftIconDescription,
                        leftIconTintColor = it.leftIconTintColor,
                        leftText = it.timerName,
                        bottomText = it.timerDuration,
                        onCardClick =  {
                            //TODO
                        }
                    )
                }
            }

            item {
                GenericRoundedCard(
                    leftIconId = state.addCustomWorkoutButton.leftIconId,
                    leftIconContentDescription = state.addCustomWorkoutButton.leftIconDescription,
                    leftIconTintColor = state.addCustomWorkoutButton.leftIconTintColor,
                    leftText = stringResource(id = state.addCustomWorkoutButton.textId),
                    onCardClick =  {
                        viewModel.userHasClickedAddTimer()
                    }

                )
            }
        }
    }
}

private fun goToAddCustomWorkoutPage(
    navController: NavController,
    workoutId: String = ""
) {
    navController.navigate(
        Section.AddCustomWorkout.withArgs(
            optionalArgs = mapOf(Params.WorkoutId.name to workoutId)
        )
    )
}