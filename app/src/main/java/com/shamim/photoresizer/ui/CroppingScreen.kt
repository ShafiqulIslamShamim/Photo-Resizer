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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.clipPath
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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.foundation.gestures.awaitFirstDown
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
    val densityVal = LocalDensity.current.density

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

    var fineRotationValue by remember { mutableStateOf(0f) } // -100 to 100
    var activeSliderMode by remember { mutableStateOf("Rotation") } // "Rotation", "Zoom", "Perspective 4D", "Grid"

    var ptTopLeft by remember { mutableStateOf(Offset(0f, 0f)) }
    var ptTopRight by remember { mutableStateOf(Offset(1f, 0f)) }
    var ptBottomRight by remember { mutableStateOf(Offset(1f, 1f)) }
    var ptBottomLeft by remember { mutableStateOf(Offset(0f, 1f)) }
    var isHoldingOriginal by remember { mutableStateOf(false) }
    var activeDragHandle by remember { mutableStateOf<Int?>(null) }

    var freeformLeft by remember { mutableStateOf(0.1f) }
    var freeformTop by remember { mutableStateOf(0.1f) }
    var freeformRight by remember { mutableStateOf(0.9f) }
    var freeformBottom by remember { mutableStateOf(0.9f) }

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
            val toolsHeightDp = 270.dp
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val toolsHeightPx = with(LocalDensity.current) { toolsHeightDp.toPx() }
                val containerWidth = constraints.maxWidth
                val containerHeight = constraints.maxHeight - toolsHeightPx.toInt()

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

                val isFreeform = viewModel.selectedPresetRatio == "Freeform"

                val standardCropW: Float
                val standardCropH: Float
                if (targetRatio > (maxCropW / maxCropH)) {
                    standardCropW = maxCropW
                    standardCropH = maxCropW / targetRatio
                } else {
                    standardCropH = maxCropH
                    standardCropW = maxCropH * targetRatio
                }

                val cropX1 = if (isFreeform) {
                    x1 + freeformLeft * baseWidth
                } else {
                    (containerWidth - standardCropW) / 2
                }

                val cropY1 = if (isFreeform) {
                    y1 + freeformTop * baseHeight
                } else {
                    (containerHeight - standardCropH) / 2
                }

                val cropX2 = if (isFreeform) {
                    x1 + freeformRight * baseWidth
                } else {
                    cropX1 + standardCropW
                }

                val cropY2 = if (isFreeform) {
                    y1 + freeformBottom * baseHeight
                } else {
                    cropY1 + standardCropH
                }

                val finalCropW = if (isFreeform) {
                    cropX2 - cropX1
                } else {
                    standardCropW
                }

                val finalCropH = if (isFreeform) {
                    cropY2 - cropY1
                } else {
                    standardCropH
                }

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

                // Layout image viewport and controls sequentially using Column to prevent any layout layover/overlap!
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. Viewport Box (takes all remaining height)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Image background panning and zoom layers
                        Box(
                            modifier =
                                Modifier.fillMaxSize().pointerInput(minScaleLimit, baseWidth, baseHeight) {
                                    if (activeSliderMode != "Perspective 4D") {
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
                                    }
                                }
                                .pointerInput(activeSliderMode) {
                                    if (activeSliderMode == "Perspective 4D") {
                                        detectTapGestures(
                                            onPress = {
                                                isHoldingOriginal = true
                                                tryAwaitRelease()
                                                isHoldingOriginal = false
                                            }
                                        )
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
                                            scaleX = if (activeSliderMode == "Perspective 4D") 1f else currentScale,
                                            scaleY = if (activeSliderMode == "Perspective 4D") 1f else currentScale,
                                            translationX = if (activeSliderMode == "Perspective 4D") 0f else currentOffsetX,
                                            translationY = if (activeSliderMode == "Perspective 4D") 0f else currentOffsetY,
                                            rotationZ = if (activeSliderMode == "Perspective 4D") 0f else (fineRotationValue / 100f) * 45f,
                                            rotationX = 0f,
                                            rotationY = 0f,
                                            cameraDistance = 12f * densityVal,
                                        ),
                                contentScale = ContentScale.Fit,
                            )
                        }

                        if (activeSliderMode == "Perspective 4D") {
                            if (!isHoldingOriginal) {
                                // Draw quadrilateral path and dimmed outer background
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val tlScreenX = x1 + ptTopLeft.x * baseWidth
                                    val tlScreenY = y1 + ptTopLeft.y * baseHeight

                                    val trScreenX = x1 + ptTopRight.x * baseWidth
                                    val trScreenY = y1 + ptTopRight.y * baseHeight

                                    val brScreenX = x1 + ptBottomRight.x * baseWidth
                                    val brScreenY = y1 + ptBottomRight.y * baseHeight

                                    val blScreenX = x1 + ptBottomLeft.x * baseWidth
                                    val blScreenY = y1 + ptBottomLeft.y * baseHeight

                                    val path = Path().apply {
                                        moveTo(tlScreenX, tlScreenY)
                                        lineTo(trScreenX, trScreenY)
                                        lineTo(brScreenX, brScreenY)
                                        lineTo(blScreenX, blScreenY)
                                        close()
                                    }

                                    // Draw dimmed background outside the quadrilateral
                                    clipPath(path, clipOp = ClipOp.Difference) {
                                        drawRect(color = Color.Black.copy(alpha = 0.65f))
                                    }

                                    // Draw white border lines
                                    drawPath(
                                        path = path,
                                        color = Color.White,
                                        style = Stroke(width = 2.dp.toPx())
                                    )

                                    // Draw the 4 corner handles
                                    val handleRadius = 11.dp.toPx()
                                    val shadowRadius = handleRadius + 2.5.dp.toPx()

                                    // TL
                                    drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = shadowRadius, center = Offset(tlScreenX, tlScreenY))
                                    drawCircle(color = Color.White, radius = handleRadius, center = Offset(tlScreenX, tlScreenY))

                                    // TR
                                    drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = shadowRadius, center = Offset(trScreenX, trScreenY))
                                    drawCircle(color = Color.White, radius = handleRadius, center = Offset(trScreenX, trScreenY))

                                    // BR
                                    drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = shadowRadius, center = Offset(brScreenX, brScreenY))
                                    drawCircle(color = Color.White, radius = handleRadius, center = Offset(brScreenX, brScreenY))

                                    // BL
                                    drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = shadowRadius, center = Offset(blScreenX, blScreenY))
                                    drawCircle(color = Color.White, radius = handleRadius, center = Offset(blScreenX, blScreenY))
                                }

                                // Interactive layer for handles dragging
                                val currentPtTopLeft by rememberUpdatedState(ptTopLeft)
                                val currentPtTopRight by rememberUpdatedState(ptTopRight)
                                val currentPtBottomRight by rememberUpdatedState(ptBottomRight)
                                val currentPtBottomLeft by rememberUpdatedState(ptBottomLeft)
                                val currentX1 by rememberUpdatedState(x1)
                                val currentY1 by rememberUpdatedState(y1)
                                val currentBaseWidth by rememberUpdatedState(baseWidth)
                                val currentBaseHeight by rememberUpdatedState(baseHeight)

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { startOffset ->
                                                    val touchX = startOffset.x
                                                    val touchY = startOffset.y

                                                    val dTL = Math.hypot((touchX - (currentX1 + currentPtTopLeft.x * currentBaseWidth)).toDouble(), (touchY - (currentY1 + currentPtTopLeft.y * currentBaseHeight)).toDouble())
                                                    val dTR = Math.hypot((touchX - (currentX1 + currentPtTopRight.x * currentBaseWidth)).toDouble(), (touchY - (currentY1 + currentPtTopRight.y * currentBaseHeight)).toDouble())
                                                    val dBR = Math.hypot((touchX - (currentX1 + currentPtBottomRight.x * currentBaseWidth)).toDouble(), (touchY - (currentY1 + currentPtBottomRight.y * currentBaseHeight)).toDouble())
                                                    val dBL = Math.hypot((touchX - (currentX1 + currentPtBottomLeft.x * currentBaseWidth)).toDouble(), (touchY - (currentY1 + currentPtBottomLeft.y * currentBaseHeight)).toDouble())

                                                    val limit = 48.dp.toPx()
                                                    val minDistance = minOf(dTL, dTR, dBR, dBL)
                                                    activeDragHandle = if (minDistance <= limit) {
                                                        when (minDistance) {
                                                            dTL -> 0
                                                            dTR -> 1
                                                            dBR -> 2
                                                            else -> 3
                                                        }
                                                    } else {
                                                        null
                                                    }
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    val active = activeDragHandle ?: return@detectDragGestures
                                                    val newX = change.position.x
                                                    val newY = change.position.y

                                                    val normX = ((newX - currentX1) / currentBaseWidth).coerceIn(0f, 1f)
                                                    val normY = ((newY - currentY1) / currentBaseHeight).coerceIn(0f, 1f)

                                                    when (active) {
                                                        0 -> ptTopLeft = Offset(normX, normY)
                                                        1 -> ptTopRight = Offset(normX, normY)
                                                        2 -> ptBottomRight = Offset(normX, normY)
                                                        3 -> ptBottomLeft = Offset(normX, normY)
                                                    }
                                                },
                                                onDragEnd = {
                                                    activeDragHandle = null
                                                }
                                            )
                                        }
                                )
                            }
                        } else {
                            if (isFreeform) {
                                val currentX1 by rememberUpdatedState(x1)
                                val currentY1 by rememberUpdatedState(y1)
                                val currentBaseWidth by rememberUpdatedState(baseWidth)
                                val currentBaseHeight by rememberUpdatedState(baseHeight)

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val down = awaitFirstDown(requireUnconsumed = false)
                                                    val touchX = down.position.x
                                                    val touchY = down.position.y

                                                    val dTL = Math.hypot((touchX - cropX1).toDouble(), (touchY - cropY1).toDouble())
                                                    val dTR = Math.hypot((touchX - cropX2).toDouble(), (touchY - cropY1).toDouble())
                                                    val dBR = Math.hypot((touchX - cropX2).toDouble(), (touchY - cropY2).toDouble())
                                                    val dBL = Math.hypot((touchX - cropX1).toDouble(), (touchY - cropY2).toDouble())

                                                    val limit = 48.dp.toPx()
                                                    val minDistance = minOf(dTL, dTR, dBR, dBL)
                                                    if (minDistance <= limit) {
                                                        val handle = when (minDistance) {
                                                            dTL -> 0
                                                            dTR -> 1
                                                            dBR -> 2
                                                            else -> 3
                                                        }
                                                        down.consume()
                                                        val dragId = down.id
                                                        while (true) {
                                                            val event = awaitPointerEvent()
                                                            val anyPressed = event.changes.any { it.pressed }
                                                            if (!anyPressed) break
                                                            val dragChange = event.changes.firstOrNull { it.id == dragId } ?: event.changes.first()
                                                            dragChange.consume()

                                                            val newX = dragChange.position.x
                                                            val newY = dragChange.position.y

                                                            val normX = ((newX - currentX1) / currentBaseWidth).coerceIn(0f, 1f)
                                                            val normY = ((newY - currentY1) / currentBaseHeight).coerceIn(0f, 1f)

                                                            when (handle) {
                                                                0 -> { // TL
                                                                    freeformLeft = normX.coerceAtMost(freeformRight - 0.1f)
                                                                    freeformTop = normY.coerceAtMost(freeformBottom - 0.1f)
                                                                }
                                                                1 -> { // TR
                                                                    freeformRight = normX.coerceAtLeast(freeformLeft + 0.1f)
                                                                    freeformTop = normY.coerceAtMost(freeformBottom - 0.1f)
                                                                }
                                                                2 -> { // BR
                                                                    freeformRight = normX.coerceAtLeast(freeformLeft + 0.1f)
                                                                    freeformBottom = normY.coerceAtLeast(freeformTop + 0.1f)
                                                                }
                                                                3 -> { // BL
                                                                    freeformLeft = normX.coerceAtMost(freeformRight - 0.1f)
                                                                    freeformBottom = normY.coerceAtLeast(freeformTop + 0.1f)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
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

                                // Under Freeform mode, draw handle circles at the four corners
                                if (isFreeform) {
                                    val handleColor = Color(0xFFE3B505) // Gold/Yellow accent
                                    val handleRadius = 11.dp.toPx()
                                    
                                    // TL
                                    drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = handleRadius + 2.5.dp.toPx(), center = Offset(cropX1, cropY1))
                                    drawCircle(color = handleColor, radius = handleRadius, center = Offset(cropX1, cropY1))
                                    drawCircle(color = Color.White, radius = handleRadius - 3.5.dp.toPx(), center = Offset(cropX1, cropY1))
                                    
                                    // TR
                                    drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = handleRadius + 2.5.dp.toPx(), center = Offset(cropX2, cropY1))
                                    drawCircle(color = handleColor, radius = handleRadius, center = Offset(cropX2, cropY1))
                                    drawCircle(color = Color.White, radius = handleRadius - 3.5.dp.toPx(), center = Offset(cropX2, cropY1))
                                    
                                    // BR
                                    drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = handleRadius + 2.5.dp.toPx(), center = Offset(cropX2, cropY2))
                                    drawCircle(color = handleColor, radius = handleRadius, center = Offset(cropX2, cropY2))
                                    drawCircle(color = Color.White, radius = handleRadius - 3.5.dp.toPx(), center = Offset(cropX2, cropY2))
                                    
                                    // BL
                                    drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = handleRadius + 2.5.dp.toPx(), center = Offset(cropX1, cropY2))
                                    drawCircle(color = handleColor, radius = handleRadius, center = Offset(cropX1, cropY2))
                                    drawCircle(color = Color.White, radius = handleRadius - 3.5.dp.toPx(), center = Offset(cropX1, cropY2))
                                }

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
                    } // End of Viewport Box

                    // 2. Controls Area Box (stays at the bottom, perfectly segregated)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(toolsHeightDp)
                            .background(Color.Black)
                            .padding(bottom = 12.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (activeSliderMode == "Perspective 4D") {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // "Tap and hold to see original" Capsule
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onPress = {
                                                    isHoldingOriginal = true
                                                    tryAwaitRelease()
                                                    isHoldingOriginal = false
                                                }
                                            )
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Tap and hold to see original",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Bottom actions with Reset
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Cancel button (X)
                                    IconButton(
                                        onClick = { activeSliderMode = "Rotation" },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Exit perspective mode",
                                            tint = Color.White
                                        )
                                    }

                                    // Reset button
                                    Text(
                                        text = "Reset",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                ptTopLeft = Offset(0f, 0f)
                                                ptTopRight = Offset(1f, 0f)
                                                ptBottomRight = Offset(1f, 1f)
                                                ptBottomLeft = Offset(0f, 1f)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    )

                                    // Confirm Button (Checkmark)
                                    IconButton(
                                        onClick = {
                                            val points = floatArrayOf(
                                                ptTopLeft.x, ptTopLeft.y,
                                                ptTopRight.x, ptTopRight.y,
                                                ptBottomRight.x, ptBottomRight.y,
                                                ptBottomLeft.x, ptBottomLeft.y
                                            )
                                            viewModel.confirmCropAndProcess(
                                                context = context,
                                                pixelLeft = 0,
                                                pixelTop = 0,
                                                croppedW = bitmap.width,
                                                croppedH = bitmap.height,
                                                perspectivePoints = points
                                            )
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            .testTag("confirm_crop_button"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Confirm perspective warp",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Only show the Grid Segment Selector if we are in Grid mode
                                if (activeSliderMode == "Grid") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color(0xFF212121))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GridOption.values().forEach { option ->
                                            val isSelected = activeGridOption == option
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .padding(horizontal = 4.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f)
                                                    )
                                                    .clickable { activeGridOption = option },
                                                contentAlignment = Alignment.Center
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

                                // Dynamic Slider depending on activeSliderMode
                                if (activeSliderMode != "Grid") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp)
                                    ) {
                                        when (activeSliderMode) {
                                            "Rotation" -> {
                                                RulerSlider(
                                                    value = fineRotationValue,
                                                    onValueChange = { fineRotationValue = it },
                                                    valueRange = -100f..100f,
                                                    isZoom = false
                                                )
                                            }
                                            "Zoom" -> {
                                                RulerSlider(
                                                    value = currentScale,
                                                    onValueChange = { s ->
                                                        val nextScale = clampScale(s)
                                                        val ratio = if (scale > 0f) nextScale / scale else 1f
                                                        scale = nextScale
                                                        offsetX = clampOffsetX(offsetX * ratio, nextScale)
                                                        offsetY = clampOffsetY(offsetY * ratio, nextScale)
                                                    },
                                                    valueRange = minScaleLimit..maxOf(minScaleLimit, 10.0f),
                                                    isZoom = true
                                                )
                                            }
                                        }
                                    }
                                }

                                // Mode Selection Tabs Row (Google Photos style, with text and small background indication)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rotation Mode Tab
                                    val isRot = activeSliderMode == "Rotation"
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { activeSliderMode = "Rotation" }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.RotateRight,
                                            contentDescription = "Rotation Slider",
                                            tint = if (isRot) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            "Rotate",
                                            color = if (isRot) MaterialTheme.colorScheme.primary else Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = if (isRot) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    // Zoom Mode Tab
                                    val isZoom = activeSliderMode == "Zoom"
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { activeSliderMode = "Zoom" }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = "Zoom Slider",
                                            tint = if (isZoom) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            "Zoom",
                                            color = if (isZoom) MaterialTheme.colorScheme.primary else Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = if (isZoom) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    // 4D Perspective Mode Tab (NEW)
                                    val isPersp4D = activeSliderMode == "Perspective 4D"
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { activeSliderMode = "Perspective 4D" }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AspectRatio,
                                            contentDescription = "4D Perspective Crop",
                                            tint = if (isPersp4D) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            "Perspective",
                                            color = if (isPersp4D) MaterialTheme.colorScheme.primary else Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = if (isPersp4D) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    // Grid Options Selector Toggle Tab
                                    val isGrid = activeSliderMode == "Grid"
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { activeSliderMode = "Grid" }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GridOn,
                                            contentDescription = "Toggle Grid Options",
                                            tint = if (isGrid) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            "Grid",
                                            color = if (isGrid) MaterialTheme.colorScheme.primary else Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = if (isGrid) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    // 90 Deg Rotate Option Tab
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.rotateOriginal() }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.RotateRight,
                                            contentDescription = "Rotate 90 deg",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            "90°",
                                            color = Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }

                                // Bottom Action Bar (Cancel, "Framing", Done)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Cancel Button
                                    IconButton(
                                        onClick = { viewModel.cancelCropping() },
                                        modifier =
                                            Modifier.size(48.dp).background(Color.DarkGray.copy(alpha = 0.5f), CircleShape),
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancel crop", tint = Color.White)
                                    }

                                    // "Framing" Label Text
                                    Text(
                                        text = "Framing",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )

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
                                                fineRotation = (fineRotationValue / 100f) * 45f,
                                            )
                                        },
                                        modifier =
                                            Modifier
                                                .size(48.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                .testTag("confirm_crop_button"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Confirm cropping selections",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom horizontal Ruler Slider / Dial modeled after Google Photos editor.
 * Provides intuitive, tactile, scrollable ruler ticks with central gold/yellow pointer
 * and tap-to-reset capability.
 */
@Composable
fun RulerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    isZoom: Boolean = false
) {
    val density = LocalDensity.current.density
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentValRange by rememberUpdatedState(valueRange)
    val currentIsZoom by rememberUpdatedState(isZoom)
    val currentValue by rememberUpdatedState(value)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(29.dp))
            .background(Color(0xFF212121))
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val currentWidth by rememberUpdatedState(width)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val isZoomMode = currentIsZoom
                        val pxPerUnit = if (isZoomMode) {
                            currentWidth / 12f
                        } else {
                            2.5f
                        }
                        val delta = -(dragAmount.x / pxPerUnit)
                        val newValue = (currentValue + delta).coerceIn(currentValRange.start, currentValRange.endInclusive)
                        currentOnValueChange(newValue)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
            val centerVal = value
            val startVal = valueRange.start
            val endVal = valueRange.endInclusive
            
            val centerXPx = width / 2f
            
            if (isZoom) {
                val tickStep = 0.5f
                val pxPerUnit = width / 12f
                val minT = (startVal / tickStep).toInt()
                val maxT = (endVal / tickStep).toInt()
                for (t in minT..maxT) {
                    val v = t * tickStep
                    val x = centerXPx + (v - centerVal) * pxPerUnit
                    val isMajor = (t % 2 == 0)
                    val tickHeight = if (isMajor) 22.dp.toPx() else 14.dp.toPx()
                    val alpha = (1f - (Math.abs(x - centerXPx) / (width / 2f))).coerceIn(0f, 1f)
                    
                    if (x in 0f..width) {
                        drawLine(
                            color = Color.White.copy(alpha = alpha * 0.45f),
                            start = Offset(x, (height - tickHeight) / 2f),
                            end = Offset(x, (height + tickHeight) / 2f),
                            strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
                        )
                    }
                }
            } else {
                val pxPerUnit = 2.5f
                val visibleUnits = (width / 2f) / pxPerUnit
                val minV = (centerVal - visibleUnits).toInt().coerceAtLeast(startVal.toInt())
                val maxV = (centerVal + visibleUnits).toInt().coerceAtMost(endVal.toInt())
                for (v in minV..maxV) {
                    if (v % 2 != 0) continue
                    val x = centerXPx + (v - centerVal) * pxPerUnit
                    val isMajor = (v % 10 == 0)
                    val tickHeight = if (isMajor) 22.dp.toPx() else 14.dp.toPx()
                    val alpha = (1f - (Math.abs(x - centerXPx) / (width / 2f))).coerceIn(0f, 1f)
                    
                    if (x in 0f..width) {
                        drawLine(
                            color = Color.White.copy(alpha = alpha * 0.45f),
                            start = Offset(x, (height - tickHeight) / 2f),
                            end = Offset(x, (height + tickHeight) / 2f),
                            strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
                        )
                    }
                }
            }
            
            // Central indicator
            drawLine(
                color = Color(0xFFE3B505),
                start = Offset(centerXPx, (height - 34.dp.toPx()) / 2f),
                end = Offset(centerXPx, (height + 34.dp.toPx()) / 2f),
                strokeWidth = 2.5.dp.toPx()
            )
        }
        
        val displayText = if (isZoom) {
            String.format("%.1fx", value)
        } else {
            val intVal = value.toInt()
            if (intVal > 0) "+$intVal" else intVal.toString()
        }
        
        Text(
            text = displayText,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable {
                    // Tap to reset to neutral default value
                    onValueChange(if (isZoom) 1.0f else 0.0f)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        }
    }
}
