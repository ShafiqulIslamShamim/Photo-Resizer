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

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.shamim.photoresizer.Util
import com.shamim.photoresizer.ViewModel

/**
 * Renders the Configure Screen where users can override dimensions, lock/select aspect ratios,
 * define size limits (either auto-compress or target file KB/MB targets), rotate, and proceed
 * to cropping or processing.
 *
 * @param viewModel State management and interaction controller.
 * @param onBack Callback triggered when navigating back to the welcome/selection screen.
 * @param onSettingsClick Callback triggered to open the Settings screen activity.
 * @param modifier Custom visual modifiers applied to the parent scaffold layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureScreen(
    viewModel: ViewModel,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val meta = viewModel.originalMetadata
    val scrollState = rememberScrollState()

    var activeConfigSegment by rememberSaveable { mutableStateOf("Custom Size") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier =
                            Modifier
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .testTag("back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                title = {
                    Text(
                        text = "Resize Preferences",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background, // এখানেই ব্যাকগ্রাউন্ড কালার দিন
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(scrollState),
            ) {
                if (meta != null) {
                    // Displays loaded preview image inside a small card containing details
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = rememberAsyncImagePainter(model = meta.uri),
                                    contentDescription = "Photo loaded preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Original Image Metadata",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )

                            // Dimension metadata columns
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(
                                        "Original Format",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text("JPEG File", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(
                                        "Resolution",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "${meta.width} × ${meta.height} px",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Column {
                                    Text(
                                        "Ratio",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = Util.getAspectRatioText(meta.width, meta.height),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Column {
                                    Text(
                                        "File Size",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = Util.formatFileSize(meta.sizeBytes),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }

                    // Main Segmented Selector
                    Text(
                        text = "Resolution Mode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(14.dp),
                            ).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val segments = listOf("Custom Size", "Preset Size", "Aspect Ratio")
                        segments.forEach { segment ->
                            val isSelected = activeConfigSegment == segment
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.Transparent
                                        },
                                    ).clickable { 
                                        activeConfigSegment = segment
                                        if (segment == "Aspect Ratio") {
                                            viewModel.selectPreset(viewModel.selectedPresetRatio)
                                        } else if (segment == "Preset Size") {
                                            if (viewModel.selectedPresetSize != "None") {
                                                viewModel.selectPresetSize(viewModel.selectedPresetSize)
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = segment,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    when (activeConfigSegment) {
                        "Custom Size" -> {
                            // Custom target resolution textfields
                            Text(
                                text = "Override Target Resolution (Pixels)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                OutlinedTextField(
                                    value = viewModel.targetWidthInput,
                                    onValueChange = {
                                        viewModel.targetWidthInput = it.filter { ch -> ch.isDigit() }
                                        viewModel.selectedPresetRatio = "Custom"
                                        viewModel.selectedPresetSize = "None"
                                    },
                                    modifier = Modifier.weight(1f).testTag("target_width_field"),
                                    label = { Text("Width (px)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    ),
                                )

                                OutlinedTextField(
                                    value = viewModel.targetHeightInput,
                                    onValueChange = {
                                        viewModel.targetHeightInput = it.filter { ch -> ch.isDigit() }
                                        viewModel.selectedPresetRatio = "Custom"
                                        viewModel.selectedPresetSize = "None"
                                    },
                                    modifier = Modifier.weight(1f).testTag("target_height_field"),
                                    label = { Text("Height (px)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    ),
                                )
                            }
                        }

                        "Preset Size" -> {
                            // Preset Size Selector
                            Text(
                                text = "Standard Document Preset Sizes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )

                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp),
                                    ).padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                val sizePresets = listOf(
                                    "None",
                                    "Passport (2×2\")",
                                    "Passport (35×45 mm)",
                                    "Visa (US)",
                                    "ID Card (CR80)",
                                    "Stamp (25×35 mm)"
                                )
                                sizePresets.forEach { preset ->
                                    val isSelected = viewModel.selectedPresetSize == preset
                                    Box(
                                        modifier = Modifier
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    Color.Transparent
                                                },
                                            ).clickable { viewModel.selectPresetSize(preset) }
                                            .padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = preset,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        "Aspect Ratio" -> {
                            // Aspect Ratio Selector
                            Text(
                                text = "Aspect Ratio",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )

                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp),
                                    ).padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                val aspectPresets = listOf("Original", "1:1", "4:3", "16:9", "9:16", "2:3", "3:2", "21:9")
                                aspectPresets.forEach { preset ->
                                    val isSelected = viewModel.selectedPresetRatio == preset
                                    Box(
                                        modifier = Modifier
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    Color.Transparent
                                                },
                                            ).clickable { viewModel.selectPreset(preset) }
                                            .padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = preset,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Size limit textfield title and picker
                    Text(
                        text = "File Size Optimization",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                            ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Segmented Selector for Auto Mode vs Custom Size Limit
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp),
                                        ).padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                // Auto Option (Default)
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (viewModel.isAutoCompress) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    Color.Transparent
                                                },
                                            ).clickable { viewModel.isAutoCompress = true },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "Auto (High Quality)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color =
                                            if (viewModel.isAutoCompress) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                    )
                                }

                                // Custom Target Size Option
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (!viewModel.isAutoCompress) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    Color.Transparent
                                                },
                                            ).clickable { viewModel.isAutoCompress = false },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "Custom Target Limit",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color =
                                            if (!viewModel.isAutoCompress) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                    )
                                }
                            }

                            if (viewModel.isAutoCompress) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "Auto description icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text =
                                            "Preserve maximum fidelity and clarity using standard default JPEG optimization without forced downscaling.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            } else {
                                Text(
                                    text = "Configure maximum forced target image size limit below:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Target Limit Number Input with Rounded Corners!
                                    OutlinedTextField(
                                        value = viewModel.sizeLimitInput,
                                        onValueChange = { input ->
                                            viewModel.sizeLimitInput = input.filter { it.isDigit() }
                                        },
                                        modifier = Modifier.weight(1.3f).testTag("size_limit_field"),
                                        label = { Text("Limit amount") },
                                        placeholder = { Text("e.g. 500") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(24.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors =
                                            OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            ),
                                    )

                                    // Unified KB vs MB selector row with rounded corners!
                                    Row(
                                        modifier =
                                            Modifier
                                                .weight(1f)
                                                .height(56.dp)
                                                .background(
                                                    color =
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(24.dp),
                                                ).padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        listOf("KB", "MB").forEach { unit ->
                                            val isSelected = viewModel.sizeLimitUnit == unit
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(
                                                            if (isSelected) {
                                                                MaterialTheme.colorScheme.primaryContainer
                                                            } else {
                                                                Color.Transparent
                                                            },
                                                        ).clickable { viewModel.sizeLimitUnit = unit },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = unit,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color =
                                                        if (isSelected) {
                                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action: Crop and configure (Save)
                    Button(
                        onClick = {
                            val wInput = viewModel.targetWidthInput.toIntOrNull()
                            val hInput = viewModel.targetHeightInput.toIntOrNull()
                            if (wInput == null || wInput <= 0 || hInput == null || hInput <= 0) {
                                Toast
                                    .makeText(
                                        context,
                                        "Please configure valid non-zero target pixels",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            } else {
                                viewModel.openCropping()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).testTag("crop_save_button"),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.Crop, contentDescription = "Crop and Save icon")
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Crop and Save Selection", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
