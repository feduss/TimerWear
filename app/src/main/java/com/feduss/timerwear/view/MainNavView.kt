package com.feduss.timerwear.view

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavHostState
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.widget.ConfirmationOverlay
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.entity.enums.Params
import com.feduss.timerwear.uistate.factory.getAddCustomWorkoutViewModel
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.uistate.factory.getCustomWorkoutView
import com.feduss.timerwear.uistate.factory.getTimerViewModel
import com.feduss.timerwear.utils.NavUtils
import com.feduss.timerwear.view.ambient.AmbientWarning
import com.feduss.timerwear.view.component.MenuView
import com.feduss.timerwear.view.component.PageView
import com.feduss.timerwear.view.custom_workout.AddCustomWorkoutView
import com.feduss.timerwear.view.custom_workout.CustomWorkoutView
import com.feduss.timerwear.view.settings.SettingsView
import com.feduss.timerwear.view.timer.TimerView
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.ambient.AmbientAware
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MainNavView(
    mainActivity: MainActivity,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit
) {
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
    val navHostState =
        rememberSwipeDismissableNavHostState(swipeToDismissBoxState = swipeToDismissBoxState)
    val navController = rememberSwipeDismissableNavController()

    val startDestination = Section.Navigation.baseRoute

    var endCurvedText: String? by remember {
        mutableStateOf(null)
    }

    val currentScreen by navController.currentBackStackEntryAsState()

    val isAlwaysOnScreen = currentScreen?.destination?.route?.contains(Section.Timer.baseRoute) == true

    val ambientState: MutableState<AmbientState> = remember {
        mutableStateOf(AmbientState.Interactive)
    }

    AmbientAware(
        isAlwaysOnScreen = isAlwaysOnScreen
    ) { ambientStateUpdate ->

        ambientState.value = ambientStateUpdate.ambientState

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
                                last = ScalingLazyColumnDefaults.ItemType.Card
                            )
                        ),
                        ambientState = ambientState
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
                                    last = ScalingLazyColumnDefaults.ItemType.Card
                                )
                            ),
                            ambientState = ambientState
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
                                    last = ScalingLazyColumnDefaults.ItemType.Card
                                )
                            ),
                            ambientState = ambientState
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
                    route = Section.AmbientWarning.parametricRoute,
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
                            ambientState = ambientState
                        ) {
                            AmbientWarning(
                                context = mainActivity,
                                viewModel = hiltViewModel(),
                                onGoToDisplaySettings = {
                                    openDisplaySettings(mainActivity)
                                },
                                onGoToTimer = {
                                    NavUtils.goToExistingWorkout(
                                        context = mainActivity,
                                        navController = navController,
                                        workoutId = workoutId,
                                        workoutType = workoutType,
                                        currentTimerIndex = currentTimerIndex,
                                        currentRepetition = currentRepetition,
                                        currentTimerSecondsRemaining = currentTimerSecondsRemaining,
                                        checkAmbientMode = false
                                    )
                                }
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
                            ambientState = ambientState,
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
                                    currentTimerSecondsRemaining = currentTimerSecondsRemaining?.toDoubleOrNull()
                                ),
                                onTimerSet = { hourTimerEnd: String ->
                                    endCurvedText = hourTimerEnd
                                },
                                onEnterBackgroundState = onEnterBackgroundState,
                                onKeepScreenOn = onKeepScreenOn,
                                ambientState = ambientState
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
                        ),
                        ambientState = ambientState
                    ) {
                        SettingsView(
                            context = mainActivity,
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

fun openDisplaySettings(mainActivity: MainActivity) {
    val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
    mainActivity.startActivity(intent)
}