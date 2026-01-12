package com.feduss.timerwear.view.custom_workout

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.feduss.timerwear.entity.enums.TimerType
import com.feduss.timerwear.extension.infiniteMarquee
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.Purple700
import com.feduss.timerwear.uistate.extension.getStringId
import com.feduss.timerwear.uistate.uistate.add_custom_timer.AddCustomWorkoutViewModel
import com.feduss.timerwear.uistate.uistate.picker.TimerPickerUiState
import com.feduss.timerwear.view.component.button.TextButton
import com.feduss.timerwear.view.component.card.GenericOtherInputCard
import com.feduss.timerwear.view.component.card.GenericRoundedCard
import com.feduss.timerwear.view.component.card.GenericTextInputCard
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.feduss.timerwear.view.component.list.LazyColumnWithScrollbar
import com.feduss.timerwear.view.component.picker.TimerPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomWorkoutView(
    context: Context,
    viewModel: AddCustomWorkoutViewModel = hiltViewModel(),
    navController: NavController,
    columnState: LazyListState
) {
    val dataUiState by viewModel.dataUiState.collectAsState()
    val navUiState by viewModel.navUiState.collectAsState()
    val timerPickerState by viewModel.timerPickerUiState.collectAsState()
    val isAddTimerButtonEnabled = viewModel.isAddTimerButtonEnabled.collectAsState()
    val isConfirmButtonEnabled = viewModel.isConfirmButtonEnabled.collectAsState()

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
    timerPickerState?.let {
        TimerPicker(
            titleId = it.titleId,
            initialMinutesValue = it.initialMinutesValue,
            initialSecondsValue = it.initialSecondsValue,
            onDismiss = it.onValueChanged
        )
    }

    LazyColumnWithScrollbar(
        state = columnState,
        spaceBetweenItems = 4
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).infiniteMarquee(),
                    text = stringResource(id = viewModel.timerSectionTitleId),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            val customTimerUiStates = state.customTimerUiStates
            if (customTimerUiStates != null) {
                items(
                    items = customTimerUiStates,
                    key = { it.uuid }
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
                            viewModel.userHasClickedOnTimer(uuid = it.uuid)
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
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    onValueChange = { newTitle ->
                                        viewModel.userHasEditedTimerTitle(
                                            context = context,
                                            uuid = it.uuid,
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
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    onCardClicked = {
                                        viewModel.userHasOpenedTimerDurationPicker(
                                            context = context,
                                            uuid = it.uuid,
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
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onPrimary
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
                                        FilterChip(
                                            modifier = Modifier.fillMaxWidth(),
                                            label = {
                                                Text(
                                                    text = stringResource(id = type.getStringId()),
                                                    textAlign = TextAlign.Center
                                                )
                                            },
                                            enabled = !it.typeUiState.isReadOnly,
                                            selected = isChecked,
                                            onClick = {
                                                viewModel.userHasClickedWorkoutType(
                                                    context = context,
                                                    uuid = it.uuid,
                                                    type = type,
                                                    isSelected = !isChecked
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(id = iconId),
                                                    contentDescription = "$it icon",
                                                    tint = Color.White
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color.Purple700,
                                                selectedLabelColor = Color.White,
                                                selectedLeadingIconColor = Color.White
                                            ),
                                            trailingIcon = {

                                                RadioButton(
                                                    onClick = {
                                                        viewModel.userHasClickedWorkoutType(
                                                            context = context,
                                                            uuid = it.uuid,
                                                            type = type,
                                                            isSelected = !isChecked
                                                        )
                                                    },
                                                    selected = isChecked,
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = Color.White,
                                                        unselectedColor = Color.White
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
                                                uuid = it.uuid
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
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                TextButton(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(54.dp),
                    enabled = isConfirmButtonEnabled.value,
                    onClick = {
                        viewModel.confirmButtonClicked(context = context)
                    },
                    title = stringResource(id = state.addWorkoutConfirmButtonUiState.textId)
                )
            }

            items(
                items = state.bottomValidationErrors
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    text = it,
                    textAlign = TextAlign.Center,
                    color = Color.Red
                )
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