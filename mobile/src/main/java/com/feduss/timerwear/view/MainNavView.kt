package com.feduss.timerwear.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.feduss.timerwear.entity.enums.BackgroundAlarmType
import com.feduss.timerwear.entity.enums.Params
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.uistate.factory.getAddCustomWorkoutViewModel
import com.feduss.timerwear.uistate.factory.getCustomWorkoutView
import com.feduss.timerwear.uistate.factory.getTimerViewModel
import com.feduss.timerwear.utils.NavUtils
import com.feduss.timerwear.view.component.MenuView
import com.feduss.timerwear.view.custom_workout.AddCustomWorkoutView
import com.feduss.timerwear.view.custom_workout.CustomWorkoutView
import com.feduss.timerwear.view.settings.SettingsView
import com.feduss.timerwear.view.timer.TimerView

@Composable
fun MainNavView(
    mainActivity: MainActivity,
    onEnterBackgroundState: (BackgroundAlarmType, Boolean) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    val startDestination = Section.Navigation.baseRoute

    var endCurvedText: String? by remember {
        mutableStateOf(null)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(WindowInsets.systemBars.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NavHost(
            modifier = Modifier.weight(1f),
            startDestination = startDestination,
            navController = navController
        ) {
                composable(route = startDestination) {
                    MenuView(
                        context = mainActivity,
                        columnState = rememberLazyListState(),
                        navController = navController,
                        openAlarmSettings =  {
                            openScheduleExactAlarmPermissionSetting(mainActivity)
                        },
                    )
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
                        CustomWorkoutView(
                            context = mainActivity,
                            viewModel = getCustomWorkoutView(
                                activity = mainActivity,
                                workoutType = workoutType
                            ),
                            columnState = rememberLazyListState(),
                            navController = navController
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
                    val workoutTypeRaw =
                        navBackStackEntry.arguments?.getString(Params.WorkoutType.name)

                    val workoutType = WorkoutType.fromString(workoutTypeRaw)

                    val workoutId =
                        navBackStackEntry.arguments?.getString(Params.WorkoutId.name)

                    if (workoutType == null) {
                        navController.popBackStack()
                    } else {
                        AddCustomWorkoutView(
                            viewModel = getAddCustomWorkoutViewModel(
                                activity = mainActivity,
                                workoutType = workoutType,
                                workoutId = workoutId?.toIntOrNull()
                            ),
                            context = mainActivity,
                            columnState = rememberLazyListState(),
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
                            onKeepScreenOn = onKeepScreenOn
                        )
                    }
                }

                composable(route = Section.Settings.baseRoute) {
                    SettingsView(
                        context = mainActivity,
                        navController = navController,
                        columnState = rememberLazyListState(),
                        onEmailFeedbackTapped = {
                            openEmail(mainActivity)
                        }
                    )
                }
            }
        }
}

private fun openEmail(activity: MainActivity) {
    val uriText = "mailto:feduss96@gmail.co," +
            "?subject=" + "TimerWear: feedback"
    val uri = uriText.toUri()
    val sendIntent = Intent(Intent.ACTION_VIEW)
    sendIntent.addCategory(Intent.CATEGORY_BROWSABLE)
    sendIntent.data = uri
    activity.startActivity(sendIntent)
}

fun openDisplaySettings(mainActivity: MainActivity) {
    val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
    mainActivity.startActivity(intent)
}

@RequiresApi(Build.VERSION_CODES.S)
fun openScheduleExactAlarmPermissionSetting(activity: MainActivity) {
    val intent = Intent(
        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
        Uri.fromParts("package", activity.packageName, null)
    )
    activity.startActivity(intent)


}