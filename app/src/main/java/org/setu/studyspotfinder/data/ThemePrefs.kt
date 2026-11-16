package org.setu.studyspotfinder.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

// remembers if the user wants dark mode. stored in shared prefs so it survives a restart.
object ThemePrefs {

    private const val PREFS = "study_spot_theme"
    private const val KEY_MODE = "night_mode"

    fun apply(context: Context) {
        val mode = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun isDark(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) ==
                AppCompatDelegate.MODE_NIGHT_YES

    fun setDark(context: Context, dark: Boolean) {
        val mode = if (dark) AppCompatDelegate.MODE_NIGHT_YES
        else AppCompatDelegate.MODE_NIGHT_NO

        // save first, then switch, because the switch recreates the activity
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_MODE, mode)
            .apply()

        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
