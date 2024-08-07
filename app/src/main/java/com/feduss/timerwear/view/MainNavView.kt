package com.feduss.timerwear.view

import androidx.compose.runtime.Composable
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
import com.feduss.timerwear.view.component.MenuView
import com.feduss.timerwear.view.component.PageView
import com.feduss.timerwear.view.custom_workout.AddCustomWorkoutView
import com.feduss.timerwear.view.custom_workout.CustomWorkoutView
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

    AppScaffold {
        SwipeDismissableNavHost(
            startDestination = startDestination,
            navController = navController,
            state = navHostState
        ) {

            composable(route = startDestination) {
                PageView {
                    MenuView(
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
                    navArgument(Params.WorkoutId.name) { type = NavType.StringType },
                )
            ) { navBackStackEntry ->
                val workoutId: String =
                    navBackStackEntry.arguments?.getString(Params.WorkoutId.name) ?: ""
                PageView {
                    AddCustomWorkoutView(
                        viewModel = getAddCustomWorkoutViewModel(
                            activity = mainActivity,
                            workoutId = workoutId
                        ),
                        context = mainActivity,
                        columnState = it,
                        navController = navController
                    )
                }
            }
        }
    }
}