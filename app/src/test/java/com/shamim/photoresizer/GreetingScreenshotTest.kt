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

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.shamim.photoresizer.ui.SettingsApp
import com.shamim.photoresizer.ui.theme.ApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    qualifiers = RobolectricDeviceQualifiers.Pixel8,
    sdk = [34],
)
class SettingsScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun settings_screenshot() {
        composeTestRule.setContent {
            val context = LocalContext.current

            val settingsManager =
                remember {
                    SettingsManager.getInstance(context)
                }

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
                    onBackClick = {},
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/settings.png")
    }
}
