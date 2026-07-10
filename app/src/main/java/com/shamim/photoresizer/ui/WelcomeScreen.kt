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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shamim.photoresizer.R

/**
 * Renders the Welcome/Landing screen for the Photo Resizer application.
 * Prompts the user to select an image from the local gallery or capture a new photo with the camera,
 * and displays quick guide cards illustrating core features.
 *
 * @param onGalleryClick Callback triggered when clicking on the gallery selection action.
 * @param onCameraClick Callback triggered when clicking on the camera capture action.
 * @param onSettingsClick Callback triggered when opening the application's style/settings menu.
 * @param modifier Custom visual modifiers applied to the parent scaffold layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = "App Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("settings_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open Settings",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background, // এখানেই ব্যাকগ্রাউন্ড কালার দিন
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Rest of Welcome screen content inside a scrolling center-aligned Column
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                //  Spacer(modifier = Modifier.height(16.dp))

                // Large icon card with asymmetric radial background glow
                Box(
                    modifier =
                        Modifier
                            .size(140.dp)
                            .background(
                                brush =
                                    Brush.radialGradient(
                                        colors =
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                Color.Transparent,
                                            ),
                                    ),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        modifier =
                            Modifier
                                .size(96.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    RoundedCornerShape(24.dp),
                                ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = "App Logo Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

        /*

        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

         */

                Text(
                    text =
                        "Resize, crop, and compress metadata to fit any target dimensions and KB files instantly. Clean client-side rendering.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gallery Button
                Button(
                    onClick = onGalleryClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp).testTag("gallery_button"),
                    colors =
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery Icon",
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(id = R.string.btn_select_gallery),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Camera Button
                OutlinedButton(
                    onClick = onCameraClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp).testTag("camera_button"),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Icon",
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(id = R.string.btn_take_camera),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
