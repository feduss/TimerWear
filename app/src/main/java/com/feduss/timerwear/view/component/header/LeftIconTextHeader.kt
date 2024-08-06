package com.feduss.timerwear.view.component.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.feduss.timerwear.extension.infiniteMarquee

@Composable
fun LeftIconTextHeader(
    title: String,
    leftIconId: Int? = null,
    leftIconContentDescription: String? = null,
    leftIconTintColor: Color? = null
) {

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        if (
            leftIconId != null &&
            leftIconContentDescription != null &&
            leftIconTintColor != null
        ) {
            Icon(
                modifier = Modifier.width(16.dp),
                imageVector = ImageVector.vectorResource(
                    id = leftIconId
                ),
                contentDescription = leftIconContentDescription,
                tint = leftIconTintColor
            )
        }

        Text(
            maxLines = 1,
            modifier = Modifier
                .weight(1f, false)
                .infiniteMarquee,
            text = title
        )
    }
}