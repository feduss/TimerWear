package com.feduss.timerwear.view.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.contentColorFor
import com.feduss.timerwear.extension.infiniteMarquee

@Composable
fun TextButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
    title: String,
    backgroundColor: Color = Color.DarkGray,
    contentColor: Color = contentColorFor(backgroundColor)
) {
    Button(
        modifier = modifier.fillMaxWidth(0.8f).infiniteMarquee,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = contentColor
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center
        )
    }
}