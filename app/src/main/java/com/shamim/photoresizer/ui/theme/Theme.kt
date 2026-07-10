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
package com.shamim.photoresizer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.shamim.photoresizer.SettingsManager
import com.shamim.photoresizer.ui.seasonal.ComposeRipple
import com.shamim.photoresizer.ui.seasonal.ComposeSeasonalEffects
import com.shamim.photoresizer.ui.seasonal.ComposeSeasonalEffectsOverlay
import com.shamim.photoresizer.ui.seasonal.ComposeSplash

val LocalSeasonalEffectController =
    staticCompositionLocalOf<MutableState<Boolean>> {
        mutableStateOf(true)
    }

// Coral palette
private val CoralDarkColors =
    darkColorScheme(
        primary = primaryCoralDark,
        onPrimary = onPrimaryCoralDark,
        primaryContainer = primaryContainerCoralDark,
        onPrimaryContainer = onPrimaryContainerCoralDark,
        secondary = secondaryCoralDark,
        onSecondary = onSecondaryCoralDark,
        secondaryContainer = secondaryContainerCoralDark,
        onSecondaryContainer = onSecondaryContainerCoralDark,
        tertiary = tertiaryCoralDark,
        onTertiary = onTertiaryCoralDark,
        tertiaryContainer = tertiaryContainerCoralDark,
        onTertiaryContainer = onTertiaryContainerCoralDark,
        error = errorCoralDark,
        onError = onErrorCoralDark,
        errorContainer = errorContainerCoralDark,
        onErrorContainer = onErrorContainerCoralDark,
        background = backgroundCoralDark,
        onBackground = onBackgroundCoralDark,
        surface = surfaceCoralDark,
        onSurface = onSurfaceCoralDark,
        surfaceVariant = surfaceVariantCoralDark,
        onSurfaceVariant = onSurfaceVariantCoralDark,
        outline = outlineCoralDark,
        outlineVariant = outlineVariantCoralDark,
        scrim = scrimCoralDark,
        inverseSurface = inverseSurfaceCoralDark,
        inverseOnSurface = inverseOnSurfaceCoralDark,
        inversePrimary = inversePrimaryCoralDark,
        surfaceDim = surfaceDimCoralDark,
        surfaceBright = surfaceBrightCoralDark,
        surfaceContainerLowest = surfaceContainerLowestCoralDark,
        surfaceContainerLow = surfaceContainerLowCoralDark,
        surfaceContainer = surfaceContainerCoralDark,
        surfaceContainerHigh = surfaceContainerHighCoralDark,
        surfaceContainerHighest = surfaceContainerHighestCoralDark,
    )
private val CoralLightColors =
    lightColorScheme(
        primary = primaryCoralLight,
        onPrimary = onPrimaryCoralLight,
        primaryContainer = primaryContainerCoralLight,
        onPrimaryContainer = onPrimaryContainerCoralLight,
        secondary = secondaryCoralLight,
        onSecondary = onSecondaryCoralLight,
        secondaryContainer = secondaryContainerCoralLight,
        onSecondaryContainer = onSecondaryContainerCoralLight,
        tertiary = tertiaryCoralLight,
        onTertiary = onTertiaryCoralLight,
        tertiaryContainer = tertiaryContainerCoralLight,
        onTertiaryContainer = onTertiaryContainerCoralLight,
        error = errorCoralLight,
        onError = onErrorCoralLight,
        errorContainer = errorContainerCoralLight,
        onErrorContainer = onErrorContainerCoralLight,
        background = backgroundCoralLight,
        onBackground = onBackgroundCoralLight,
        surface = surfaceCoralLight,
        onSurface = onSurfaceCoralLight,
        surfaceVariant = surfaceVariantCoralLight,
        onSurfaceVariant = onSurfaceVariantCoralLight,
        outline = outlineCoralLight,
        outlineVariant = outlineVariantCoralLight,
        scrim = scrimCoralLight,
        inverseSurface = inverseSurfaceCoralLight,
        inverseOnSurface = inverseOnSurfaceCoralLight,
        inversePrimary = inversePrimaryCoralLight,
        surfaceDim = surfaceDimCoralLight,
        surfaceBright = surfaceBrightCoralLight,
        surfaceContainerLowest = surfaceContainerLowestCoralLight,
        surfaceContainerLow = surfaceContainerLowCoralLight,
        surfaceContainer = surfaceContainerCoralLight,
        surfaceContainerHigh = surfaceContainerHighCoralLight,
        surfaceContainerHighest = surfaceContainerHighestCoralLight,
    )

// Gold/Brown palette
private val GoldDarkColors =
    darkColorScheme(
        primary = primaryAmberDark,
        onPrimary = onPrimaryAmberDark,
        primaryContainer = primaryContainerAmberDark,
        onPrimaryContainer = onPrimaryContainerAmberDark,
        secondary = secondaryAmberDark,
        onSecondary = onSecondaryAmberDark,
        secondaryContainer = secondaryContainerAmberDark,
        onSecondaryContainer = onSecondaryContainerAmberDark,
        tertiary = tertiaryAmberDark,
        onTertiary = onTertiaryAmberDark,
        tertiaryContainer = tertiaryContainerAmberDark,
        onTertiaryContainer = onTertiaryContainerAmberDark,
        error = errorAmberDark,
        onError = onErrorAmberDark,
        errorContainer = errorContainerAmberDark,
        onErrorContainer = onErrorContainerAmberDark,
        background = backgroundAmberDark,
        onBackground = onBackgroundAmberDark,
        surface = surfaceAmberDark,
        onSurface = onSurfaceAmberDark,
        surfaceVariant = surfaceVariantAmberDark,
        onSurfaceVariant = onSurfaceVariantAmberDark,
        outline = outlineAmberDark,
        outlineVariant = outlineVariantAmberDark,
        scrim = scrimAmberDark,
        inverseSurface = inverseSurfaceAmberDark,
        inverseOnSurface = inverseOnSurfaceAmberDark,
        inversePrimary = inversePrimaryAmberDark,
        surfaceDim = surfaceDimAmberDark,
        surfaceBright = surfaceBrightAmberDark,
        surfaceContainerLowest = surfaceContainerLowestAmberDark,
        surfaceContainerLow = surfaceContainerLowAmberDark,
        surfaceContainer = surfaceContainerAmberDark,
        surfaceContainerHigh = surfaceContainerHighAmberDark,
        surfaceContainerHighest = surfaceContainerHighestAmberDark,
    )
private val GoldLightColors =
    lightColorScheme(
        primary = primaryAmberLight,
        onPrimary = onPrimaryAmberLight,
        primaryContainer = primaryContainerAmberLight,
        onPrimaryContainer = onPrimaryContainerAmberLight,
        secondary = secondaryAmberLight,
        onSecondary = onSecondaryAmberLight,
        secondaryContainer = secondaryContainerAmberLight,
        onSecondaryContainer = onSecondaryContainerAmberLight,
        tertiary = tertiaryAmberLight,
        onTertiary = onTertiaryAmberLight,
        tertiaryContainer = tertiaryContainerAmberLight,
        onTertiaryContainer = onTertiaryContainerAmberLight,
        error = errorAmberLight,
        onError = onErrorAmberLight,
        errorContainer = errorContainerAmberLight,
        onErrorContainer = onErrorContainerAmberLight,
        background = backgroundAmberLight,
        onBackground = onBackgroundAmberLight,
        surface = surfaceAmberLight,
        onSurface = onSurfaceAmberLight,
        surfaceVariant = surfaceVariantAmberLight,
        onSurfaceVariant = onSurfaceVariantAmberLight,
        outline = outlineAmberLight,
        outlineVariant = outlineVariantAmberLight,
        scrim = scrimAmberLight,
        inverseSurface = inverseSurfaceAmberLight,
        inverseOnSurface = inverseOnSurfaceAmberLight,
        inversePrimary = inversePrimaryAmberLight,
        surfaceDim = surfaceDimAmberLight,
        surfaceBright = surfaceBrightAmberLight,
        surfaceContainerLowest = surfaceContainerLowestAmberLight,
        surfaceContainerLow = surfaceContainerLowAmberLight,
        surfaceContainer = surfaceContainerAmberLight,
        surfaceContainerHigh = surfaceContainerHighAmberLight,
        surfaceContainerHighest = surfaceContainerHighestAmberLight,
    )

// Pink/Purple palette
private val PinkDarkColors =
    darkColorScheme(
        primary = primaryBlossomDark,
        onPrimary = onPrimaryBlossomDark,
        primaryContainer = primaryContainerBlossomDark,
        onPrimaryContainer = onPrimaryContainerBlossomDark,
        secondary = secondaryBlossomDark,
        onSecondary = onSecondaryBlossomDark,
        secondaryContainer = secondaryContainerBlossomDark,
        onSecondaryContainer = onSecondaryContainerBlossomDark,
        tertiary = tertiaryBlossomDark,
        onTertiary = onTertiaryBlossomDark,
        tertiaryContainer = tertiaryContainerBlossomDark,
        onTertiaryContainer = onTertiaryContainerBlossomDark,
        error = errorBlossomDark,
        onError = onErrorBlossomDark,
        errorContainer = errorContainerBlossomDark,
        onErrorContainer = onErrorContainerBlossomDark,
        background = backgroundBlossomDark,
        onBackground = onBackgroundBlossomDark,
        surface = surfaceBlossomDark,
        onSurface = onSurfaceBlossomDark,
        surfaceVariant = surfaceVariantBlossomDark,
        onSurfaceVariant = onSurfaceVariantBlossomDark,
        outline = outlineBlossomDark,
        outlineVariant = outlineVariantBlossomDark,
        scrim = scrimBlossomDark,
        inverseSurface = inverseSurfaceBlossomDark,
        inverseOnSurface = inverseOnSurfaceBlossomDark,
        inversePrimary = inversePrimaryBlossomDark,
        surfaceDim = surfaceDimBlossomDark,
        surfaceBright = surfaceBrightBlossomDark,
        surfaceContainerLowest = surfaceContainerLowestBlossomDark,
        surfaceContainerLow = surfaceContainerLowBlossomDark,
        surfaceContainer = surfaceContainerBlossomDark,
        surfaceContainerHigh = surfaceContainerHighBlossomDark,
        surfaceContainerHighest = surfaceContainerHighestBlossomDark,
    )
private val PinkLightColors =
    lightColorScheme(
        primary = primaryBlossomLight,
        onPrimary = onPrimaryBlossomLight,
        primaryContainer = primaryContainerBlossomLight,
        onPrimaryContainer = onPrimaryContainerBlossomLight,
        secondary = secondaryBlossomLight,
        onSecondary = onSecondaryBlossomLight,
        secondaryContainer = secondaryContainerBlossomLight,
        onSecondaryContainer = onSecondaryContainerBlossomLight,
        tertiary = tertiaryBlossomLight,
        onTertiary = onTertiaryBlossomLight,
        tertiaryContainer = tertiaryContainerBlossomLight,
        onTertiaryContainer = onTertiaryContainerBlossomLight,
        error = errorBlossomLight,
        onError = onErrorBlossomLight,
        errorContainer = errorContainerBlossomLight,
        onErrorContainer = onErrorContainerBlossomLight,
        background = backgroundBlossomLight,
        onBackground = onBackgroundBlossomLight,
        surface = surfaceBlossomLight,
        onSurface = onSurfaceBlossomLight,
        surfaceVariant = surfaceVariantBlossomLight,
        onSurfaceVariant = onSurfaceVariantBlossomLight,
        outline = outlineBlossomLight,
        outlineVariant = outlineVariantBlossomLight,
        scrim = scrimBlossomLight,
        inverseSurface = inverseSurfaceBlossomLight,
        inverseOnSurface = inverseOnSurfaceBlossomLight,
        inversePrimary = inversePrimaryBlossomLight,
        surfaceDim = surfaceDimBlossomLight,
        surfaceBright = surfaceBrightBlossomLight,
        surfaceContainerLowest = surfaceContainerLowestBlossomLight,
        surfaceContainerLow = surfaceContainerLowBlossomLight,
        surfaceContainer = surfaceContainerBlossomLight,
        surfaceContainerHigh = surfaceContainerHighBlossomLight,
        surfaceContainerHighest = surfaceContainerHighestBlossomLight,
    )

// Blue/Lavender palette
private val BlueDarkColors =
    darkColorScheme(
        primary = primaryOceanDark,
        onPrimary = onPrimaryOceanDark,
        primaryContainer = primaryContainerOceanDark,
        onPrimaryContainer = onPrimaryContainerOceanDark,
        secondary = secondaryOceanDark,
        onSecondary = onSecondaryOceanDark,
        secondaryContainer = secondaryContainerOceanDark,
        onSecondaryContainer = onSecondaryContainerOceanDark,
        tertiary = tertiaryOceanDark,
        onTertiary = onTertiaryOceanDark,
        tertiaryContainer = tertiaryContainerOceanDark,
        onTertiaryContainer = onTertiaryContainerOceanDark,
        error = errorOceanDark,
        onError = onErrorOceanDark,
        errorContainer = errorContainerOceanDark,
        onErrorContainer = onErrorContainerOceanDark,
        background = backgroundOceanDark,
        onBackground = onBackgroundOceanDark,
        surface = surfaceOceanDark,
        onSurface = onSurfaceOceanDark,
        surfaceVariant = surfaceVariantOceanDark,
        onSurfaceVariant = onSurfaceVariantOceanDark,
        outline = outlineOceanDark,
        outlineVariant = outlineVariantOceanDark,
        scrim = scrimOceanDark,
        inverseSurface = inverseSurfaceOceanDark,
        inverseOnSurface = inverseOnSurfaceOceanDark,
        inversePrimary = inversePrimaryOceanDark,
        surfaceDim = surfaceDimOceanDark,
        surfaceBright = surfaceBrightOceanDark,
        surfaceContainerLowest = surfaceContainerLowestOceanDark,
        surfaceContainerLow = surfaceContainerLowOceanDark,
        surfaceContainer = surfaceContainerOceanDark,
        surfaceContainerHigh = surfaceContainerHighOceanDark,
        surfaceContainerHighest = surfaceContainerHighestOceanDark,
    )
private val BlueLightColors =
    lightColorScheme(
        primary = primaryOceanLight,
        onPrimary = onPrimaryOceanLight,
        primaryContainer = primaryContainerOceanLight,
        onPrimaryContainer = onPrimaryContainerOceanLight,
        secondary = secondaryOceanLight,
        onSecondary = onSecondaryOceanLight,
        secondaryContainer = secondaryContainerOceanLight,
        onSecondaryContainer = onSecondaryContainerOceanLight,
        tertiary = tertiaryOceanLight,
        onTertiary = onTertiaryOceanLight,
        tertiaryContainer = tertiaryContainerOceanLight,
        onTertiaryContainer = onTertiaryContainerOceanLight,
        error = errorOceanLight,
        onError = onErrorOceanLight,
        errorContainer = errorContainerOceanLight,
        onErrorContainer = onErrorContainerOceanLight,
        background = backgroundOceanLight,
        onBackground = onBackgroundOceanLight,
        surface = surfaceOceanLight,
        onSurface = onSurfaceOceanLight,
        surfaceVariant = surfaceVariantOceanLight,
        onSurfaceVariant = onSurfaceVariantOceanLight,
        outline = outlineOceanLight,
        outlineVariant = outlineVariantOceanLight,
        scrim = scrimOceanLight,
        inverseSurface = inverseSurfaceOceanLight,
        inverseOnSurface = inverseOnSurfaceOceanLight,
        inversePrimary = inversePrimaryOceanLight,
        surfaceDim = surfaceDimOceanLight,
        surfaceBright = surfaceBrightOceanLight,
        surfaceContainerLowest = surfaceContainerLowestOceanLight,
        surfaceContainerLow = surfaceContainerLowOceanLight,
        surfaceContainer = surfaceContainerOceanLight,
        surfaceContainerHigh = surfaceContainerHighOceanLight,
        surfaceContainerHighest = surfaceContainerHighestOceanLight,
    )

// Green/Teal palette
private val GreenDarkColors =
    darkColorScheme(
        primary = primaryEmeraldDark,
        onPrimary = onPrimaryEmeraldDark,
        primaryContainer = primaryContainerEmeraldDark,
        onPrimaryContainer = onPrimaryContainerEmeraldDark,
        secondary = secondaryEmeraldDark,
        onSecondary = onSecondaryEmeraldDark,
        secondaryContainer = secondaryContainerEmeraldDark,
        onSecondaryContainer = onSecondaryContainerEmeraldDark,
        tertiary = tertiaryEmeraldDark,
        onTertiary = onTertiaryEmeraldDark,
        tertiaryContainer = tertiaryContainerEmeraldDark,
        onTertiaryContainer = onTertiaryContainerEmeraldDark,
        error = errorEmeraldDark,
        onError = onErrorEmeraldDark,
        errorContainer = errorContainerEmeraldDark,
        onErrorContainer = onErrorContainerEmeraldDark,
        background = backgroundEmeraldDark,
        onBackground = onBackgroundEmeraldDark,
        surface = surfaceEmeraldDark,
        onSurface = onSurfaceEmeraldDark,
        surfaceVariant = surfaceVariantEmeraldDark,
        onSurfaceVariant = onSurfaceVariantEmeraldDark,
        outline = outlineEmeraldDark,
        outlineVariant = outlineVariantEmeraldDark,
        scrim = scrimEmeraldDark,
        inverseSurface = inverseSurfaceEmeraldDark,
        inverseOnSurface = inverseOnSurfaceEmeraldDark,
        inversePrimary = inversePrimaryEmeraldDark,
        surfaceDim = surfaceDimEmeraldDark,
        surfaceBright = surfaceBrightEmeraldDark,
        surfaceContainerLowest = surfaceContainerLowestEmeraldDark,
        surfaceContainerLow = surfaceContainerLowEmeraldDark,
        surfaceContainer = surfaceContainerEmeraldDark,
        surfaceContainerHigh = surfaceContainerHighEmeraldDark,
        surfaceContainerHighest = surfaceContainerHighestEmeraldDark,
    )
private val GreenLightColors =
    lightColorScheme(
        primary = primaryEmeraldLight,
        onPrimary = onPrimaryEmeraldLight,
        primaryContainer = primaryContainerEmeraldLight,
        onPrimaryContainer = onPrimaryContainerEmeraldLight,
        secondary = secondaryEmeraldLight,
        onSecondary = onSecondaryEmeraldLight,
        secondaryContainer = secondaryContainerEmeraldLight,
        onSecondaryContainer = onSecondaryContainerEmeraldLight,
        tertiary = tertiaryEmeraldLight,
        onTertiary = onTertiaryEmeraldLight,
        tertiaryContainer = tertiaryContainerEmeraldLight,
        onTertiaryContainer = onTertiaryContainerEmeraldLight,
        error = errorEmeraldLight,
        onError = onErrorEmeraldLight,
        errorContainer = errorContainerEmeraldLight,
        onErrorContainer = onErrorContainerEmeraldLight,
        background = backgroundEmeraldLight,
        onBackground = onBackgroundEmeraldLight,
        surface = surfaceEmeraldLight,
        onSurface = onSurfaceEmeraldLight,
        surfaceVariant = surfaceVariantEmeraldLight,
        onSurfaceVariant = onSurfaceVariantEmeraldLight,
        outline = outlineEmeraldLight,
        outlineVariant = outlineVariantEmeraldLight,
        scrim = scrimEmeraldLight,
        inverseSurface = inverseSurfaceEmeraldLight,
        inverseOnSurface = inverseOnSurfaceEmeraldLight,
        inversePrimary = inversePrimaryEmeraldLight,
        surfaceDim = surfaceDimEmeraldLight,
        surfaceBright = surfaceBrightEmeraldLight,
        surfaceContainerLowest = surfaceContainerLowestEmeraldLight,
        surfaceContainerLow = surfaceContainerLowEmeraldLight,
        surfaceContainer = surfaceContainerEmeraldLight,
        surfaceContainerHigh = surfaceContainerHighEmeraldLight,
        surfaceContainerHighest = surfaceContainerHighestEmeraldLight,
    )

private val FallbackDarkColorScheme =
    darkColorScheme(
        primary = Color(0xFFD0BCFF),
        secondary = Color(0xFFCCC2DC),
        tertiary = Color(0xFFEFB8C8),
    )

private val FallbackLightColorScheme =
    lightColorScheme(
        primary = Color(0xFF6650a4),
        secondary = Color(0xFF625b71),
        tertiary = Color(0xFF7D5260),
    )

/**
 * Renders the custom Material Design 3 theme for the application.
 * Dynamically switches color palettes (such as Emerald, Blossom, Ocean, Amber, Coral, or System Dynamic)
 * and applies pitch-black dark colors for energy-saving AMOLED screens.
 *
 * @param darkModeSetting Selected mode for dark mode ("on", "off", "system").
 * @param isAmoledSetting True to force amoled-compatible pure black background color schemes in dark mode.
 * @param selectedPaletteSetting Selected color palette name ("dynamic", "green", "pink", "blue", "gold", "coral").
 * @param content Nested layout blocks inside this theme wrapper.
 */
@Composable
fun ApplicationTheme(
    darkModeSetting: String = "system",
    isAmoledSetting: Boolean = false,
    selectedPaletteSetting: String = "dynamic",
    content: @Composable () -> Unit,
) {
    val darkTheme =
        when (darkModeSetting) {
            "on" -> true
            "off" -> false
            else -> isSystemInDarkTheme()
        }

    val context = LocalContext.current
    var baseScheme: ColorScheme =
        if (selectedPaletteSetting == "dynamic" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) {
                when (selectedPaletteSetting) {
                    "coral" -> CoralDarkColors
                    "gold" -> GoldDarkColors
                    "pink" -> PinkDarkColors
                    "blue" -> BlueDarkColors
                    "green" -> GreenDarkColors
                    else -> CoralDarkColors
                }
            } else {
                when (selectedPaletteSetting) {
                    "coral" -> CoralLightColors
                    "gold" -> GoldLightColors
                    "pink" -> PinkLightColors
                    "blue" -> BlueLightColors
                    "green" -> GreenLightColors
                    else -> CoralLightColors
                }
            }
        }

    // Apply AMOLED absolute black background if enabled and darkTheme is true
    if (darkTheme && isAmoledSetting) {
        baseScheme =
            baseScheme.copy(
                background = Color.Black,
                surface = Color(0xFF110B0B),
                surfaceVariant = Color(0xFF1F1514),
            )
    }

    val seasonalController = remember { mutableStateOf(true) }

    CompositionLocalProvider(LocalSeasonalEffectController provides seasonalController) {
        MaterialTheme(colorScheme = baseScheme, typography = Typography) {
            val settings = remember { SettingsManager.getInstance(context) }
            val isSeasonalEnabledSetting by settings.isSeasonalEnabled.collectAsState()
            val isSeasonalEnabled = isSeasonalEnabledSetting && seasonalController.value
            val selectedSeason by settings.selectedSeason.collectAsState()

            val ripples = remember { mutableStateListOf<ComposeRipple>() }
            val splashes = remember { mutableStateListOf<ComposeSplash>() }

            val primaryColor = baseScheme.primary
            val secondaryColor = baseScheme.secondary
            val tertiaryColor = baseScheme.tertiary

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .then(
                            if (isSeasonalEnabled) {
                                Modifier.pointerInput(selectedSeason) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Final)
                                            event.changes.forEach { change ->
                                                if (change.pressed && !change.previousPressed) {
                                                    val season =
                                                        ComposeSeasonalEffects.getActiveSeason(
                                                            selectedSeason,
                                                            context,
                                                        )
                                                    ComposeSeasonalEffects.addTouchFeedback(
                                                        ripples,
                                                        splashes,
                                                        season,
                                                        change.position.x,
                                                        change.position.y,
                                                        primaryColor,
                                                        secondaryColor,
                                                        tertiaryColor,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Modifier
                            },
                        ),
            ) {
                content()
                if (isSeasonalEnabled) {
                    ComposeSeasonalEffectsOverlay(
                        selectedSeason = selectedSeason,
                        ripples = ripples,
                        splashes = splashes,
                    )
                }
            }
        }
    }
}
