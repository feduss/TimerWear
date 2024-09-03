package com.feduss.timerwear.view.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.LocalTextStyle
import androidx.wear.compose.material.Text
import com.feduss.timerwear.extension.infiniteMarquee

@Composable
fun GenericTextInputCard(
    titleId: Int,
    placeholderId: Int,
    value: String,
    keyboardType: KeyboardType,
    isReadOnly: Boolean = false,
    errorTextId: Int?,
    onValueChange: (String) -> Unit
) {

    val defaultColor = Color.DarkGray
    val errorColor = Color.Red
    val shape = RoundedCornerShape(16.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.infiniteMarquee,
            text = stringResource(id = titleId),
            textAlign = TextAlign.Start
        )

        val customTextSelectionColors = TextSelectionColors(
            handleColor = Color.Transparent,
            backgroundColor = Color.Transparent
        )

        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        color = defaultColor,
                        shape = shape
                    )
                    .border(1.dp, if (errorTextId != null) errorColor else defaultColor, shape),
                value = value,
                readOnly = isReadOnly,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.None
                ),
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(Color.Unspecified),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        val placeholder = stringResource(id = placeholderId)
                        if (placeholder.isNotEmpty() && value.isEmpty()) {
                            Text(
                                modifier = Modifier.infiniteMarquee,
                                text = placeholder,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }

                }
            )
        }

        if (errorTextId != null) {
            Text(
                modifier = Modifier.infiniteMarquee,
                text = stringResource(id = errorTextId),
                textAlign = TextAlign.Start,
                color = errorColor
            )
        }
    }
}