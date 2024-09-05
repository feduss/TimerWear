package com.feduss.timerwear.utils

import android.content.Context
import android.provider.Settings

class AmbientUtils {

    companion object {
        fun isAmbientDisplayOn(context: Context): Boolean {
            return try {
                Settings.Global.getInt(
                    context.contentResolver,
                    "ambient_enabled"
                ) == 1
            } catch (e: Exception) {
                false
            }
        }
    }
}