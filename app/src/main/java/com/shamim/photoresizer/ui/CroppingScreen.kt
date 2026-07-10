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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shamim.photoresizer.ViewModel
import com.shamim.photoresizer.ui.theme.LocalSeasonalEffectController

enum class GridOption(
    val label: String,
) {
    NONE("None"),
    RULE_OF_THIRDS("3x3"),
    GOLDEN_RATIO("Golden"),
    FOUR_BY_FOUR("4x4"),
}

/**
 * Renders the interactive Cropping Screen.
 * Provides multi-touch gestures (pinch-to-zoom, pan) to crop and align the original bitmap
 * with custom rules of thirds or golden ratio guides, and confirms photo scaling coordinates.
 *
 * @param viewModel State management and interaction controller.
 * @param onSettingsClick Callback triggered to open the Settings screen.
 * @param modifier Custom visual modifiers applied to the parent scaffold layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CroppingScreen(
    viewModel: ViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val seasonalController = LocalSeasonalEffectController.current
    DisposableEffect(Unit) {
        seasonalController.value = false
        onDispose {
            seasonalController.value = true
        }
    }

    val bitmap = viewModel.originalBitmap
    val context = LocalContext.current

    if (bitmap == null) return

    val targetWVal = viewModel.targetWidthInput.toIntOrNull() ?: bitmap.width
    val targetHVal = viewModel.targetHeightInput.toIntOrNull() ?: bitmap.height

    // Calculate crop selection target aspects
    val targetRatio = targetWVal.toFloat() / targetHVal.toFloat()

    // Tracking panning/zoom scales
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var activeGridOption by remember { mutableStateOf(GridOption.RULE_OF_THIRDS) }
    var showGridOptions by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.cancelCropping() },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                title = {
                    Text(
                        text = "Crop & Zoom",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("settings_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.5f)),
            )
        },
        containerColor =
            Color.Black, // Since cropping is dark-room style, black containerColor is best, or
        // MaterialTheme.colorScheme.background.
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val toolsHeightPx = with(LocalDensity.current) { 240.dp.toPx() }
                val containerWidth = constraints.maxWidth
                val containerHeight = constraints.maxHeight - toolsHeightPx

                // Base display image fitting details
                val imageAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
                val containerAspect = containerWidth.toFloat() / containerHeight.toFloat()

                val baseWidth: Float
                val baseHeight: Float
                if (imageAspect > containerAspect) {
                    baseWidth = containerWidth.toFloat()
                    baseHeight = containerWidth.toFloat() / imageAspect
                } else {
                    baseHeight = containerHeight.toFloat()
                    baseWidth = containerHeight.toFloat() * imageAspect
                }

                val x1 = (containerWidth - baseWidth) / 2
                val y1 = (containerHeight - baseHeight) / 2

                // Crop frame boundaries should reside strictly inside the fitted visual bounds of the image
                val maxCropW = baseWidth * 0.98f
                val maxCropH = baseHeight * 0.98f

                val finalCropW: Float
                val finalCropH: Float
                if (targetRatio > (maxCropW / maxCropH)) {
                    finalCropW = maxCropW
                    finalCropH = maxCropW / targetRatio
                } else {
                    finalCropH = maxCropH
                    finalCropW = maxCropH * targetRatio
                }

                val cropX1 = (containerWidth - finalCropW) / 2
                val cropY1 = (containerHeight - finalCropH) / 2
                val cropX2 = cropX1 + finalCropW
                val cropY2 = cropY1 + finalCropH

                val minScaleW = finalCropW / baseWidth
                val minScaleH = finalCropH / baseHeight
                val minScaleLimit = maxOf(minScaleW, minScaleH)

                val cX = containerWidth / 2f
                val cY = containerHeight / 2f

                // Clamp functions for scale and offset
                fun clampScale(s: Float): Float {
                    val minLimit = minScaleLimit
                    val maxLimit = maxOf(minLimit, 10.0f)
                    return s.coerceIn(minLimit, maxLimit)
                }

                fun clampOffsetX(
                    x: Float,
                    s: Float,
                ): Float {
                    val hScaledWidth = (baseWidth / 2f) * s
                    val minOffsetX = cropX2 - cX - hScaledWidth
                    val maxOffsetX = cropX1 - cX + hScaledWidth
                    val finalMin = minOf(minOffsetX, maxOffsetX)
                    val finalMax = maxOf(minOffsetX, maxOffsetX)
                    return x.coerceIn(finalMin, finalMax)
                }

                fun clampOffsetY(
                    y: Float,
                    s: Float,
                ): Float {
                    val hScaledHeight = (baseHeight / 2f) * s
                    val minOffsetY = cropY2 - cY - hScaledHeight
                    val maxOffsetY = cropY1 - cY + hScaledHeight
                    val finalMin = minOf(minOffsetY, maxOffsetY)
                    val finalMax = maxOf(minOffsetY, maxOffsetY)
                    return y.coerceIn(finalMin, finalMax)
                }

                // Keep values in bound automatically when key elements change
                val currentScale = clampScale(scale)
                val currentOffsetX = clampOffsetX(offsetX, currentScale)
                val currentOffsetY = clampOffsetY(offsetY, currentScale)

                LaunchedEffect(minScaleLimit, baseWidth, baseHeight) {
                    scale = clampScale(scale)
                    offsetX = clampOffsetX(offsetX, scale)
                    offsetY = clampOffsetY(offsetY, scale)
                }

                // Wrap Image Box and Canvas inside a Box of exact size matching containerWidth and
                // containerHeight
                Box(
                    modifier =
                        Modifier.size(
                            width = with(LocalDensity.current) { containerWidth.toDp() },
                            height = with(LocalDensity.current) { containerHeight.toDp() },
                        ),
                ) {
                    // Image background panning and zoom layers
                    Box(
                        modifier =
                            Modifier.fillMaxSize().pointerInput(minScaleLimit, baseWidth, baseHeight) {
                                detectTransformGestures { centroid, pan, zoom, _ ->
                                    val targetScale = clampScale(scale * zoom)
                                    val actualZoom = if (scale > 0f) targetScale / scale else 1f

                                    val centroidX = centroid.x
                                    val centroidY = centroid.y

                                    // Centroid-aware zoom and translation
                                    val newRawX =
                                        (offsetX - (centroidX - cX)) * actualZoom + (centroidX - cX) + pan.x
                                    val newRawY =
                                        (offsetY - (centroidY - cY)) * actualZoom + (centroidY - cY) + pan.y

                                    scale = targetScale
                                    offsetX = clampOffsetX(newRawX, targetScale)
                                    offsetY = clampOffsetY(newRawY, targetScale)
                                }
                            },
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Image being cropped",
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = currentScale,
                                        scaleY = currentScale,
                                        translationX = currentOffsetX,
                                        translationY = currentOffsetY,
                                    ),
                            contentScale = ContentScale.Fit,
                        )
                    }

                    // Custom drawn opaque mask revealing centered transparent portal of matched aspect
                    Canvas(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
                    ) {
                        // Draw dimmed outer layer
                        drawRect(color = Color.Black.copy(alpha = 0.7f))

                        // Punch viewport hole matching chosen aspect ratio
                        drawRect(
                            color = Color.Transparent,
                            topLeft = Offset(cropX1, cropY1),
                            size = Size(finalCropW, finalCropH),
                            blendMode = BlendMode.Clear,
                        )

                        // Dynamic border overlay
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(cropX1, cropY1),
                            size = Size(finalCropW, finalCropH),
                            style = Stroke(width = 3.5.dp.toPx()),
                        )

                        // Grid lines overlay
                        val gridColor = Color.White.copy(alpha = 0.5f)
                        val gridStroke = 3.0.dp.toPx()

                        when (activeGridOption) {
                            GridOption.NONE -> {}

                            GridOption.RULE_OF_THIRDS -> {
                                // Vertical lines
                                drawLine(
                                    gridColor,
                                    Offset(cropX1 + finalCropW / 3f, cropY1),
                                    Offset(cropX1 + finalCropW / 3f, cropY2),
                                    gridStroke,
                                )
                                drawLine(
                                    gridColor,
                                    Offset(cropX1 + 2 * finalCropW / 3f, cropY1),
                                    Offset(cropX1 + 2 * finalCropW / 3f, cropY2),
                                    gridStroke,
                                )
                                // Horizontal lines
                                drawLine(
                                    gridColor,
                                    Offset(cropX1, cropY1 + finalCropH / 3f),
                                    Offset(cropX2, cropY1 + finalCropH / 3f),
                                    gridStroke,
                                )
                                drawLine(
                                    gridColor,
                                    Offset(cropX1, cropY1 + 2 * finalCropH / 3f),
                                    Offset(cropX2, cropY1 + 2 * finalCropH / 3f),
                                    gridStroke,
                                )
                            }

                            GridOption.GOLDEN_RATIO -> {
                                // Vertical lines (at 0.382 and 0.618)
                                drawLine(
                                    gridColor,
                                    Offset(cropX1 + finalCropW * 0.382f, cropY1),
                                    Offset(cropX1 + finalCropW * 0.382f, cropY2),
                                    gridStroke,
                                )
                                drawLine(
                                    gridColor,
                                    Offset(cropX1 + finalCropW * 0.618f, cropY1),
                                    Offset(cropX1 + finalCropW * 0.618f, cropY2),
                                    gridStroke,
                                )
                                // Horizontal lines
                                drawLine(
                                    gridColor,
                                    Offset(cropX1, cropY1 + finalCropH * 0.382f),
                                    Offset(cropX2, cropY1 + finalCropH * 0.382f),
                                    gridStroke,
                                )
                                drawLine(
                                    gridColor,
                                    Offset(cropX1, cropY1 + finalCropH * 0.618f),
                                    Offset(cropX2, cropY1 + finalCropH * 0.618f),
                                    gridStroke,
                                )
                            }

                            GridOption.FOUR_BY_FOUR -> {
                                // Vertical lines
                                drawLine(
                                    gridColor,
                                    Offset(cropX1 + finalCropW * 0.25f, cropY1),
                                    Offset(cropX1 + finalCropW * 0.25f, cropY2),
                                    gridStroke,
                                )
                                drawLine(
                                    gridColor,
                                    Offset(cropX1 + finalCropW * 0.50f, cropY1),
                                    Offset(cropX1 + finalCropW * 0.50f, cropY2),
                                    gridStroke,
                                )
                                drawLine(
                                    gridColor,
                                    Offset(cropX1 + finalCropW * 0.75f, cropY1),
                                    Offset(cropX1 + finalCropW * 0.75f, cropY2),
                                    gridStroke,
                                )
                                // Horizontal lines
                                drawLine(
                                    gridColor,
                                    Offset(cropX1, cropY1 + finalCropH * 0.25f),
                                    Offset(cropX2, cropY1 + finalCropH * 0.25f),
                                    gridStroke,
                                )
                                drawLine(
                                    gridColor,
                                    Offset(cropX1, cropY1 + finalCropH * 0.50f),
                                    Offset(cropX2, cropY1 + finalCropH * 0.50f),
                                    gridStroke,
                                )
                                drawLine(
                                    gridColor,
                                    Offset(cropX1, cropY1 + finalCropH * 0.75f),
                                    Offset(cropX2, cropY1 + finalCropH * 0.75f),
                                    gridStroke,
                                )
                            }
                        }

                        // Crop boundary thicker indicators
                        val thick = 4.dp.toPx()
                        val len = 20.dp.toPx()

                        // Top-left corner
                        drawLine(
                            Color.White,
                            Offset(cropX1 - thick / 2, cropY1),
                            Offset(cropX1 + len, cropY1),
                            thick,
                        )
                        drawLine(
                            Color.White,
                            Offset(cropX1, cropY1 - thick / 2),
                            Offset(cropX1, cropY1 + len),
                            thick,
                        )

                        // Top-right corner
                        drawLine(
                            Color.White,
                            Offset(cropX2 + thick / 2, cropY1),
                            Offset(cropX2 - len, cropY1),
                            thick,
                        )
                        drawLine(
                            Color.White,
                            Offset(cropX2, cropY1 - thick / 2),
                            Offset(cropX2, cropY1 + len),
                            thick,
                        )

                        // Bottom-left corner
                        drawLine(
                            Color.White,
                            Offset(cropX1 - thick / 2, cropY2),
                            Offset(cropX1 + len, cropY2),
                            thick,
                        )
                        drawLine(
                            Color.White,
                            Offset(cropX1, cropY2 + thick / 2),
                            Offset(cropX1, cropY2 - len),
                            thick,
                        )

                        // Bottom-right corner
                        drawLine(
                            Color.White,
                            Offset(cropX2 + thick / 2, cropY2),
                            Offset(cropX2 - len, cropY2),
                            thick,
                        )
                        drawLine(
                            Color.White,
                            Offset(cropX2, cropY2 + thick / 2),
                            Offset(cropX2, cropY2 - len),
                            thick,
                        )
                    }
                }

                // Custom Overlay Tools: Zoom, Rotation, Cancels, Checks buttons
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp),
                ) {
                    // Grid selector row
                    if (showGridOptions) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Grid",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                            GridOption.values().forEach { option ->
                                val isSelected = activeGridOption == option
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    Color.White.copy(alpha = 0.12f)
                                                },
                                            ).clickable { activeGridOption = option }
                                            .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = option.label,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }

                    // Zoom slider controls to simplify emulator inputs
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Zoom",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(48.dp),
                        )
                        Slider(
                            value = currentScale,
                            onValueChange = { s ->
                                val nextScale = clampScale(s)
                                val ratio = if (scale > 0f) nextScale / scale else 1f
                                scale = nextScale
                                offsetX = clampOffsetX(offsetX * ratio, nextScale)
                                offsetY = clampOffsetY(offsetY * ratio, nextScale)
                            },
                            valueRange = minScaleLimit..maxOf(minScaleLimit, 10.0f),
                            modifier = Modifier.weight(1f),
                            colors =
                                SliderDefaults.colors(
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                ),
                        )
                    }

                    // Quick Rotate and confirmation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Cancel Button
                        IconButton(
                            onClick = { viewModel.cancelCropping() },
                            modifier =
                                Modifier.size(52.dp).background(Color.DarkGray.copy(alpha = 0.8f), CircleShape),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel crop", tint = Color.White)
                        }

                        // Rotations options right inside Crop screen
                        IconButton(
                            onClick = { viewModel.rotateOriginal() },
                            modifier =
                                Modifier
                                    .size(52.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                        CircleShape,
                                    ),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.RotateRight,
                                contentDescription = "Rotate 90 deg",
                                tint = Color.White,
                            )
                        }

                        // Grid options toggle button
                        IconButton(
                            onClick = { showGridOptions = !showGridOptions },
                            modifier =
                                Modifier
                                    .size(52.dp)
                                    .background(
                                        if (showGridOptions) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.DarkGray.copy(alpha = 0.8f)
                                        },
                                        CircleShape,
                                    ),
                        ) {
                            Icon(
                                Icons.Default.GridOn,
                                contentDescription = "Toggle Grid Options",
                                tint = Color.White,
                            )
                        }

                        // Confirm / Done Button
                        IconButton(
                            onClick = {
                                val srcX1Unscaled = (cropX1 - cX - currentOffsetX) / currentScale + cX
                                val srcY1Unscaled = (cropY1 - cY - currentOffsetY) / currentScale + cY
                                val srcX2Unscaled = (cropX2 - cX - currentOffsetX) / currentScale + cX
                                val srcY2Unscaled = (cropY2 - cY - currentOffsetY) / currentScale + cY

                                val relX1 = srcX1Unscaled - x1
                                val relY1 = srcY1Unscaled - y1
                                val relX2 = srcX2Unscaled - x1
                                val relY2 = srcY2Unscaled - y1

                                val pxLeft =
                                    ((relX1 / baseWidth) * bitmap.width).toInt().coerceIn(0, bitmap.width)
                                val pxTop =
                                    ((relY1 / baseHeight) * bitmap.height).toInt().coerceIn(0, bitmap.height)
                                val pxRight =
                                    ((relX2 / baseWidth) * bitmap.width).toInt().coerceIn(0, bitmap.width)
                                val pxBottom =
                                    ((relY2 / baseHeight) * bitmap.height).toInt().coerceIn(0, bitmap.height)

                                val finalW = (pxRight - pxLeft).coerceAtLeast(1)
                                val finalH = (pxBottom - pxTop).coerceAtLeast(1)

                                viewModel.confirmCropAndProcess(
                                    context = context,
                                    pixelLeft = pxLeft,
                                    pixelTop = pxTop,
                                    croppedW = finalW,
                                    croppedH = finalH,
                                )
                            },
                            modifier =
                                Modifier
                                    .size(60.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .testTag("confirm_crop_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Confirm cropping selections",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
