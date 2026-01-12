package com.feduss.timerwear.view.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feduss.timerwear.uistate.extension.PurpleCustom

@Composable
fun CustomAlertDialog(
    titleId: Int,
    subtitle: Int? = null,
    negativeButtonIconId: Int,
    negativeButtonIconDesc: String,
    negativeButtonClicked: () -> Unit,
    positiveButtonIconId: Int,
    positiveButtonIconDesc: String,
    positiveButtonClicked: () -> Unit
) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(titleId),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        onDismissRequest = negativeButtonClicked,
        dismissButton = {
            val color = Color.DarkGray
            IconButton(
                modifier = Modifier
                    .width(48.dp)
                    .aspectRatio(1f)
                    .background(
                        color = color,
                        shape = CircleShape
                    ),
                colors = IconButtonDefaults.iconButtonColors(color, color),
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            id = negativeButtonIconId
                        ),
                        contentDescription = negativeButtonIconDesc,
                        tint = Color.White
                    )
                },
                onClick = negativeButtonClicked
            )
        },
        confirmButton = {
            val color = Color.PurpleCustom
            IconButton(
                modifier = Modifier
                    .width(48.dp)
                    .aspectRatio(1f)
                    .background(
                        color = color,
                        shape = CircleShape
                    ),
                colors = IconButtonDefaults.iconButtonColors(color, color),
                content = {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            id = positiveButtonIconId
                        ),
                        contentDescription = positiveButtonIconDesc,
                        tint = Color.Black
                    )
                },
                onClick = positiveButtonClicked
            )
        },
        text = {
            subtitle?.let {
                Text(
                    text = stringResource(it),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}