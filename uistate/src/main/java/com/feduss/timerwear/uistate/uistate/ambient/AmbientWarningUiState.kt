package com.feduss.timerwear.uistate.uistate.ambient

data class AmbientWarningUiState(
    val titleId: Int,
    val subTitleId: Int,
    val positiveIconId: Int,
    val positiveIconDescription: String,
    val negativeIconId: Int,
    val negativeIconDescription: String,
)
