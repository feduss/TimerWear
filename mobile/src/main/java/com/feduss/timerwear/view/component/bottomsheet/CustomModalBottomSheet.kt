package com.feduss.timerwear.view.component.bottomsheet

import android.view.Gravity
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.feduss.buswear.view.component.text.HyperlinkText

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CustomModalBottomSheet(
    sheetState: SheetState,
    title: String,
    footerText: String,
    content: @Composable (ColumnScope.() -> Unit),
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color.LightGray
            )
        },
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontSize = TextUnit(16f, TextUnitType.Sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            content()

            Text(
                text = footerText,
                textAlign = TextAlign.Center,
                fontSize = TextUnit(16f, TextUnitType.Sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}