package com.feduss.timerwear.uistate.uistate

import androidx.compose.ui.text.input.KeyboardType

data class GenericTextInputUiState(
    val value: String,
    val titleId: Int,
    val placeholderId: Int,
    val keyboardType: KeyboardType,
    val errorTextId: Int? = null
)