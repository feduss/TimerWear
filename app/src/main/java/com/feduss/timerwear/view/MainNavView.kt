package com.feduss.timerwear.view

import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavHostState
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.widget.ConfirmationOverlay
import com.feduss.timerwear.entity.enums.Params
import com.feduss.timerwear.uistate.factory.getAddCustomWorkoutViewModel
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.uistate.factory.getCustomWorkoutView
import com.feduss.timerwear.uistate.factory.getTimerViewModel
import com.feduss.timerwear.view.component.MenuView
import com.feduss.timerwear.view.component.PageView
import com.feduss.timerwear.view.custom_workout.AddCustomWorkoutView
import com.feduss.timerwear.view.custom_workout.CustomWorkoutView
import com.feduss.timerwear.view.settings.SettingsView
import com.feduss.timerwear.view.timer.TimerView
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import kotlinx.coroutines.launch

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

    AppScaffold(
        timeText = {}
    ) {
        SwipeDismissableNavHost(
            startDestination = startDestination,
            navController = navController,
            state = navHostState
        ) {

            composable(route = startDestination) {
                PageView(
                    columnState = rememberResponsiveColumnState(
                        contentPadding = ScalingLazyColumnDefaults.padding(
                            first = ScalingLazyColumnDefaults.ItemType.Text,
                            last = ScalingLazyColumnDefaults.ItemType.SingleButton
                        )
                    )
                ) {
                    MenuView(
                        context = mainActivity,
                        columnState = it,
                        navController = navController
                    )
                }
            }

            composable(
                route = Section.CustomWorkout.parametricRoute
            ) { navBackStackEntry ->
                val workoutTypeRaw =
                    navBackStackEntry.arguments?.getString(Params.WorkoutType.name)

                val workoutType = WorkoutType.fromString(workoutTypeRaw)

                if (workoutType == null) {
                    navController.popBackStack()
                } else {
                    PageView(
                        columnState = rememberResponsiveColumnState(
                            contentPadding = ScalingLazyColumnDefaults.padding(
                                first = ScalingLazyColumnDefaults.ItemType.Text,
                                last = ScalingLazyColumnDefaults.ItemType.SingleButton
                            )
                        )
                    ) {
                        CustomWorkoutView(
                            context = mainActivity,
                            viewModel = getCustomWorkoutView(
                                activity = mainActivity,
                                workoutType = workoutType
                            ),
                            columnState = it,
                            navController = navController,
                            swipeToDismissBoxState = swipeToDismissBoxState
                        )
                    }
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
                val workoutTypeRaw =
                    navBackStackEntry.arguments?.getString(Params.WorkoutType.name)

                val workoutType = WorkoutType.fromString(workoutTypeRaw)

                val workoutId =
                    navBackStackEntry.arguments?.getString(Params.WorkoutId.name)

                if (workoutType == null) {
                    navController.popBackStack()
                } else {

                    PageView(
                        columnState = rememberResponsiveColumnState(
                            contentPadding = ScalingLazyColumnDefaults.padding(
                                first = ScalingLazyColumnDefaults.ItemType.Text,
                                last = ScalingLazyColumnDefaults.ItemType.SingleButton
                            )
                        )
                    ) {
                        AddCustomWorkoutView(
                            viewModel = getAddCustomWorkoutViewModel(
                                activity = mainActivity,
                                workoutType = workoutType,
                                workoutId = workoutId?.toIntOrNull()
                            ),
                            context = mainActivity,
                            columnState = it,
                            navController = navController
                        )
                    }
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

                val workoutTypeRaw =
                    navBackStackEntry.arguments?.getString(Params.WorkoutType.name)

                val workoutType = WorkoutType.fromString(workoutTypeRaw)

                val currentTimerIndex =
                    navBackStackEntry.arguments?.getString(Params.CurrentTimerIndex.name)


                val currentRepetition =
                    navBackStackEntry.arguments?.getString(Params.CurrentRepetition.name)


                val currentTimerSecondsRemaining =
                    navBackStackEntry.arguments?.getString(Params.CurrentTimerSecondsRemaining.name)

                if (workoutId == null || workoutType == null) {
                    navController.popBackStack()
                } else {
                    PageView(
                        columnState = rememberColumnState(),
                        endCurvedText = endCurvedText
                    ) {
                        TimerView(
                            context = mainActivity,
                            navController = navController,
                            viewModel = getTimerViewModel(
                                activity = mainActivity,
                                workoutId = workoutId.toInt(),
                                workoutType = workoutType,
                                currentTimerIndex = currentTimerIndex?.toIntOrNull(),
                                currentRepetition = currentRepetition?.toIntOrNull(),
                                currentTimerSecondsRemaining = currentTimerSecondsRemaining?.toIntOrNull()
                            ),
                            onTimerSet = { hourTimerEnd: String ->
                                endCurvedText = hourTimerEnd
                            },
                            onKeepScreenOn = {
                                if (it) {
                                    keepScreenOn(mainActivity)
                                } else {
                                    restoreScreenTimeout(mainActivity)
                                }
                            }
                        )
                    }
                }


            }

            composable(route = Section.Settings.baseRoute) {
                PageView(
                    columnState = rememberResponsiveColumnState(
                        contentPadding = ScalingLazyColumnDefaults.padding(
                            first = ScalingLazyColumnDefaults.ItemType.Text,
                            last = ScalingLazyColumnDefaults.ItemType.Text
                        )
                    )
                ) {
                    SettingsView(
                        columnState = it,
                        onEmailFeedbackTapped = {
                            openEmail(mainActivity)
                        }
                    )
                }
            }
        }
    }
}

fun keepScreenOn(activity: MainActivity) {
    val window = activity.window
    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

fun restoreScreenTimeout(activity: MainActivity) {
    val window = activity.window
    window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

private fun openEmail(activity: MainActivity) {
    val uriText = "mailto:feduss96@gmail.co," +
            "?subject=" + "TimerWear: feedback"
    val uri = Uri.parse(uriText)
    val sendIntent = Intent(Intent.ACTION_VIEW)
    sendIntent.addCategory(Intent.CATEGORY_BROWSABLE)
    sendIntent.data = uri

    val remoteActivityHelper = RemoteActivityHelper(activity)

    activity.lifecycleScope.launch {
        try {
            remoteActivityHelper.startRemoteActivity(sendIntent)
            ConfirmationOverlay()
                .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                .showOn(activity)
        } catch (throwable: Throwable) {
            ConfirmationOverlay()
                .setType(ConfirmationOverlay.FAILURE_ANIMATION)
                .showOn(activity)
        }
    }
}