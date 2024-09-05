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
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.uistate.uistate.custom_timer.CustomWorkoutViewModel
import com.feduss.timerwear.utils.AmbientUtils
import com.feduss.timerwear.utils.NavUtils
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
                NavUtils.goToAddCustomWorkoutPage(
                    navController = navController,
                    workoutType = it.workoutType
                )
            }
            is CustomWorkoutViewModel.NavUiState.EditCustomWorkoutClicked ->
                NavUtils.goToAddCustomWorkoutPage(
                    navController = navController,
                    workoutId = it.workoutId.toString(),
                    workoutType = it.workoutType
                )
            is CustomWorkoutViewModel.NavUiState.ExistingCustomWorkoutClicked -> {
                NavUtils.goToExistingWorkout(
                    context = context,
                    navController = navController,
                    workoutId = it.workoutId.toString(),
                    workoutType = it.workoutType,
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
        dataUiState?.let { state ->

            item {
                LeftIconTextHeader(
                    title = stringResource(id = state.headerTitleId)
                )
            }

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