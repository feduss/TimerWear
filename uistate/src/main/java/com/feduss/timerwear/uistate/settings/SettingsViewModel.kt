package com.feduss.timerwear.uistate.settings

import androidx.lifecycle.ViewModel
import com.feduss.timerwear.uistate.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(): ViewModel() {

    val feedbackEmail = "feduss96@gmail.com"

    // Copies
    val headerTextId = R.string.settings_title
    val feedbackTextId = R.string.settings_feedback_title
    val appVersionTextId = R.string.settings_app_version
}