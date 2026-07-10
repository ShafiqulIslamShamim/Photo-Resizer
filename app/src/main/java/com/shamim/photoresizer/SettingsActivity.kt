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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.shamim.photoresizer.ui.SettingsApp
import com.shamim.photoresizer.ui.theme.ApplicationTheme

class SettingsActivity : ComponentActivity() {
    /**
     * Called when the activity is starting.
     * Sets up the edge-to-edge layout and renders the Jetpack Compose-based Settings UI
     * under the application theme with custom settings state mappings.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied in onSaveInstanceState.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager.getInstance(context) }
            val themeMode by settingsManager.themeMode.collectAsState()
            val isAmoled by settingsManager.isAmoled.collectAsState()
            val selectedPalette by settingsManager.selectedPalette.collectAsState()

            ApplicationTheme(
                darkModeSetting = themeMode,
                isAmoledSetting = isAmoled,
                selectedPaletteSetting = selectedPalette,
            ) {
                SettingsApp(
                    settingsManager = settingsManager,
                    onBackClick = { finish() },
                )
            }
        }
    }
}
