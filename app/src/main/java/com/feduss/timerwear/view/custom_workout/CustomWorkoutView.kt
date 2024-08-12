package com.feduss.timerwear.view.custom_workout

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.foundation.SwipeToDismissBoxState
import androidx.wear.compose.foundation.lazy.items
import com.feduss.timerwear.entity.enums.Params
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.uistate.uistate.custom_timer.CustomWorkoutViewModel
import com.feduss.timerwear.view.component.card.CustomWorkoutCardView
import com.feduss.timerwear.view.component.card.GenericRoundedCard
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun CustomWorkoutView(
    context: Context,
    viewModel: CustomWorkoutViewModel = hiltViewModel(),
    navController: NavController,
    columnState: ScalingLazyColumnState,
    swipeToDismissBoxState: SwipeToDismissBoxState
) {

    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()

    viewModel.loadUiState(context = context)

    LaunchedEffect(Unit) {
        viewModel.checkActiveTimer(context)
    }

    navUiState?.let {
        when (it) {
            is CustomWorkoutViewModel.NavUiState.AddCustomWorkoutClicked -> {
                goToAddCustomWorkoutPage(
                    navController = navController
                )
            }
            is CustomWorkoutViewModel.NavUiState.EditCustomWorkoutClicked ->
                goToAddCustomWorkoutPage(
                    navController = navController,
                    workoutId = it.id.toString()
                )
            is CustomWorkoutViewModel.NavUiState.ExistingCustomWorkoutClicked -> {
                goToExistingWorkout(
                    navController = navController,
                    workoutId = it.workoutId.toString(),
                    currentTimerIndex = it.currentTimerIndex?.toString(),
                    currentRepetition = it.currentRepetition?.toString(),
                    currentTimerSecondsRemaining = it.currentTimerSecondsRemaining?.toString()
                )
            }

            is CustomWorkoutViewModel.NavUiState.BalloonDismissed -> {
                viewModel.balloonDismissed(context = context)
            }

            is CustomWorkoutViewModel.NavUiState.CustomWorkoutDeleted -> {
                Toast.makeText(
                    context,
                    stringResource(id = it.textId),
                    Toast.LENGTH_SHORT
                ).show()
            }

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
                    items = customWorkoutState
                ) { state ->

                    state.onCardClicked = {
                        viewModel.userHasClickedExistingWorkout(state.id)
                    }

                    state.onEditWorkoutButtonClicked = {
                        viewModel.userHasClickedEditCustomWorkout(
                            id = state.id
                        )
                    }

                    state.onDeleteWorkoutButtonClicked = {
                        viewModel.userHasClickedDeleteCustomWorkout(
                            context = context,
                            id = state.id
                        )
                    }

                    state.onBalloonDismissed = {
                        viewModel.onBalloonDismissed()
                    }

                    CustomWorkoutCardView(
                        state = state,
                        swipeToDismissBoxState = swipeToDismissBoxState
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
                        viewModel.userHasClickedAddCustomWorkout()
                    }

                )
            }
        }
    }
}

fun goToExistingWorkout(
    navController: NavController, workoutId: String, currentTimerIndex: String?,
    currentRepetition: String?, currentTimerSecondsRemaining: String?
) {

    var optionalArgs: Map<String, String>? = null
    if (currentTimerIndex != null && currentRepetition != null && currentTimerSecondsRemaining != null) {
        optionalArgs = mapOf(
            Params.CurrentTimerIndex.name to currentTimerIndex,
            Params.CurrentRepetition.name to currentRepetition,
            Params.CurrentTimerSecondsRemaining.name to currentTimerSecondsRemaining
        )
    }

    navController.navigate(Section.Timer.withArgs(
        args = listOf(workoutId, TimerType.CustomWorkout.toString()),
        optionalArgs = optionalArgs
    ))
}

private fun goToAddCustomWorkoutPage(
    navController: NavController,
    workoutId: String? = null
) {

    var optionalArgs: Map<String, String>? = null
    if (workoutId != null) {
        optionalArgs = mapOf(Params.WorkoutId.name to workoutId)
    }
    navController.navigate(
        Section.AddCustomWorkout.withArgs(
            optionalArgs = optionalArgs
        )
    )
}