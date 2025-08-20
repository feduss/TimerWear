package com.feduss.timerwear.view.component

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.app.AlarmManagerCompat.canScheduleExactAlarms
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.CompactButton
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.entity.enums.WorkoutType
import com.feduss.timerwear.lifecycle.OnLifecycleEvent
import com.feduss.timerwear.uistate.MenuViewModel
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.Purple200
import com.feduss.timerwear.uistate.extension.Teal200
import com.feduss.timerwear.view.component.card.GenericRoundedCard
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MenuView(
    context: Context,
    columnState: ScalingLazyColumnState,
    navController: NavController,
    viewModel: MenuViewModel = hiltViewModel(),
    openAlarmSettings: () -> Unit
) {

    val alarmManager by remember {
        mutableStateOf(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
    }

    val canScheduleExactAlarms = remember {
        mutableStateOf(false)
    }

    RequestPermission(
        alarmManager = alarmManager,
        canScheduleExactAlarms = canScheduleExactAlarms
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

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState
    ) {
        item {
            LeftIconTextHeader(
                title = "TimerWear"
            )
        }

        if (!canScheduleExactAlarms.value) {

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = stringResource(viewModel.scheduleAlarmWarningId),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontSize = TextUnit(10f, TextUnitType.Sp),
                        lineHeight = TextUnit(16f, TextUnitType.Sp)
                    )

                    CompactButton(
                        modifier = Modifier
                            .width(32.dp)
                            .aspectRatio(1f),
                        //colors = ButtonDefaults.primaryButtonColors(defaultColor, defaultColor),
                        content = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_alarm_settings),
                                contentDescription = "Alarm settings icon",
                                tint = Color.Black
                            )
                        },
                        shape = CircleShape,
                        onClick = {
                            openAlarmSettings()
                        }
                    )

                    Spacer(Modifier.height(0.dp))
                }
            }
        }

        item {
            GenericRoundedCard(
                leftText = stringResource(R.string.main_page_custom_workout_button),
                leftIconId = R.drawable.ic_timer,
                leftIconContentDescription = "ic_timer",
                leftIconTintColor = Color.Purple200,
                isEnabled = canScheduleExactAlarms.value,
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
                isEnabled = canScheduleExactAlarms.value,
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
                isEnabled = canScheduleExactAlarms.value,
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
    canScheduleExactAlarms: MutableState<Boolean>
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

                canScheduleExactAlarms.value = canScheduleExactAlarms(alarmManager)

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
