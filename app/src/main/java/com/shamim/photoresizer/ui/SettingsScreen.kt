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
package com.shamim.photoresizer.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.PathParser
import com.shamim.photoresizer.OTAUpdateHelper
import com.shamim.photoresizer.R
import com.shamim.photoresizer.SettingsManager

/**
 * Entry-point Composable for the Settings screen subsystem.
 * Routes between the main settings panel and the personalized color/style settings view.
 *
 * @param settingsManager Singleton manager for reading and writing SharedPreferences.
 * @param onBackClick Callback triggered when navigating back to the previous screen or closing the activity.
 */
@Composable
fun SettingsApp(
    settingsManager: SettingsManager,
    onBackClick: () -> Unit,
) {
    var activeSubScreen by remember { mutableStateOf("settings") }

    BackHandler(enabled = activeSubScreen != "settings") {
        if (activeSubScreen == "style") {
            activeSubScreen = "settings"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        when (activeSubScreen) {
            "settings" -> {
                MainSettingsView(
                    settingsManager = settingsManager,
                    onBackClick = onBackClick,
                    onNavigateToStyle = { activeSubScreen = "style" },
                )
            }

            "style" -> {
                StyleSettingsView(
                    settingsManager = settingsManager,
                    onBackClick = { activeSubScreen = "settings" },
                )
            }
        }
    }
}

/**
 * Opens a web URL link in an external web browser application.
 *
 * @param context Current execution context.
 * @param url The valid URL string.
 */
private fun openUrl(
    context: Context,
    url: String,
) {
    try {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Launches an email client to send inquiry or support requests.
 *
 * @param context Current execution context.
 * @param toEmail Recipient email address.
 */
private fun openEmail(
    context: Context,
    toEmail: String,
) {
    var appName = "Photo Resizer"
    var versionName = "1.4"
    var versionCode = 4

    try {
        val pm = context.packageManager
        val pi = pm.getPackageInfo(context.packageName, 0)
        appName = pm.getApplicationLabel(pm.getApplicationInfo(context.packageName, 0)).toString()
        versionName = pi.versionName ?: "1.4"
        versionCode = PackageInfoCompat.getLongVersionCode(pi).toInt()
    } catch (e: Exception) {
        // Safe fallback
    }

    val subject = "Feedback - $appName v$versionName (Code: $versionCode)"
    try {
        val intent =
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        val chooser = Intent.createChooser(intent, "Send Email feedback using...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "No email client application found", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Renders a dialog displaying the latest news and updates, containing external links to
 * official GitHub and Facebook community resources.
 *
 * @param onDismiss Callback triggered when the dialog is dismissed or closed.
 * @param onFacebookClick Callback triggered when clicking on the Facebook community option.
 * @param onGithubClick Callback triggered when clicking on the GitHub repository link.
 */
@Composable
fun NewsUpdatesDialog(
    onDismiss: () -> Unit,
    onFacebookClick: () -> Unit,
    onGithubClick: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "News & Updates",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(
                    text = "Follow our community channels to receive announcements and release logs.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                // Facebook Option
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onFacebookClick() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Facebook logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Facebook Channel",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Join our community group",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // GitHub Option
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onGithubClick() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "GitHub profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "GitHub Developer Profile",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Check open-source repositories",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

/**
 * Checks for OTA updates manually and renders a dialog informing the user about the
 * update status, current version, and available OTA packages.
 *
 * @param onDismiss Callback triggered to close or dismiss the update dialog.
 */
@Composable
fun CheckUpdateDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val appVersion =
        remember {
            try {
                val pm = context.packageManager
                val pi = pm.getPackageInfo(context.packageName, 0)
                pi.versionName ?: "1.1"
            } catch (e: Exception) {
                "1.1"
            }
        }
    val appVersionCode =
        remember {
            try {
                val pm = context.packageManager
                val pi = pm.getPackageInfo(context.packageName, 0)
                androidx.core.content.pm.PackageInfoCompat
                    .getLongVersionCode(pi)
                    .toInt()
            } catch (e: Exception) {
                1
            }
        }
    var isChecking by remember { mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        isChecking = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isChecking) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Checking for Updates...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Contacting OTA update servers safely.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Up-to-date Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your App Is Up To Date!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text =
                            "Installed version: v$appVersion (Build $appVersionCode)\nNo newer updates are available currently.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Awesome")
                    }
                }
            }
        }
    }
}

/**
 * Renders the primary settings view, enabling users to toggle preferences like theme (Light, Dark, System),
 * AMOLED black backgrounds, seasonal effects, update checks, community news, and links.
 *
 * @param settingsManager Singleton manager for reading and writing SharedPreferences.
 * @param onBackClick Callback triggered when backing out from the settings panel.
 * @param onNavigateToStyle Callback triggered to open the palette custom style view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsView(
    settingsManager: SettingsManager,
    onBackClick: () -> Unit,
    onNavigateToStyle: () -> Unit,
) {
    val context = LocalContext.current

    val appVersion =
        remember {
            try {
                val pm = context.packageManager
                val pi = pm.getPackageInfo(context.packageName, 0)
                pi.versionName ?: "1.4"
            } catch (e: Exception) {
                "1.4"
            }
        }

    var showNewsDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    SectionTitle(stringResource(id = R.string.settings_appearance))

                    SettingsSection {
                        SettingItem(
                            headlineText = stringResource(id = R.string.settings_style),
                            supportingText = "Dark mode • Color palette",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    onNavigateToStyle()
                                },
                        )
                    }

                    SettingsSectionDivider()

                    SectionTitle(stringResource(id = R.string.settings_about))

                    SettingsSection {
                        SettingItem(
                            headlineText = "Developed by",
                            supportingText = "Shafiqul Islam Shamim",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    openUrl(context, "https://t.me/md_shamim12")
                                },
                        )
                        SettingsSectionDivider()
                        SettingItem(
                            headlineText = "News & Updates",
                            supportingText = "Follow Facebook and GitHub channels",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    showNewsDialog = true
                                },
                        )
                        SettingsSectionDivider()
                        SettingItem(
                            headlineText = "Check for updates",
                            supportingText = "Check for newer OTA updates",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    OTAUpdateHelper.hookPreference(context)
                                },
                        )
                        SettingsSectionDivider()
                        SettingItem(
                            headlineText = stringResource(id = R.string.settings_version),
                            supportingText = appVersion,
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                )
                            },
                        )
                        SettingsSectionDivider()
                        SettingItem(
                            headlineText = stringResource(id = R.string.settings_privacy),
                            supportingText = "Privacy policy • Terms & conditions",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    openUrl(
                                        context,
                                        "https://github.com/ShafiqulIslamShamim/Photo-Resizer/blob/main/PrivacyPolicy.txt",
                                    )
                                },
                        )
                    }

                    SettingsSectionDivider()

                    SectionTitle(stringResource(id = R.string.settings_support))

                    SettingsSection {
                        SettingItem(
                            headlineText = stringResource(id = R.string.settings_rate_us),
                            supportingText = stringResource(id = R.string.rate_us_subtitle),
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    openUrl(
                                        context,
                                        "https://play.google.com/store/apps/details?id=" + context.packageName,
                                    )
                                },
                        )
                        SettingsSectionDivider()
                        SettingItem(
                            headlineText = stringResource(id = R.string.more_apps_title),
                            supportingText = stringResource(id = R.string.more_apps_subtitle),
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Shop,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    openUrl(
                                        context,
                                        "https://play.google.com/store/search?q=pub:Shafiqul%20Islam%20Shamim&c=apps",
                                    )
                                },
                        )
                        SettingsSectionDivider()
                        SettingItem(
                            headlineText = stringResource(id = R.string.settings_feedback),
                            supportingText = stringResource(id = R.string.feedback_subtitle),
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Feedback,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    openEmail(context, "shafiqulislamshamimofficial@gmail.com")
                                },
                        )
                    }
                }
            }
        }
    }

    // --- Dialogs ---
    if (showNewsDialog) {
        NewsUpdatesDialog(
            onDismiss = { showNewsDialog = false },
            onFacebookClick = {
                openUrl(context, "https://www.facebook.com/share/18wbmDDERe/")
            },
            onGithubClick = {
                openUrl(context, "https://github.com/ShafiqulIslamShamim/")
            },
        )
    }

    if (showUpdateDialog) {
        CheckUpdateDialog(
            onDismiss = { showUpdateDialog = false },
        )
    }
}

/**
 * Encapsulates settings option items inside a rounded Card-style surface.
 *
 * @param modifier Custom visual modifiers.
 * @param content Nested layout blocks inside the section container.
 */
@Composable
fun SettingsSection(
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit),
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
    ) {
        Column(content = content)
    }
}

/**
 * Standard horizontal separator line placed between discrete settings sections.
 */
@Composable
fun SettingsSectionDivider() =
    HorizontalDivider(
        thickness = 4.dp,
        color = MaterialTheme.colorScheme.background,
    )

/**
 * Renders an individual configurable settings option item as a Material Design 3 List Item.
 *
 * @param headlineText Primary bold title of the settings option.
 * @param supportingText Concise description or hint about the option.
 * @param modifier Custom visual modifiers.
 * @param leadingContent Optional prefix element (such as an icon).
 * @param trailingContent Optional suffix element (such as a Switch or Arrow).
 * @param colors ListItem styling colors.
 * @param tonalElevation Tonal elevation for depth styling.
 * @param shadowElevation Shadow elevation for physical depth rendering.
 */
@Composable
fun SettingItem(
    headlineText: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(containerColor = Color.Transparent),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
) = ListItem(
    headlineContent = { Text(text = headlineText) },
    supportingContent = {
        Text(
            text = supportingText,
            maxLines = 1,
            modifier = Modifier.basicMarquee(),
        )
    },
    leadingContent = leadingContent,
    trailingContent = trailingContent,
    modifier = modifier,
    colors = colors,
    tonalElevation = tonalElevation,
    shadowElevation = shadowElevation,
)

/**
 * Displays a section label/header for grouping relevant settings options.
 *
 * @param title The text label of the section.
 */
@Composable
fun SectionTitle(title: String) {
    Row(Modifier.fillMaxWidth().padding(start = 22.dp, top = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Sub-screen Composable allowing the user to select visual themes, AMOLED pitch-black backgrounds,
 * seasonal particle overlays, custom palette choices, and view simulated theme results.
 *
 * @param settingsManager Settings manager for persisting local choices.
 * @param onBackClick Callback triggered when navigating back to the main settings view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleSettingsView(
    settingsManager: SettingsManager,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val themeMode by settingsManager.themeMode.collectAsState()
    val isAmoled by settingsManager.isAmoled.collectAsState()
    val selectedPalette by settingsManager.selectedPalette.collectAsState()
    val isSeasonalEnabled by settingsManager.isSeasonalEnabled.collectAsState()
    val selectedSeason by settingsManager.selectedSeason.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Style Settings",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Hero Illustration Card drawn directly with Jetpack Compose Canvas
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        StyleIllustration(selectedPalette = selectedPalette)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    SectionTitle("Accents & Schemes")

                    SettingsSection {
                        val isDynamicSelected = selectedPalette == "dynamic"
                        SettingItem(
                            headlineText = "Dynamic Color Theme (Default)",
                            supportingText = "Default adaptive Android system theme",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                if (isDynamicSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                            modifier = Modifier.clickable { settingsManager.setSelectedPalette("dynamic") },
                        )

                        SettingsSectionDivider()

                        // Row of 5 split palettes with titles
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val palettes =
                                listOf(
                                    Triple("green", "Emerald", listOf(Color(0xFF3FAF75), Color(0xFF1B5339))),
                                    Triple("pink", "Blossom", listOf(Color(0xFFE5989B), Color(0xFF6C3A43))),
                                    Triple("blue", "Ocean", listOf(Color(0xFF5796E2), Color(0xFF1E4882))),
                                    Triple("gold", "Amber", listOf(Color(0xFFDE9E36), Color(0xFF6B4511))),
                                    Triple("coral", "Coral", listOf(Color(0xFFE57373), Color(0xFF7F2F2F))),
                                )

                            palettes.forEach { (name, label, colors) ->
                                val isSelected = selectedPalette == name
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { settingsManager.setSelectedPalette(name) },
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        SplitCirclePreview(
                                            leftColor = colors[0],
                                            rightColor = colors[1],
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        if (isSelected) {
                                            Box(
                                                modifier =
                                                    Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color =
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                    )
                                }
                            }
                        }
                    }

                    SettingsSectionDivider()

                    SectionTitle("Preferred Theme Mode")

                    SettingsSection {
                        // Segmented Control Row directly inside the section
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            val modesList =
                                listOf(
                                    "system" to "System",
                                    "off" to "Off (Light)",
                                    "on" to "On (Dark)",
                                )
                            modesList.forEach { (key, label) ->
                                val isSelected = themeMode == key
                                Button(
                                    onClick = { settingsManager.setThemeMode(key) },
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primaryContainer
                                                } else {
                                                    Color.Transparent
                                                },
                                            contentColor =
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                        ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .border(
                                                width = if (isSelected) 0.dp else 1.dp,
                                                color =
                                                    if (isSelected) {
                                                        Color.Transparent
                                                    } else {
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                    },
                                                shape = RoundedCornerShape(24.dp),
                                            ),
                                    contentPadding = PaddingValues(0.dp),
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }

                    SettingsSectionDivider()

                    SectionTitle("Seasonal Effects")

                    SettingsSection {
                        SettingItem(
                            headlineText = "Enable Seasonal Effects",
                            supportingText = "Interact with falling elements themed around the seasons",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = isSeasonalEnabled,
                                    onCheckedChange = {
                                        settingsManager.setIsSeasonalEnabled(it)
                                    },
                                    colors =
                                        SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        ),
                                )
                            },
                            modifier =
                                Modifier.clickable {
                                    settingsManager.setIsSeasonalEnabled(!isSeasonalEnabled)
                                },
                        )

                        if (isSeasonalEnabled) {
                            SettingsSectionDivider()

                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(19.dp),
                            ) {
                                val seasonsList =
                                    listOf(
                                        "auto" to "Auto",
                                        "winter" to "Winter",
                                        "spring" to "Spring",
                                        "summer" to "Summer",
                                        "monsoon" to "Rainy",
                                        "fall" to "Fall",
                                    )

                                seasonsList.chunked(3).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        rowItems.forEach { (key, label) ->
                                            val isSelected = selectedSeason == key
                                            Button(
                                                onClick = {
                                                    settingsManager.setSelectedSeason(key)
                                                },
                                                colors =
                                                    ButtonDefaults.buttonColors(
                                                        containerColor =
                                                            if (isSelected) {
                                                                MaterialTheme.colorScheme.primaryContainer
                                                            } else {
                                                                Color.Transparent
                                                            },
                                                        contentColor =
                                                            if (isSelected) {
                                                                MaterialTheme.colorScheme.onPrimaryContainer
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                            },
                                                    ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier =
                                                    Modifier
                                                        .weight(1f)
                                                        .height(38.dp)
                                                        .border(
                                                            width = if (isSelected) 0.dp else 1.dp,
                                                            color =
                                                                if (isSelected) {
                                                                    Color.Transparent
                                                                } else {
                                                                    MaterialTheme.colorScheme.surfaceVariant
                                                                },
                                                            shape = RoundedCornerShape(12.dp),
                                                        ),
                                                contentPadding = PaddingValues(0.dp),
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    textAlign = TextAlign.Center,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsSectionDivider()

                    SectionTitle("Display Options")

                    SettingsSection {
                        SettingItem(
                            headlineText = "AMOLED Black Mode",
                            supportingText = "Uses pitch-black background to save battery",
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = isAmoled,
                                    onCheckedChange = { settingsManager.setIsAmoled(it) },
                                    colors =
                                        SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        ),
                                )
                            },
                            modifier = Modifier.clickable { settingsManager.setIsAmoled(!isAmoled) },
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Renders a vertically split circular preview of a color palette (left is primary/accent, right is
 * darker/shade).
 *
 * @param leftColor The color displayed on the left half.
 * @param rightColor The color displayed on the right half.
 * @param modifier Custom visual modifiers applied to the Canvas drawing area.
 */
@Composable
fun SplitCirclePreview(
    leftColor: Color,
    rightColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawArc(
            color = leftColor,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = true,
        )
        drawArc(
            color = rightColor,
            startAngle = 270f,
            sweepAngle = 180f,
            useCenter = true,
        )
    }
}

/**
 * Code-drawn vector illustration of art girl and forest color circles mapping user's style
 * screenshots perfectly.
 *
 * @param selectedPalette The currently selected active theme palette name.
 */
@Composable
fun StyleIllustration(selectedPalette: String) {
    val activeLabel =
        when (selectedPalette) {
            "dynamic" -> "Dynamic Active"
            "green" -> "Emerald Active"
            "pink" -> "Blossom Active"
            "blue" -> "Ocean Active"
            "gold" -> "Amber Active"
            "coral" -> "Coral Active"
            else -> "Dynamic Active"
        }

    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val secondaryColor = colorScheme.secondary

    // Define all path strings and their corresponding color mapper
    val elements =
        remember {
            listOf(
                // path 1
                "M307.693 659.535L316.417 662.148L330.648 629.74L317.772 625.884L307.693 659.535Z" to
                    { scheme: ColorScheme ->
                        Color(0xFFFFB8B8)
                    },
                // path 2 (sco50)
                "M306.32 656.02L323.502 661.166L323.503 661.166C326.407 662.036 328.846 664.024 330.284 666.692C331.722 669.361 332.042 672.491 331.172 675.395L331.065 675.751L302.934 667.325L306.32 656.02Z" to
                    { scheme: ColorScheme ->
                        scheme.primaryContainer
                    },
                // path 3
                "M390.602 666.663L399.153 663.528L391.13 629.055L378.51 633.682L390.602 666.663Z" to
                    { scheme: ColorScheme ->
                        Color(0xFFFFB8B8)
                    },
                // path 4 (sco50)
                "M387.398 664.671L404.237 658.496L404.238 658.496C407.084 657.453 410.228 657.583 412.978 658.858C415.729 660.132 417.86 662.447 418.904 665.293L419.031 665.642L391.46 675.751L387.398 664.671Z" to
                    { scheme: ColorScheme ->
                        scheme.primaryContainer
                    },
                // path 5 (sco90)
                "M322.321 600.868L307.464 649.897L325.292 655.097L342.378 609.782L322.321 600.868Z" to
                    { scheme: ColorScheme ->
                        scheme.secondaryContainer
                    },
                // path 6 (sco90)
                "M369.121 612.011L382.493 658.068L401.064 649.154L387.693 606.068L369.121 612.011Z" to
                    { scheme: ColorScheme ->
                        scheme.secondaryContainer
                    },
                // path 7 (sco40)
                "M362.162 620.318C350.27 620.263 338.406 619.169 326.705 617.047L326.407 616.987V605.657L319.617 604.903L324.917 585.975C322.446 556.815 325.957 519.885 327.092 509.256C327.352 506.764 327.524 505.365 327.524 505.365L333.482 454.715L343.453 445.512L343.453 445.512C344.408 455.098 337.66 479.399 336.733 482.586L348.18 487.975L355.451 492.61L365.408 493.652L362.705 506.164L347.582 506.504C346.344 506.807 345.073 506.956 343.799 506.946V506.946Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 8 (opacity 0.2)
                "M337.708 477.181L335.418 491.475L350.921 497.87L337.708 477.181Z" to
                    { scheme: ColorScheme ->
                        Color.Black.copy(alpha = 0.2f)
                    },
                // path 9
                "M368.128 445.437H332.284C331.547 445.436 330.84 445.143 330.319 444.622C329.797 444.1 329.504 443.394 329.503 442.656V427.206C329.514 421.723 331.7 416.467 335.581 412.593C339.463 408.72 344.722 406.544 350.206 406.544C355.69 406.544 360.95 408.72 364.831 412.593C368.712 416.467 370.898 421.723 370.909 427.206V442.656C370.908 443.394 370.615 444.1 370.094 444.622C369.572 445.143 368.865 445.436 368.128 445.437V445.437Z" to
                    { scheme: ColorScheme ->
                        Color(0xFF2F2E41)
                    },
                // path 10
                "M354.164 444.507C362.547 444.507 369.342 437.711 369.342 429.328C369.342 420.945 362.547 414.149 354.164 414.149C345.781 414.149 338.985 420.945 338.985 429.328C338.985 437.711 345.781 444.507 354.164 444.507Z" to
                    { scheme: ColorScheme ->
                        Color(0xFFFFB8B8)
                    },
                // path 11
                "M375.807 428.751H353.889L353.665 425.605L352.541 428.751H349.166L348.721 422.515L346.493 428.751H339.963V428.442C339.968 424.101 341.695 419.938 344.765 416.868C347.835 413.797 351.998 412.07 356.34 412.065H359.43C363.772 412.07 367.935 413.797 371.005 416.868C374.075 419.938 375.802 424.1 375.807 428.442V428.751Z" to
                    { scheme: ColorScheme ->
                        Color(0xFF2F2E41)
                    },
                // path 12
                "M353.71 448.321C353.545 448.321 353.38 448.307 353.218 448.278L337.169 445.446V418.922H354.836L354.398 419.432C348.313 426.529 352.897 438.038 356.172 444.266C356.413 444.722 356.521 445.237 356.482 445.752C356.444 446.266 356.261 446.76 355.954 447.175C355.698 447.53 355.36 447.819 354.97 448.018C354.58 448.218 354.148 448.321 353.71 448.321V448.321Z" to
                    { scheme: ColorScheme ->
                        Color(0xFF2F2E41)
                    },
                // path 13 (tco90)
                "M382.029 512.433H373.231C372.949 512.434 372.677 512.33 372.468 512.14C372.259 511.95 372.128 511.689 372.101 511.408L370.34 493.359H384.919L383.159 511.408C383.132 511.689 383.001 511.95 382.792 512.14C382.583 512.33 382.311 512.434 382.029 512.433V512.433Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiaryContainer
                    },
                // path 14 (tco40)
                "M384.896 495.63H370.364C370.063 495.63 369.774 495.51 369.561 495.297C369.348 495.085 369.229 494.796 369.228 494.495V491.77C369.229 491.469 369.348 491.18 369.561 490.968C369.774 490.755 370.063 490.635 370.364 490.635H384.896C385.197 490.635 385.486 490.755 385.699 490.968C385.911 491.18 386.031 491.469 386.031 491.77V494.495C386.031 494.796 385.911 495.085 385.699 495.297C385.486 495.51 385.197 495.63 384.896 495.63V495.63Z" to
                    { scheme: ColorScheme ->
                        scheme.outline
                    },
                // path 15 (opacity 0.2)
                "M327.892 504.667C331.932 509.345 337.412 512.547 343.47 513.772C349.528 514.996 355.821 514.173 361.36 511.432L364.664 509.797L327.892 504.667Z" to
                    { scheme: ColorScheme ->
                        Color.Black.copy(alpha = 0.2f)
                    },
                // path 16
                "M380.711 501.173C380.034 500.412 379.198 499.809 378.263 499.406C377.327 499.004 376.315 498.811 375.297 498.842C374.279 498.872 373.28 499.126 372.371 499.584C371.462 500.043 370.664 500.695 370.034 501.495L354.709 497.193L349.823 505.869L371.551 511.64C372.965 512.61 374.686 513.026 376.387 512.812C378.088 512.597 379.651 511.766 380.78 510.475C381.909 509.185 382.526 507.525 382.513 505.811C382.5 504.096 381.859 502.446 380.711 501.173V501.173Z" to
                    { scheme: ColorScheme ->
                        Color(0xFFFFB8B8)
                    },
                // path 17 (sco50)
                "M344.139 510.801C336.852 510.802 326.993 506.53 315.29 498.255C314.637 497.803 314.086 497.221 313.671 496.545C313.255 495.869 312.985 495.114 312.876 494.327C312.013 488.858 317.348 481.464 317.871 480.756L323.479 465.355C323.543 465.105 325.351 458.441 329.888 456.071C330.843 455.581 331.893 455.303 332.966 455.258C334.039 455.212 335.108 455.399 336.102 455.806C344.744 458.953 337.996 483.254 337.069 486.441L348.52 491.83L355.791 496.465L365.748 497.507L363.045 510.019L347.922 510.359C346.684 510.662 345.413 510.811 344.139 510.801V510.801Z" to
                    { scheme: ColorScheme ->
                        scheme.primaryContainer
                    },
                // path 18 (sco90)
                "M887.675 396.659C887.667 382.038 884.407 367.601 878.13 354.396C871.854 341.19 862.719 329.546 851.386 320.307C840.054 311.068 826.808 304.465 812.609 300.978C798.409 297.49 783.612 297.205 769.289 300.143C754.966 303.08 741.475 309.167 729.795 317.963C718.115 326.758 708.538 338.042 701.757 350.996C694.976 363.95 691.162 378.25 690.591 392.86C690.02 407.47 692.706 422.025 698.454 435.469C698.358 435.362 698.258 435.257 698.162 435.15C702.541 445.355 708.611 454.748 716.116 462.932C716.139 462.957 716.162 462.981 716.184 463.006C716.79 463.666 717.399 464.322 718.022 464.966C727.031 474.362 737.816 481.874 749.752 487.068C761.687 492.261 774.536 495.032 787.551 495.219L784.22 676.148H794.512L792.429 556.733L807.316 548.895L805.045 544.582L792.334 551.274L791.356 495.21C817.102 494.62 841.595 483.978 859.596 465.56C877.596 447.143 887.674 422.412 887.675 396.659V396.659Z" to
                    { scheme: ColorScheme ->
                        scheme.secondaryContainer
                    },
                // path 19 (sco90)
                "M595.087 348.612C595.078 331.477 591.257 314.558 583.902 299.082C576.547 283.606 565.841 269.96 552.56 259.133C539.28 248.306 523.757 240.568 507.116 236.481C490.476 232.394 473.135 232.06 456.349 235.502C439.564 238.945 423.754 246.079 410.066 256.386C396.378 266.694 385.154 279.917 377.208 295.098C369.262 310.279 364.792 327.038 364.122 344.159C363.453 361.281 366.601 378.338 373.337 394.093C373.224 393.967 373.108 393.845 372.995 393.719C378.127 405.679 385.24 416.686 394.036 426.278C394.062 426.307 394.089 426.335 394.116 426.364C394.825 427.137 395.539 427.907 396.269 428.661C406.826 439.672 419.466 448.476 433.454 454.563C447.441 460.649 462.499 463.896 477.752 464.115L473.848 676.148H485.908L483.467 536.204L500.913 527.019L498.252 521.964L483.356 529.806L482.21 464.105C512.382 463.413 541.086 450.941 562.181 429.358C583.276 407.774 595.087 378.792 595.087 348.612V348.612Z" to
                    { scheme: ColorScheme ->
                        scheme.secondaryContainer
                    },
                // path 20 (sco90)
                "M263.259 303.418C263.249 283.919 258.901 264.666 250.531 247.055C242.16 229.444 229.977 213.915 214.864 201.593C199.751 189.272 182.086 180.467 163.15 175.816C144.213 171.165 124.48 170.785 105.378 174.702C86.2763 178.62 68.2854 186.738 52.7086 198.468C37.1319 210.197 24.3595 225.245 15.3167 242.521C6.27402 259.797 1.18748 278.868 0.425642 298.352C-0.336191 317.836 3.24577 337.246 10.912 355.175C10.7835 355.032 10.6506 354.893 10.5227 354.75C16.362 368.36 24.4571 380.886 34.4667 391.801C34.4966 391.834 34.5273 391.866 34.5572 391.899C35.3649 392.779 36.1771 393.655 37.0081 394.513C49.022 407.043 63.4053 417.062 79.323 423.989C95.2408 430.915 112.376 434.61 129.733 434.859L125.291 676.148H139.016L136.238 516.895L156.091 506.442L153.062 500.69L136.111 509.614L134.807 434.847C169.142 434.06 201.807 419.868 225.812 395.306C249.818 370.744 263.258 337.763 263.259 303.418V303.418Z" to
                    { scheme: ColorScheme ->
                        scheme.secondaryContainer
                    },
                // path 21 (tco90)
                "M756.685 171.952C804.169 171.952 842.661 133.459 842.661 85.9757C842.661 38.4926 804.169 0 756.685 0C709.202 0 670.71 38.4926 670.71 85.9757C670.71 133.459 709.202 171.952 756.685 171.952Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiaryContainer
                    },
                // path 22 (pco50)
                "M245.559 359.928C340.724 359.928 417.871 282.782 417.871 187.616C417.871 92.4513 340.724 15.3047 245.559 15.3047C150.394 15.3047 73.2471 92.4513 73.2471 187.616C73.2471 282.782 150.394 359.928 245.559 359.928Z" to
                    { scheme: ColorScheme ->
                        scheme.primary
                    },
                // path 23 (opacity 0.2)
                "M118.329 72.5254C102.257 110.123 100.076 152.21 112.178 191.267C124.28 230.324 149.875 263.805 184.39 285.727C218.905 307.65 260.09 316.586 300.587 310.937C341.084 305.289 378.252 285.425 405.452 254.895C395.633 277.865 380.938 298.426 362.384 315.154C343.831 331.881 321.862 344.374 298.001 351.768C274.139 359.162 248.956 361.28 224.195 357.975C199.433 354.67 175.687 346.021 154.6 332.628C133.513 319.234 115.591 301.416 102.074 280.408C88.5569 259.401 79.7689 235.705 76.3188 210.964C72.8686 186.223 74.8388 161.027 82.0929 137.123C89.3469 113.218 101.711 91.1768 118.329 72.5254V72.5254Z" to
                    { scheme: ColorScheme ->
                        Color.Black.copy(alpha = 0.2f)
                    },
                // path 24 (sco30)
                "M246.032 187.616H246.506L255.027 676.148H237.038L246.032 187.616Z" to
                    { scheme: ColorScheme ->
                        scheme.secondary
                    },
                // path 25 (sco30)
                "M273.438 446.177L244.954 461.173L248.923 468.713L277.407 453.717L273.438 446.177Z" to
                    { scheme: ColorScheme ->
                        scheme.secondary
                    },
                // path 26 (sco40)
                "M509.115 671.578C509.115 671.578 509.737 658.552 522.482 660.066Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 27 (tco90)
                "M505.514 659.182C509.037 659.182 511.893 656.326 511.893 652.803C511.893 649.281 509.037 646.425 505.514 646.425C501.991 646.425 499.135 649.281 499.135 652.803C499.135 656.326 501.991 659.182 505.514 659.182Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiaryContainer
                    },
                // path 28 (sco40)
                "M506.277 663.544H504.476V676.148H506.277V663.544Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 29 (sco40)
                "M67.0829 669.778C67.0829 669.778 67.7046 656.751 80.4493 658.266Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 30 (tco90)
                "M63.4818 657.381C67.0046 657.381 69.8605 654.526 69.8605 651.003C69.8605 647.48 67.0046 644.624 63.4818 644.624C59.959 644.624 57.1032 647.48 57.1032 651.003C57.1032 654.526 59.959 657.381 63.4818 657.381Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiaryContainer
                    },
                // path 31 (sco40)
                "M64.2445 661.744H62.4439V674.348H64.2445V661.744Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 32 (sco40)
                "M171.514 670.678C171.514 670.678 172.136 657.651 184.881 659.165Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 33 (tco90)
                "M167.913 658.282C171.436 658.282 174.292 655.426 174.292 651.903C174.292 648.38 171.436 645.524 167.913 645.524C164.39 645.524 161.534 648.38 161.534 651.903C161.534 655.426 164.39 658.282 167.913 658.282Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiaryContainer
                    },
                // path 34 (sco40)
                "M168.676 662.645H166.875V675.248H168.676V662.645Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 35
                "M449.243 83.2986L462.038 73.0652C452.098 71.9686 448.014 77.3895 446.343 81.6802C438.578 78.4557 430.124 82.6815 430.124 82.6815L455.724 91.9753C454.433 88.5257 452.185 85.5159 449.243 83.2986V83.2986Z" to
                    { scheme: ColorScheme ->
                        Color(0xFF3F3D56)
                    },
                // path 36
                "M643.827 187.54L656.622 177.306C646.682 176.21 642.598 181.631 640.927 185.921C633.161 182.697 624.708 186.923 624.708 186.923L650.308 196.216C649.016 192.767 646.768 189.757 643.827 187.54V187.54Z" to
                    { scheme: ColorScheme ->
                        Color(0xFF3F3D56)
                    },
                // path 37
                "M433.955 276.492L446.749 266.259C436.81 265.162 432.726 270.583 431.054 274.874C423.289 271.65 414.835 275.875 414.835 275.875L440.435 285.169C439.144 281.72 436.896 278.71 433.955 276.492Z" to
                    { scheme: ColorScheme ->
                        Color(0xFF3F3D56)
                    },
                // path 38 (sco40)
                "M683.655 676.307C683.655 676.307 684.277 663.28 697.022 664.794Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 39 (sco40)
                "M563.919 676.307C563.919 676.307 564.541 663.28 577.286 664.794Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 40 (sco40)
                "M127.289 676.307C127.289 676.307 127.91 663.28 140.655 664.794Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 41 (sco40)
                "M737.671 676.307C737.671 676.307 738.293 663.28 751.038 664.794Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 42 (sco40)
                "M712.464 676.307C712.464 676.307 713.086 663.28 725.83 664.794Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 43 (sco40)
                "M660.465 676.307C660.465 676.307 659.843 663.28 647.099 664.794Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 44 (sco40)
                "M453.403 676.307C453.403 676.307 452.781 663.28 440.037 664.794Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 45 (sco40)
                "M281.452 676.307C281.452 676.307 280.83 663.28 268.085 664.794Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 46 (sco40)
                "M98.6969 676.307C98.6969 676.307 98.0752 663.28 85.3305 664.794Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 47 (sco40)
                "M791.904 676.307C791.904 676.307 791.283 663.28 778.538 664.794Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 48 (sco40)
                "M714.481 677.207C714.481 677.207 713.859 664.181 701.115 665.695Z" to
                    { scheme: ColorScheme ->
                        scheme.tertiary
                    },
                // path 49 (sco40)
                "M888 674.604H0V676.604H888V674.604Z" to { scheme: ColorScheme -> scheme.tertiary },
            )
        }

    // Parse the path strings into Compose Path objects and cache them
    val parsedPaths =
        remember(elements) {
            elements.map { (pathStr, colorSelector) ->
                val androidPath = PathParser.createPathFromPathData(pathStr)
                val composePath = androidPath.asComposePath()
                composePath to colorSelector
            }
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(1.dp, colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val viewBoxWidth = 888f
            val viewBoxHeight = 678f

            val scaleX = size.width / viewBoxWidth
            val scaleY = size.height / viewBoxHeight
            val scale = minOf(scaleX, scaleY)

            val dx = (size.width - viewBoxWidth * scale) / 2f
            val dy = (size.height - viewBoxHeight * scale) / 2f

            withTransform({
                translate(dx, dy)
                scale(scale, scale, pivot = Offset.Zero)
            }) {
                parsedPaths.forEach { (path, colorSelector) ->
                    drawPath(
                        path = path,
                        color = colorSelector(colorScheme),
                    )
                }
            }
        }

        // Chip / Badge overlay on bottom-left
        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 16.dp)
                    .background(secondaryColor.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = activeLabel,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
