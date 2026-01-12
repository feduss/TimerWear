package com.feduss.timerwear.view.component.card

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feduss.timerwear.extension.infiniteMarquee

@Composable
fun GenericRoundedCard(
    leftText: String,
    bottomText: String? = null,
    rightText: String? = null,
    leftIconId: Int? = null,
    leftIconContentDescription: String? = null,
    leftIconTintColor: Color? = null,
    rightIconId: Int? = null,
    rightIconContentDescription: String? = null,
    rightIconTintColor: Color? = null,
    rightIconSize: Dp = 24.dp,
    isExpanded: Boolean = false,
    onCardClick: () -> Unit = {},
    isEnabled: Boolean = true,
    hasValidationError: Boolean = false,
    expandedContent:  @Composable (() -> Unit) = {}
) {

    var customModifier =
        if (expandedContent != {} )
            Modifier
            .padding(bottom = 0.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            )
        else Modifier

    val errorCardColor = MaterialTheme.colorScheme.error
    val shape = MaterialTheme.shapes.large

    if (hasValidationError) {
        customModifier = customModifier.border(
            width = 1.dp,
            color = errorCardColor,
            shape = shape
        )
    }

    val DisabledAlpha = 0.38f
    val disabledCardColor = MaterialTheme.colorScheme.primary.copy(alpha = DisabledAlpha)
    val disabledContentColor = contentColorFor(disabledCardColor).copy(alpha = DisabledAlpha)
    Card(
        modifier = customModifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = disabledCardColor,
            disabledContentColor = disabledContentColor
        ),
        enabled = isEnabled,
        onClick = onCardClick
    ) {

        Column(
            Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (
                    leftIconId != null &&
                    leftIconContentDescription != null &&
                    leftIconTintColor != null
                ) {
                    Icon(
                        modifier = Modifier.width(24.dp),
                        imageVector = ImageVector.vectorResource(
                            id = leftIconId
                        ),
                        contentDescription = leftIconContentDescription,
                        tint = if (isEnabled) leftIconTintColor else disabledContentColor
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        maxLines = 1,
                        modifier = Modifier
                            .infiniteMarquee(),
                        text = leftText,
                        textAlign = TextAlign.Left,
                        color = if (isEnabled) MaterialTheme.colorScheme.onPrimary else disabledContentColor
                    )

                    if (bottomText != null) {
                        Text(
                            maxLines = 1,
                            modifier = Modifier
                                .infiniteMarquee(),
                            text = bottomText,
                            textAlign = TextAlign.Left,
                            color = if (isEnabled) MaterialTheme.colorScheme.onPrimary else disabledContentColor
                        )
                    }
                }

                if (rightText != null) {
                    Text(
                        text = rightText,
                        color = if (isEnabled) MaterialTheme.colorScheme.onPrimary else disabledContentColor
                    )
                }

                if (
                    rightIconId != null &&
                    rightIconContentDescription != null &&
                    rightIconTintColor != null
                ) {
                    Icon(
                        modifier = Modifier.width(rightIconSize),
                        imageVector = ImageVector.vectorResource(
                            id = rightIconId
                        ),
                        contentDescription = rightIconContentDescription,
                        tint = if (isEnabled) rightIconTintColor else disabledContentColor
                    )
                }
            }

            if (isEnabled && isExpanded) {
                expandedContent()
            }
        }
    }
}
