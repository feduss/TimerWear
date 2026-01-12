package com.feduss.timerwear.view.menu

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.core.app.AlarmManagerCompat.canScheduleExactAlarms
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.feduss.timerwear.view.component.list.LazyColumnWithScrollbar
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.lifecycle.OnLifecycleEvent
import com.feduss.timerwear.uistate.MenuViewModel
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.Purple200
import com.feduss.timerwear.uistate.extension.Teal200
import com.feduss.timerwear.view.component.card.GenericRoundedCard
import com.feduss.timerwear.view.component.header.LeftIconTextHeader

@Composable
fun MenuView(
    context: Context,
    columnState: LazyListState,
    navController: NavController,
    viewModel: MenuViewModel = hiltViewModel(),
    openAlarmSettings: () -> Unit
) {

    val alarmManager by remember {
        mutableStateOf(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
    }

    val canScheduleExactAlarms = viewModel.canScheduleExactAlarms.collectAsState()

    RequestPermission(
        alarmManager = alarmManager,
        viewModel = viewModel
    )

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
            is MenuViewModel.NavUiState.GoToHiit -> {
                goToCustomWorkoutList(
                    navController = navController,
                    workoutType = WorkoutType.Hiit
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

    LazyColumnWithScrollbar(
        state = columnState,
        spaceBetweenItems = 4
    ) {
        item {
            LeftIconTextHeader(
                title = "TimerWear"
            )
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            canScheduleExactAlarms.value != null &&
            canScheduleExactAlarms.value == false
        ) {

            item {
                GenericRoundedCard(
                    leftText = stringResource(viewModel.scheduleAlarmWarningId),
                    leftIconId = viewModel.warningIconId,
                    leftIconContentDescription = "ic_warning",
                    leftIconTintColor = Color.Yellow,
                    onCardClick = {
                        openAlarmSettings()
                    }
                )
            }
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
                leftText = stringResource(R.string.main_page_hiit_timer_button),
                leftIconId = R.drawable.ic_hiit,
                leftIconContentDescription = "ic_hiit",
                leftIconTintColor = Color.Red,
                onCardClick = {
                    viewModel.userClickedOnHiit()
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
private fun RequestPermission(
    alarmManager: AlarmManager,
    viewModel: MenuViewModel
) {
    val notificationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false) -> {}
            permissions.getOrDefault(Manifest.permission.VIBRATE, false) -> {}
            else -> {}
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {

                viewModel.updateExactAlarmSchedulingPermission(newValue = canScheduleExactAlarms(alarmManager))

                val permissions = ArrayList<String>()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }

                permissions.add(Manifest.permission.VIBRATE)

                notificationPermissionRequest.launch(permissions.toTypedArray())
            }
            else -> { }
        }
    }
}

fun goToCustomWorkoutList(navController: NavController, workoutType: WorkoutType) {
    navController.navigate(Section.CustomWorkout.withArgs(listOf(workoutType.toString())))
}
