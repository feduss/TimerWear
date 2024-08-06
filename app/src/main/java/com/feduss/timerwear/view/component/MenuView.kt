package com.feduss.timerwear.view.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.feduss.timerwear.entity.enums.Section
import com.feduss.timerwear.view.component.card.GenericRoundedCard
import com.feduss.timerwear.uistate.MenuViewModel
import com.feduss.timerwear.view.component.header.LeftIconTextHeader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.feduss.timerwear.uistate.R
import com.feduss.timerwear.uistate.extension.Purple200
import com.feduss.timerwear.uistate.extension.Purple700
import com.feduss.timerwear.uistate.extension.Teal200

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MenuView(
    columnState: ScalingLazyColumnState,
    navController: NavController,
    viewModel: MenuViewModel = hiltViewModel()
) {

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
                    navController.navigate(Section.CustomWorkout.baseRoute)
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
                    //navController.navigate(Section.FavoritesLines.baseRoute)
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
                    //navController.navigate(Section.NearestStop.baseRoute)
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
                    //navController.navigate(Section.SettingsView.baseRoute)
                }
            )
        }
    }
}