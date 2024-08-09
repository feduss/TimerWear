package com.feduss.timerwear.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.feduss.timerwear.entity.enums.TimerType

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

        fun cancelTimerInPrefs(context: Context) {
            setStringPref(context, PrefParam.CurrentTimerIndex.value, null)
            setStringPref(context, PrefParam.CurrentRepetition.value, null)
            setStringPref(context, PrefParam.CurrentTimerSecondsRemaining.value, null)
        }

        fun setNextTimerInPrefs(context: Context, newCurrentTimerIndex: Int, newCurrentRepetition: Int) {

            setStringPref(
                context,
                PrefParam.CurrentTimerIndex.value,
                newCurrentTimerIndex.toString()
            )

            setStringPref(
                context,
                PrefParam.CurrentRepetition.value,
                newCurrentRepetition.toString()
            )

            setStringPref(context, PrefParam.CurrentTimerSecondsRemaining.value, null)
        }

        fun isTimerActive(context: Context) = getStringPref(
            context,
            PrefParam.IsTimerActive.value
        ) == "true"

        fun getTimerType(context: Context) = TimerType.fromString(
            getStringPref(
                context,
                PrefParam.TimerType.value
            )
        )
    }
}