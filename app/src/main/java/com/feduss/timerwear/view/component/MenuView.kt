package com.feduss.timerwear.view.component

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.view.component.card.GenericRoundedCard
import com.feduss.timerwear.uistate.MenuViewModel
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.Purple200
import com.feduss.timerwear.uistate.extension.Teal200

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MenuView(
    context: Context,
    columnState: ScalingLazyColumnState,
    navController: NavController,
    viewModel: MenuViewModel = hiltViewModel()
) {

    RequestPermission()

    val navUiState by viewModel.navUiState.collectAsState()

    navUiState?.let {
        when(it) {
            is MenuViewModel.NavUiState.GoToCustomWorkout -> {
                goToCustomWorkoutList(
                    navController = navController,
                    workoutType = WorkoutType.CustomWorkout
                )
            }
            is MenuViewModel.NavUiState.GoToEmom -> {
                goToCustomWorkoutList(
                    navController = navController,
                    workoutType = WorkoutType.Emom
                )
            }
            is MenuViewModel.NavUiState.GoToTabata -> {
                goToCustomWorkoutList(
                    navController = navController,
                    workoutType = WorkoutType.Tabata
                )
            }

            MenuViewModel.NavUiState.GoToSettings -> {
                goToSettings(
                    navController = navController
                )
            }
        }
        viewModel.navStateFired()
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
                    viewModel.userClickedOnSettings()
                }
            )
        }
    }
}

fun goToSettings(navController: NavController) {
    navController.navigate(
        Section.Settings.baseRoute
    )
}

@Composable
private fun RequestPermission() {
    val notificationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false) -> {

            }

            permissions.getOrDefault(Manifest.permission.VIBRATE, false) -> {

            }

            else -> {

            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {

                val permissions = ArrayList<String>()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.addAll(
                        listOf(
                            Manifest.permission.POST_NOTIFICATIONS,
                            Manifest.permission.USE_EXACT_ALARM
                        )
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
                }

                permissions.add(Manifest.permission.VIBRATE)

                notificationPermissionRequest.launch(permissions.toTypedArray())
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

fun goToCustomWorkoutList(navController: NavController, workoutType: WorkoutType) {
    navController.navigate(Section.CustomWorkout.withArgs(listOf(workoutType.toString())))
}
