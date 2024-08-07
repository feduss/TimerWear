package com.feduss.timerwear.view.custom_workout

import android.content.Context
import android.widget.Toast
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
import androidx.wear.compose.foundation.SwipeToDismissBoxState
import androidx.wear.compose.foundation.lazy.items
import com.feduss.timerwear.entity.enums.Params
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.uistate.uistate.custom_timer.CustomWorkoutViewModel
import com.feduss.timerwear.view.component.card.CustomWorkoutCardView
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
    columnState: ScalingLazyColumnState,
    swipeToDismissBoxState: SwipeToDismissBoxState
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
                TODO()
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
                        //TODO:
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