package com.feduss.timerwear.view.component.card

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.contentColorFor
import com.feduss.timerwear.extension.infiniteMarquee

@Composable
fun GenericRoundedCard(
    leftText: String,
    leftTextColor: Color = Color.White,
    bottomText: String? = null,
    bottomTextColor: Color = Color.White,
    rightText: String? = null,
    rightTextColor: Color = Color.White,
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

    val errorCardColor = Color.Red
    val shape = MaterialTheme.shapes.large
    var modifier = Modifier.fillMaxWidth()

    if (hasValidationError) {
        modifier = modifier.border(
            width = 1.dp,
            color = errorCardColor,
            shape = shape
        )
    }

    val disabledCardColor = MaterialTheme.colors.primary.copy(alpha = ContentAlpha. disabled)
    val disabledContentColor = contentColorFor(disabledCardColor).copy(alpha = ContentAlpha. disabled)
    Card(
        modifier = modifier,
        backgroundPainter =
        if (!isEnabled)
            CardDefaults.cardBackgroundPainter(startBackgroundColor = disabledCardColor)
        else
            CardDefaults.cardBackgroundPainter(),
        shape = shape,
        enabled = isEnabled,
        onClick = onCardClick
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
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
                            .infiniteMarquee,
                        text = leftText,
                        textAlign = TextAlign.Left,
                        color = if (isEnabled) leftTextColor else disabledContentColor
                    )

                    if (bottomText != null) {
                        Text(
                            maxLines = 1,
                            modifier = Modifier
                                .infiniteMarquee,
                            text = bottomText,
                            textAlign = TextAlign.Left,
                            color = if (isEnabled) bottomTextColor else disabledContentColor
                        )
                    }
                }

                if (rightText != null) {
                    Text(
                        text = rightText,
                        color = if (isEnabled) rightTextColor else disabledContentColor
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
