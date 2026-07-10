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

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UnfoldMore
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.shamim.photoresizer.Util
import com.shamim.photoresizer.ViewModel

enum class ComparisonMode {
    SPLIT_SLIDER,
    SIDE_BY_SIDE,
    QUICK_TOGGLE,
}

/**
 * Renders the Preview Screen.
 * Displays comparison tools (split-slider, side-by-side, quick-toggle comparison views),
 * processed file metadata, and formats selection (JPEG, PNG, WEBP) to save to public gallery.
 *
 * @param viewModel State management and interaction controller.
 * @param onSettingsClick Callback triggered to open the Settings screen activity.
 * @param modifier Custom visual modifiers applied to the parent scaffold layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    viewModel: ViewModel,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val originalMeta = viewModel.originalMetadata
    val processedBmp = viewModel.processedBitmap
    val savedBytes = viewModel.processedSizeBytes
    val scrollState = rememberScrollState()

    // Preview Mode Selection State
    var selectedCompareMode by remember { mutableStateOf(ComparisonMode.SPLIT_SLIDER) }
    // Backwards-compatible single toggle state
    var showActiveAfterState by remember { mutableStateOf(true) }
    // Selected format for saving the processed image
    var selectedFormat by remember { mutableStateOf("JPG") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.reset() },
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
                        text = "Optimization Results",
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
                if (originalMeta != null && processedBmp != null) {
                    // Before vs After savings summary badge pill
                    val savingPct =
                        if (originalMeta.sizeBytes > 0) {
                            val factor =
                                (originalMeta.sizeBytes - savedBytes).toFloat() / originalMeta.sizeBytes
                            (factor * 100).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }

                    // Stats badge
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = "$savingPct% Scaled Down",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text =
                                        "Compressed ${Util.formatFileSize(originalMeta.sizeBytes)} to ${Util.formatFileSize(savedBytes)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    text =
                                        "A perfect slim copy optimized for online portals, applications, or social sharing.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }

                    // Beautiful segmented layout comparing modes
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp),
                                ).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val modes =
                            listOf(
                                ComparisonMode.SPLIT_SLIDER to "Split View",
                                ComparisonMode.SIDE_BY_SIDE to "Dual Grid",
                                ComparisonMode.QUICK_TOGGLE to "Tab Switch",
                            )

                        modes.forEach { (mode, title) ->
                            val isSelected = selectedCompareMode == mode
                            Box(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                Color.Transparent
                                            },
                                        ).clickable { selectedCompareMode = mode }
                                        .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color =
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                )
                            }
                        }
                    }

                    // Central Dynamic Comparison Viewport
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            when (selectedCompareMode) {
                                ComparisonMode.SPLIT_SLIDER -> {
                                    InteractiveSplitCompare(
                                        originalUri = originalMeta.uri,
                                        processedBmp = processedBmp,
                                    )
                                }

                                ComparisonMode.SIDE_BY_SIDE -> {
                                    SideBySideCompare(
                                        originalUri = originalMeta.uri,
                                        processedBmp = processedBmp,
                                        originalSize = originalMeta.sizeBytes,
                                        processedSize = savedBytes,
                                    )
                                }

                                ComparisonMode.QUICK_TOGGLE -> {
                                    SimpleToggleCompare(
                                        originalUri = originalMeta.uri,
                                        processedBmp = processedBmp,
                                        showProcessed = showActiveAfterState,
                                        onToggle = { showActiveAfterState = it },
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Meta descriptor rows
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        "Output Dimensions",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        "${processedBmp.width} × ${processedBmp.height} px",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "New File Size",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = Util.formatFileSize(savedBytes),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    // Format selection header
                    Text(
                        text = "Export File Format",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    )

                    // Dynamic segment format picker
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp),
                                ).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val formats = listOf("JPG", "PNG", "WEBP")
                        formats.forEach { format ->
                            val isSelected = selectedFormat == format
                            Box(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                Color.Transparent
                                            },
                                        ).clickable { selectedFormat = format }
                                        .testTag("format_picker_$format"),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = format,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color =
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                )
                            }
                        }
                    }

                    // Expose export and save options
                    Button(
                        onClick = {
                            val savedUri =
                                Util.saveBitmapToPublicStorage(
                                    context = context,
                                    bitmap = processedBmp,
                                    quality = 90,
                                    formatStr = selectedFormat,
                                    sizeLimitKb = null,
                                )
                            if (savedUri != null) {
                                Toast
                                    .makeText(
                                        context,
                                        "Saved successfully to Pictures/Photo-Resizer!",
                                        Toast.LENGTH_LONG,
                                    ).show()
                            } else {
                                Toast.makeText(context, "Failed to export image", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).testTag("save_to_gallery_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Icon")
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Save Image to Pictures", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry Icon")
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Process Another Photo", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * A slidable split-view comparison Composable.
 * Allows users to drag a central divider handle to view the original photo (on the left side)
 * and the newly compressed/scaled photo (on the right side) seamlessly over layered canvasses.
 *
 * @param originalUri Content Uri representing the original loaded photo.
 * @param processedBmp Fully scaled/compressed bitmap results.
 */
@Composable
fun InteractiveSplitCompare(
    originalUri: android.net.Uri,
    processedBmp: Bitmap,
) {
    var sliderFraction by remember { mutableStateOf(0.5f) }

    Column {
        Text(
            text = "◀ Drag handle to compare Original (Left) vs Processed (Right) ▶",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            textAlign = TextAlign.Center,
        )

        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.05f)),
        ) {
            val componentWidth = constraints.maxWidth.toFloat()

            // Original Image (Background)
            androidx.compose.foundation.Image(
                painter = rememberAsyncImagePainter(model = originalUri),
                contentDescription = "Original Background Selection",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )

            // Processed Image (Foreground Overlap with clip)
            Box(
                modifier =
                    Modifier.fillMaxSize().graphicsLayer {
                        clip = true
                        shape =
                            object : Shape {
                                override fun createOutline(
                                    size: androidx.compose.ui.geometry.Size,
                                    layoutDirection: LayoutDirection,
                                    density: Density,
                                ): Outline =
                                    Outline.Rectangle(
                                        androidx.compose.ui.geometry.Rect(
                                            left = size.width * sliderFraction,
                                            top = 0f,
                                            right = size.width,
                                            bottom = size.height,
                                        ),
                                    )
                            }
                    },
            ) {
                androidx.compose.foundation.Image(
                    bitmap = processedBmp.asImageBitmap(),
                    contentDescription = "Processed Overlay Selection",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Divider vertical Line
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(2.5.dp)
                        .background(Color.White)
                        .offset(x = maxWidth * sliderFraction),
            )

            // Slider Drag pill Handle button
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = (maxWidth * sliderFraction) - 20.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaFraction = dragAmount.x / componentWidth
                                sliderFraction = (sliderFraction + deltaFraction).coerceIn(0.001f, 0.999f)
                            }
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = "Slidable comparison splitter",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp).graphicsLayer(rotationZ = 90f),
                )
            }
        }
    }
}

/**
 * Side-by-side side dual layout comparing original and processed photos directly.
 * Displays both images as card frames with file sizes labeled immediately below them for reference.
 *
 * @param originalUri Content Uri of the original photo.
 * @param processedBmp Scale-adjusted/compressed resulting Bitmap.
 * @param originalSize Metadata file size of the original image in bytes.
 * @param processedSize Calculated file size of the processed image in bytes.
 */
@Composable
fun SideBySideCompare(
    originalUri: android.net.Uri,
    processedBmp: Bitmap,
    originalSize: Long,
    processedSize: Long,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Original Box
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.05f)),
        ) {
            androidx.compose.foundation.Image(
                painter = rememberAsyncImagePainter(model = originalUri),
                contentDescription = "Before selection",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "Before (${Util.formatFileSize(originalSize)})",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Processed Box
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.05f)),
        ) {
            androidx.compose.foundation.Image(
                bitmap = processedBmp.asImageBitmap(),
                contentDescription = "After Selection",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            RoundedCornerShape(6.dp),
                        ).padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "After (${Util.formatFileSize(processedSize)})",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/**
 * Quick tap comparison Composable.
 * Renders tab selections for Original and Processed states. Allows the user to quickly
 * click back and forth between states to compare fine compression details of the same region.
 *
 * @param originalUri Content Uri of the original photo.
 * @param processedBmp Scaled/compressed resulting Bitmap.
 * @param showProcessed True to display the processed photo, false for original.
 * @param onToggle Callback triggered on tab clicks, updating comparison modes.
 */
@Composable
fun SimpleToggleCompare(
    originalUri: android.net.Uri,
    processedBmp: Bitmap,
    showProcessed: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Column {
        // Toggles tab line
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                    .padding(3.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (showProcessed) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            },
                        ).clickable { onToggle(true) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Optimized View",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (showProcessed) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
            }
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (!showProcessed) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            },
                        ).clickable { onToggle(false) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Original View",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (!showProcessed) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = showProcessed,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                androidx.compose.foundation.Image(
                    bitmap = processedBmp.asImageBitmap(),
                    contentDescription = "Processed photo selection",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = !showProcessed,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(model = originalUri),
                    contentDescription = "Original photo selection",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
