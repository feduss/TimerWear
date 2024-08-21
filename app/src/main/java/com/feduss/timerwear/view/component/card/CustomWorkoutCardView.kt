package com.feduss.timerwear.view.component.card

import com.feduss.timerwear.uistate.uistate.custom_timer.CustomWorkoutCardUiState
import com.feduss.timerwear.uistate.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.RevealActionType
import androidx.wear.compose.foundation.RevealState
import androidx.wear.compose.foundation.RevealValue
import androidx.wear.compose.foundation.SwipeToDismissBoxState
import androidx.wear.compose.foundation.edgeSwipeToDismiss
import androidx.wear.compose.foundation.rememberRevealState
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.SwipeToRevealCard
import androidx.wear.compose.material.SwipeToRevealDefaults
import androidx.wear.compose.material.SwipeToRevealPrimaryAction
import androidx.wear.compose.material.SwipeToRevealSecondaryAction
import androidx.wear.compose.material.SwipeToRevealUndoAction
import androidx.wear.compose.material.Text
import com.feduss.timerwear.extension.infiniteMarquee
import com.feduss.timerwear.uistate.extension.Teal200
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.OnBalloonDismissListener
import com.skydoves.balloon.compose.Balloon
import com.skydoves.balloon.compose.BalloonWindow
import com.skydoves.balloon.compose.rememberBalloonBuilder
import com.skydoves.balloon.overlay.BalloonOverlayAnimation
import com.skydoves.balloon.overlay.BalloonOverlayRect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalWearMaterialApi::class,
    ExperimentalWearFoundationApi::class
)
@Composable
fun CustomWorkoutCardView(
    modifier: Modifier = Modifier,
    state: CustomWorkoutCardUiState,
    swipeToDismissBoxState: SwipeToDismissBoxState,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var balloonJob: Job? = null
    val coroutineScope = rememberCoroutineScope()
    val revealState = rememberRevealState()

    val undoDone = remember {
        mutableStateOf(false)
    }

    val isWorkaroundEnabled = true

    val builder = rememberBalloonBuilder {
        setArrowSize(10)
        setArrowPosition(0.5f)
        setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
        setPadding(12)
        setMarginHorizontal(12)
        setCornerRadius(8f)
        setTextColor(Color.White.toArgb())
        setBackgroundColor(Color.Gray.toArgb())
        setBalloonAnimation(BalloonAnimation.ELASTIC)
        setIsVisibleOverlay(true)
        setOverlayColorResource(R.color.overlay)
        setDismissWhenOverlayClicked(false)
        setOverlayPadding(8f)
        setDismissWhenClicked(true)
        setDismissWhenTouchMargin(true)
        setDismissWhenTouchOutside(true)
        setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE)
        setOverlayShape(BalloonOverlayRect)
        setLifecycleOwner(lifecycleOwner)
        onBalloonDismissListener = OnBalloonDismissListener {
            balloonJob?.cancel()
            state.onBalloonDismissed()
        }
    }

    //Balloon is visible for FlowType.StopList only!
    var balloonWindow: BalloonWindow? by remember { mutableStateOf(null) }

    Balloon(
        builder = builder,
        onBalloonWindowInitialized = { balloonWindow = it },
        onComposedAnchor = {
            if (state.isBalloonEnabled) {
                balloonWindow?.showAlignBottom()

                balloonJob = coroutineScope.launch {
                    repeat(2) {
                        delay(2000)
                        revealState.animateTo(RevealValue.Revealing)
                        delay(2000)
                        revealState.animateTo(RevealValue.Covered)
                        revealState.lastActionType = RevealActionType.None
                    }
                    balloonWindow?.dismiss()
                }
            }
        },
        balloonContent = {
            Text(
                modifier = Modifier.infiniteMarquee,
                text = stringResource(R.string.custom_workout_tooltip_text),
                maxLines = 1
            )
        }
    ) {

        SwipeToRevealCard(
            modifier = modifier
                .fillMaxWidth()
                .edgeSwipeToDismiss(swipeToDismissBoxState = swipeToDismissBoxState)
                .semantics {
                    // Use custom actions to make the primary and secondary actions accessible
                    customActions = listOf(
                        CustomAccessibilityAction("Edit this custom workout") {
                            coroutineScope.launch {
                                revealState.animateTo(RevealValue.Revealed)
                            }
                            state.onEditWorkoutButtonClicked()
                            true
                        },
                        CustomAccessibilityAction("Delete this custom workout") {
                            resetRevealState(
                                coroutineScope = coroutineScope,
                                revealState = revealState,
                                isWorkaroundEnabled = isWorkaroundEnabled
                            )
                            state.onDeleteWorkoutButtonClicked()
                            true
                        }
                    )
                },
            primaryAction = {
                SwipeToRevealPrimaryAction(
                    revealState = revealState,
                    icon = {
                        Icon(
                            modifier = Modifier.width(24.dp),
                            imageVector = ImageVector.vectorResource(
                                id = R.drawable.ic_edit
                            ),
                            contentDescription = "ic_edit",
                            tint = Color.Black
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.custom_workout_card_swipe_primary_action_title),
                            color = Color.Black
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            revealState.animateTo(RevealValue.Revealed)
                        }
                        state.onEditWorkoutButtonClicked()

                    }
                )
            },
            secondaryAction = {
                SwipeToRevealSecondaryAction(
                    revealState = revealState,
                    onClick = {
                        coroutineScope.launch {
                            revealState.animateTo(RevealValue.Revealed)
                            delay(3000)
                            if (!undoDone.value) {
                                state.onDeleteWorkoutButtonClicked()
                                resetRevealState(
                                    coroutineScope = coroutineScope,
                                    revealState = revealState,
                                    isWorkaroundEnabled = isWorkaroundEnabled
                                )
                            }
                            undoDone.value = false
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.width(24.dp),
                        imageVector = ImageVector.vectorResource(
                            id = R.drawable.ic_delete
                        ),
                        contentDescription = "ic_delete",
                        tint = Color.Black
                    )
                }
            },
            undoSecondaryAction = {
                SwipeToRevealUndoAction(
                    revealState = revealState,
                    icon = {
                        Icon(
                            modifier = Modifier.width(24.dp),
                            imageVector = ImageVector.vectorResource(
                                id = R.drawable.ic_restore
                            ),
                            contentDescription = "ic_restore",
                            tint = Color.White
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.custom_workout_card_swipe_secondary_action_undo_button),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    },
                    onClick = {
                        undoDone.value = true
                        resetRevealState(
                            coroutineScope = coroutineScope,
                            revealState = revealState,
                            isWorkaroundEnabled = isWorkaroundEnabled
                        )
                    }
                )
            },
            onFullSwipe = {
                state.onEditWorkoutButtonClicked()
                //TODO: this is a workaround, check slack --> https://kotlinlang.slack.com/archives/C02GBABJUAF/p1711923876665509
                if (isWorkaroundEnabled) {
                    revealState.lastActionType = RevealActionType.None
                }
            },
            revealState = revealState,
            colors = SwipeToRevealDefaults.actionColors(
                primaryActionBackgroundColor = Color.Teal200,
                primaryActionContentColor = Color.Teal200,
                secondaryActionBackgroundColor = Color.Red,
                secondaryActionContentColor = Color.Red,
            )
        ) {
            ContentBody(
                state = state
            )
        }
    }
}

@OptIn(ExperimentalWearFoundationApi::class)
private fun resetRevealState(
    coroutineScope: CoroutineScope,
    revealState: RevealState,
    isWorkaroundEnabled: Boolean
) {
    coroutineScope.launch {
        if (isWorkaroundEnabled) {
            revealState.animateTo(RevealValue.Covered)
        }
    }
}

@Composable
private fun ContentBody(
    state: CustomWorkoutCardUiState
) {
    GenericRoundedCard(
        leftIconId = state.leftIconId,
        leftIconContentDescription = state.leftIconDescription,
        leftIconTintColor = state.leftIconTintColor,
        leftText = state.name,
        bottomText = state.duration,
        onCardClick = {
            state.onCardClicked()
        }
    )
}