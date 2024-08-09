package com.feduss.timerwear.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavHostState
import com.feduss.timerwear.entity.enums.Params
import com.feduss.timerwear.uistate.factory.getAddCustomWorkoutViewModel
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.uistate.factory.getTimerViewModel
import com.feduss.timerwear.view.component.MenuView
import com.feduss.timerwear.view.component.PageView
import com.feduss.timerwear.view.custom_workout.AddCustomWorkoutView
import com.feduss.timerwear.view.custom_workout.CustomWorkoutView
import com.feduss.timerwear.view.timer.TimerView
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MainNavView(
    mainActivity: MainActivity
) {
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
    val navHostState =
        rememberSwipeDismissableNavHostState(swipeToDismissBoxState = swipeToDismissBoxState)
    val navController = rememberSwipeDismissableNavController()

    val startDestination = Section.Navigation.baseRoute

    var endCurvedText: String? by remember {
        mutableStateOf(null)
    }

    AppScaffold {
        SwipeDismissableNavHost(
            startDestination = startDestination,
            navController = navController,
            state = navHostState
        ) {

            composable(route = startDestination) {
                PageView {
                    MenuView(
                        context = mainActivity,
                        columnState = it,
                        navController = navController
                    )
                }
            }

            composable(route = Section.CustomWorkout.baseRoute) {
                PageView {
                    CustomWorkoutView(
                        context = mainActivity,
                        columnState = it,
                        navController = navController,
                        swipeToDismissBoxState = swipeToDismissBoxState
                    )
                }
            }

            composable(
                route = Section.AddCustomWorkout.parametricRoute,
                arguments = listOf(
                    navArgument(Params.WorkoutId.name) {
                        nullable = true
                        type = NavType.StringType
                    },
                )
            ) { navBackStackEntry ->
                val workoutId =
                    navBackStackEntry.arguments?.getString(Params.WorkoutId.name)
                PageView {
                    AddCustomWorkoutView(
                        viewModel = getAddCustomWorkoutViewModel(
                            activity = mainActivity,
                            workoutId = workoutId?.toIntOrNull()
                        ),
                        context = mainActivity,
                        columnState = it,
                        navController = navController
                    )
                }
            }

            composable(
                route = Section.Timer.parametricRoute,
                arguments = listOf(
                    navArgument(Params.WorkoutId.name) { type = NavType.StringType },
                    navArgument(Params.CurrentTimerIndex.name) {
                        nullable = true
                        type = NavType.StringType
                    },
                    navArgument(Params.CurrentRepetition.name) {
                        nullable = true
                        type = NavType.StringType
                    },
                    navArgument(Params.CurrentTimerSecondsRemaining.name) {
                        nullable = true
                        type = NavType.StringType
                    },
                )
            ) { navBackStackEntry ->
                val workoutId =
                    navBackStackEntry.arguments?.getString(Params.WorkoutId.name)


                val currentTimerIndex =
                    navBackStackEntry.arguments?.getString(Params.CurrentTimerIndex.name)


                val currentRepetition =
                    navBackStackEntry.arguments?.getString(Params.CurrentRepetition.name)


                val currentTimerSecondsRemaining =
                    navBackStackEntry.arguments?.getString(Params.CurrentTimerSecondsRemaining.name)

                if (workoutId == null) {
                    navController.popBackStack()
                } else {
                    PageView(
                        endCurvedText = endCurvedText
                    ) {
                        TimerView(
                            context = mainActivity,
                            navController = navController,
                            viewModel = getTimerViewModel(
                                activity = mainActivity,
                                workoutId = workoutId.toInt(),
                                currentTimerIndex = currentTimerIndex?.toIntOrNull(),
                                currentRepetition = currentRepetition?.toIntOrNull(),
                                currentTimerSecondsRemaining = currentTimerSecondsRemaining?.toIntOrNull()
                            ),
                            onTimerSet = { hourTimerEnd: String ->
                                endCurvedText = hourTimerEnd
                            },
                        )
                    }
                }


            }
        }
    }
}