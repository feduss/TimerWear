package com.feduss.timerwear.view.custom_workout

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.RadioButtonDefaults
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.extension.infiniteMarquee
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.getStringId
import com.feduss.timerwear.uistate.uistate.add_custom_timer.AddCustomWorkoutViewModel
import com.feduss.timerwear.uistate.uistate.picker.TimerPickerUiState
import com.feduss.timerwear.view.component.button.TextButton
import com.feduss.timerwear.view.component.card.GenericOtherInputCard
import com.feduss.timerwear.view.component.card.GenericRoundedCard
import com.feduss.timerwear.view.component.card.GenericTextInputCard
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.feduss.timerwear.view.component.picker.TimerPicker
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun AddCustomWorkoutView(
    context: Context,
    viewModel: AddCustomWorkoutViewModel = hiltViewModel(),
    navController: NavController,
    columnState: ScalingLazyColumnState
) {
    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()
    val timerPickerState by viewModel.timerPickerUiState.collectAsState()
    val isAddTimerButtonEnabled = viewModel.isAddTimerButtonEnabled.collectAsState()
    val isConfirmButtonEnabled = viewModel.isConfirmButtonEnabled.collectAsState()

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()

    LaunchedEffect(Unit) {
        viewModel.loadUiState(context)
    }

    navUiState?.let {
        when (it) {
            is AddCustomWorkoutViewModel.NavUiState.GoBackToCustomWorkoutList ->
                navController.popBackStack()
        }
        viewModel.navStateFired()
    }

    BackHandler {
        handleBackButton(timerPickerState, viewModel, navController)
    }

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        onDismissed = {
            handleBackButton(timerPickerState, viewModel, navController)
        }
    ) {
        timerPickerState?.let {
            TimerPicker(
                titleId = it.titleId,
                initialMinutesValue = it.initialMinutesValue,
                initialSecondsValue = it.initialSecondsValue,
                onValuesConfirmed = it.onValueChanged
            )
        } ?:
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

                item {
                    GenericTextInputCard(
                        titleId = state.titleUiState.titleId,
                        placeholderId = state.titleUiState.placeholderId,
                        value = state.titleUiState.value,
                        keyboardType = state.titleUiState.keyboardType,
                        errorTextId = state.titleUiState.errorTextId,
                        onValueChange = {
                            viewModel.userHasEditedWorkoutTitle(
                                context = context,
                                newValue = it
                            )
                        }
                    )
                }

                item {
                    GenericTextInputCard(
                        titleId = state.repetitionsUiState.titleId,
                        placeholderId = state.repetitionsUiState.placeholderId,
                        value = state.repetitionsUiState.value,
                        keyboardType = state.repetitionsUiState.keyboardType,
                        errorTextId = state.repetitionsUiState.errorTextId,
                        onValueChange = {
                            viewModel.userHasEditedWorkoutRepetition(
                                context = context,
                                newValue = it
                            )
                        }
                    )
                }

                val repetitionsValue = state.repetitionsUiState.value
                val intermediumRestUiState = state.intermediumRestUiState
                val intermediumRestFrequencyUiState = state.intermediumRestFrequencyUiState
                if (
                    intermediumRestUiState != null &&
                    intermediumRestFrequencyUiState != null &&
                    repetitionsValue.isNotEmpty() &&
                    repetitionsValue.toInt() > 1
                    ) {
                    item {
                        val titleId = intermediumRestUiState.titleId
                        val value: String =
                            if (intermediumRestUiState.value != null) {
                                intermediumRestUiState.value.toString()
                            } else {
                                ""
                            }
                        GenericOtherInputCard(
                            titleId = titleId,
                            placeholderId = intermediumRestUiState.placeholderId,
                            value = value,
                            errorTextId = intermediumRestUiState.errorTextId,
                            onCardClicked = {
                                viewModel.userHasOpenedIntermediumRestPicker(
                                    context = context,
                                    titleId = titleId,
                                    newModel = intermediumRestUiState.value
                                )
                            }
                        )
                    }

                    item {
                        GenericTextInputCard(
                            titleId = intermediumRestFrequencyUiState.titleId,
                            placeholderId = intermediumRestFrequencyUiState.placeholderId,
                            value = intermediumRestFrequencyUiState.value,
                            keyboardType = intermediumRestFrequencyUiState.keyboardType,
                            errorTextId = intermediumRestFrequencyUiState.errorTextId,
                            onValueChange = {
                                viewModel.userHasEditedWorkoutIntermediumRestFrequency(
                                    context = context,
                                    newValue = it
                                )
                            }
                        )
                    }
                }

                item {
                    Text(
                        modifier = Modifier.infiniteMarquee,
                        text = stringResource(id = viewModel.timerSectionTitleId),
                        textAlign = TextAlign.Center
                    )
                }

                val customTimerUiStates = state.customTimerUiStates
                if (customTimerUiStates != null) {
                    items(
                        items = customTimerUiStates,
                        key = { it.id }
                    ) {
                        GenericRoundedCard(
                            leftIconId = it.leftIconId,
                            leftIconContentDescription = it.leftIconDescription,
                            leftIconTintColor = it.leftIconTintColor,
                            leftText = it.nameUiState.value,
                            rightIconId = it.rightIconId,
                            rightIconContentDescription = it.rightIconDescription,
                            rightIconTintColor = it.rightIconTintColor,
                            bottomText = it.durationUiState.value?.toString(),
                            isExpanded = it.isExpanded,
                            hasValidationError = !it.isExpanded && !it.isValid,
                            onCardClick = {
                                viewModel.userHasClickedOnTimer(id = it.id)
                            },
                            expandedContent = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    GenericTextInputCard(
                                        titleId = it.nameUiState.titleId,
                                        placeholderId = it.nameUiState.placeholderId,
                                        value = it.nameUiState.value,
                                        keyboardType = it.nameUiState.keyboardType,
                                        isReadOnly = it.isNameReadOnly,
                                        errorTextId = it.nameUiState.errorTextId,
                                        onValueChange = { newTitle ->
                                            viewModel.userHasEditedTimerTitle(
                                                context = context,
                                                id = it.id,
                                                newTitle = newTitle
                                            )
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    val titleId = it.durationUiState.titleId
                                    val value: String =
                                        if (it.durationUiState.value != null) {
                                            it.durationUiState.value.toString()
                                        } else {
                                            ""
                                        }
                                    GenericOtherInputCard(
                                        titleId = titleId,
                                        placeholderId = it.durationUiState.placeholderId,
                                        value = value,
                                        errorTextId = it.durationUiState.errorTextId,
                                        onCardClicked = {
                                            viewModel.userHasOpenedTimerDurationPicker(
                                                context = context,
                                                id = it.id,
                                                titleId = titleId,
                                                newModel = it.durationUiState.value
                                            )
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(id = it.typeUiState.titleId),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val timerTypes =
                                            TimerType.entries.filter { type -> type != TimerType.IntermediumRest }
                                        timerTypes.forEachIndexed { index, type ->
                                            val iconId: Int =
                                                when (type) {
                                                    TimerType.Work -> R.drawable.ic_run
                                                    TimerType.Rest -> R.drawable.ic_bed
                                                    TimerType.IntermediumRest -> R.drawable.ic_bed
                                                }
                                            val isChecked = it.typeUiState.value == type
                                            ToggleChip(
                                                modifier = Modifier.fillMaxWidth(),
                                                label = {
                                                    Text(
                                                        text = stringResource(id = type.getStringId()),
                                                        textAlign = TextAlign.Center
                                                    )
                                                },
                                                enabled = !it.typeUiState.isReadOnly,
                                                checked = isChecked,
                                                onCheckedChange = { isSelected ->
                                                    viewModel.userHasClickedWorkoutType(
                                                        context = context,
                                                        id = it.id,
                                                        type = type,
                                                        isSelected = isSelected
                                                    )
                                                },
                                                appIcon = {
                                                    Icon(
                                                        imageVector = ImageVector.vectorResource(id = iconId),
                                                        contentDescription = "$it icon"
                                                    )
                                                },
                                                colors = ToggleChipDefaults.toggleChipColors(
                                                    checkedStartBackgroundColor = Color.DarkGray,
                                                    checkedContentColor = Color.White,
                                                    checkedToggleControlColor = Color.White,
                                                    uncheckedContentColor = Color.White,
                                                    uncheckedToggleControlColor = Color.White
                                                ),
                                                toggleControl = {

                                                    RadioButton(
                                                        selected = isChecked,
                                                        colors = RadioButtonDefaults.colors(
                                                            selectedRingColor = Color.White,
                                                            selectedDotColor = Color.White,
                                                            unselectedRingColor = Color.White,
                                                            unselectedDotColor = Color.White
                                                        )
                                                    )
                                                }
                                            )

                                            if (index < timerTypes.count() - 1) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Delete timer buttons isn't visible for emom and hiit
                                    val removeButtonTextId = it.removeButtonTextId
                                    if (removeButtonTextId != null) {
                                        Text(
                                            modifier = Modifier.clickable {
                                                viewModel.userHasRemovedTimer(
                                                    context = context,
                                                    id = it.id
                                                )
                                            },
                                            text = stringResource(id = removeButtonTextId),
                                            textAlign = TextAlign.Center,
                                            color = Color.Red
                                        )
                                    }

                                }
                            }

                        )
                    }
                }

                val addTimerButtonUiState = state.addTimerButtonUiState
                if (addTimerButtonUiState != null) {
                    item {
                        GenericRoundedCard(
                            leftIconId = addTimerButtonUiState.leftIconId,
                            leftIconContentDescription = addTimerButtonUiState.leftIconDescription,
                            leftIconTintColor = addTimerButtonUiState.leftIconTintColor,
                            leftText = stringResource(id = addTimerButtonUiState.textId),
                            isEnabled = isAddTimerButtonEnabled.value,
                            onCardClick = {
                                viewModel.userHasAddedNewTimer(context = context)
                            }

                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    TextButton(
                        enabled = isConfirmButtonEnabled.value,
                        onClick = {
                            viewModel.confirmButtonClicked(context = context)
                        },
                        title = stringResource(id = state.addWorkoutConfirmButtonUiState.textId)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(
                    items = state.bottomValidationErrors
                ) {
                    Text(
                        text = it,
                        textAlign = TextAlign.Center,
                        fontSize = TextUnit(10f, TextUnitType.Sp),
                        color = Color.Red
                    )
                }
            }
        }
    }
}

private fun handleBackButton(
    timerPickerState: TimerPickerUiState?,
    viewModel: AddCustomWorkoutViewModel,
    navController: NavController
) {
    if (timerPickerState != null) {
        viewModel.userHasDismissedTimerPicker()
    } else {
        navController.popBackStack()
    }
}