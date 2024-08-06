package com.feduss.timerwear.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PrefsUtils {

    companion object {
        private fun getSharedPreferences(context: Context ): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }

        fun getStringPref(context: Context, pref: String): String? {
            return getSharedPreferences(context).getString(pref, null)
        }

        fun getStringSetPref(context: Context, pref: String): MutableSet<String>? {
            return getSharedPreferences(context).getStringSet(pref, null)
        }

        fun setStringPref(context: Context, pref: String, newValue: String?) {

            if (newValue == null) {
                getSharedPreferences(context).edit().remove(pref).apply()
            } else {
                getSharedPreferences(context).edit().putString(pref, newValue).apply()
            }
        }

        fun setStringSetPref(context: Context, pref: String, newValue: Set<String>?) {

            if (newValue == null) {
                getSharedPreferences(context).edit().remove(pref).apply()
            } else {
                getSharedPreferences(context).edit().putStringSet(pref, newValue).apply()
            }
        }
    }
}