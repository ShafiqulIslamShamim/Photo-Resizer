/*
* Copyright (c) 2026 Shafiqul Islam Shamim
* GitHub: https://github.com/ShafiqulIslamShamim/Photo-Resizer
*
* All Rights Reserved.
*
* This source code is made publicly available solely for viewing, collaboration,
* educational reference, and submitting pull requests to the official repository.
*
* No permission is granted to copy, modify, redistribute, sublicense, or use
* this source code, in whole or in part, for personal, commercial, or any other
* purpose without the prior written permission of the copyright holder.
*/
package com.shamim.photoresizer

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager private constructor(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings_v1", Context.MODE_PRIVATE)

    private val _themeMode =
        MutableStateFlow(
            prefs.getString("theme_mode", "system") ?: "system",
        ) // Default to On/Dark Theme for aesthetic consistency
    val themeMode: StateFlow<String> = _themeMode

    private val _isAmoled = MutableStateFlow(prefs.getBoolean("is_amoled", false))
    val isAmoled: StateFlow<Boolean> = _isAmoled

    private val _isSeasonalEnabled = MutableStateFlow(prefs.getBoolean("is_seasonal_enabled", false))
    val isSeasonalEnabled: StateFlow<Boolean> = _isSeasonalEnabled

    private val _selectedSeason =
        MutableStateFlow(prefs.getString("selected_season", "auto") ?: "auto")
    val selectedSeason: StateFlow<String> = _selectedSeason

    private val _selectedPalette =
        MutableStateFlow(prefs.getString("selected_palette", "dynamic") ?: "dynamic")
    val selectedPalette: StateFlow<String> = _selectedPalette

    /**
     * Updates the application theme mode (e.g., "system", "light", "dark") in preferences and the state flow.
     *
     * @param mode The selected theme mode string.
     */
    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    /**
     * Updates the AMOLED dark mode state in preferences and the state flow.
     *
     * @param enabled True to enable AMOLED pure black dark mode, false for standard dark grey.
     */
    fun setIsAmoled(enabled: Boolean) {
        prefs.edit().putBoolean("is_amoled", enabled).apply()
        _isAmoled.value = enabled
    }

    /**
     * Updates the seasonal overlay effects state in preferences and the state flow.
     *
     * @param enabled True to enable live seasonal animations overlay, false to disable.
     */
    fun setIsSeasonalEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_seasonal_enabled", enabled).commit()
        _isSeasonalEnabled.value = enabled
    }

    /**
     * Updates the current selected season (e.g., "auto", "autumn", "winter", "rainy", "spring") in preferences and state flow.
     *
     * @param season The selected season key or "auto" to detect based on system calendar date.
     */
    fun setSelectedSeason(season: String) {
        prefs.edit().putString("selected_season", season).commit()
        _selectedSeason.value = season
    }

    /**
     * Updates the selected material color palette scheme in preferences and state flow.
     *
     * @param palette The color palette scheme key (e.g., "dynamic", "pastel", etc.).
     */
    fun setSelectedPalette(palette: String) {
        prefs.edit().putString("selected_palette", palette).apply()
        _selectedPalette.value = palette
    }

    companion object {
        @Volatile private var INSTANCE: SettingsManager? = null

        /**
         * Returns the thread-safe singleton instance of SettingsManager.
         *
         * @param context Context used to retrieve the shared preferences file.
         * @return The single SettingsManager instance.
         */
        fun getInstance(context: Context): SettingsManager =
            INSTANCE
                ?: synchronized(this) {
                    val instance = SettingsManager(context.applicationContext)
                    INSTANCE = instance
                    instance
                }
    }
}
