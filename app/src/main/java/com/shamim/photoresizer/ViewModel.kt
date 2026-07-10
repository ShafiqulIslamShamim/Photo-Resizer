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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ViewModel : androidx.lifecycle.ViewModel() {
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Welcome)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    var originalMetadata by mutableStateOf<ImageMetadata?>(null)
        private set

    var originalBitmap by mutableStateOf<Bitmap?>(null)
        private set

    // Targets inputs
    var targetWidthInput by mutableStateOf("")
    var targetHeightInput by mutableStateOf("")

    // Size limit / decreasement target
    var isAutoCompress by mutableStateOf(true)
    var sizeLimitInput by mutableStateOf("")
    var sizeLimitUnit by mutableStateOf("KB")

    // Aspect ratio selected preset
    var selectedPresetRatio by mutableStateOf("Original")

    // Processed result reference state
    var processedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var processedSizeBytes by mutableStateOf(0L)
        private set

    /**
     * Resets the ViewModel state to default values, clearing original/processed images,
     * resetting configuration targets, and navigating back to the Welcome screen.
     */
    fun reset() {
        originalMetadata = null
        originalBitmap = null
        processedBitmap = null
        processedSizeBytes = 0
        targetWidthInput = ""
        targetHeightInput = ""
        isAutoCompress = true
        sizeLimitInput = ""
        sizeLimitUnit = "KB"
        selectedPresetRatio = "Original"
        _screenState.value = ScreenState.Welcome
    }

    /**
     * Inspects, loads, and registers a newly selected image from a content URI.
     * Decodes exif metadata and rotates/adjusts the source bitmap on a background thread.
     *
     * @param context Application/Activity context.
     * @param uri The secure content Uri of the image to process.
     */
    fun setImage(
        context: Context,
        uri: Uri,
    ) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                val metadata = Util.loadMetadataFromUri(context, uri)
                if (metadata != null) {
                    originalMetadata = metadata
                    targetWidthInput = metadata.width.toString()
                    targetHeightInput = metadata.height.toString()

                    // Decode corrected bitmap inside background thread to keep UI interactive
                    val fullBitmap =
                        withContext(Dispatchers.IO) {
                            Util.loadCorrectedBitmap(context, uri)
                        }
                    originalBitmap = fullBitmap

                    _screenState.value = ScreenState.Configure
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to inspect image details", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loadingState.value = false
            }
        }
    }

    /**
     * Selects an aspect ratio preset ("Original", "1:1", "4:3", "16:9") and updates
     * the target width/height inputs based on the original photo size.
     *
     * @param ratioName The name of the aspect ratio preset.
     */
    fun selectPreset(ratioName: String) {
        selectedPresetRatio = ratioName
        val meta = originalMetadata ?: return
        val currentW = meta.width
        val currentH = meta.height
        when (ratioName) {
            "Original" -> {
                targetWidthInput = currentW.toString()
                targetHeightInput = currentH.toString()
            }

            "1:1" -> {
                val size = if (currentW < currentH) currentW else currentH
                targetWidthInput = size.toString()
                targetHeightInput = size.toString()
            }

            "4:3" -> {
                val targetH = (currentW * 3) / 4
                targetWidthInput = currentW.toString()
                targetHeightInput = targetH.toString()
            }

            "16:9" -> {
                val targetH = (currentW * 9) / 16
                targetWidthInput = currentW.toString()
                targetHeightInput = targetH.toString()
            }
        }
    }

    /**
     * Navigates the application UI to the crop editor screen.
     */
    fun openCropping() {
        _screenState.value = ScreenState.Cropping
    }

    /**
     * Cancels the active crop editing session and returns to the configuration screen.
     */
    fun cancelCropping() {
        _screenState.value = ScreenState.Configure
    }

    /**
     * Rotates the original bitmap by 90 degrees clockwise in a background worker thread.
     * Also updates internal dimensions, metadata, and re-evaluates the active preset ratio.
     */
    fun rotateOriginal() {
        val bitmap = originalBitmap ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _loadingState.value = true
            val matrix = Matrix().apply { postRotate(90f) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            withContext(Dispatchers.Main) {
                originalBitmap = rotated
                val oldMeta = originalMetadata
                if (oldMeta != null) {
                    val newMeta =
                        ImageMetadata(
                            width = rotated.width,
                            height = rotated.height,
                            sizeBytes = oldMeta.sizeBytes,
                            uri = oldMeta.uri,
                        )
                    originalMetadata = newMeta

                    if (selectedPresetRatio == "Original") {
                        targetWidthInput = rotated.width.toString()
                        targetHeightInput = rotated.height.toString()
                    } else {
                        selectPreset(selectedPresetRatio)
                    }
                }
                _loadingState.value = false
            }
        }
    }

    /**
     * Crops the original bitmap according to physical rectangular coordinates, applies scaling,
     * compresses the output to stay strictly below target file sizes (if specified in custom mode),
     * and navigates to the result preview page.
     *
     * @param context Application/Activity context.
     * @param pixelLeft Left X pixel coordinate of the cropped area on the original image.
     * @param pixelTop Top Y pixel coordinate of the cropped area on the original image.
     * @param croppedW Cropped bounding width of the sub-image.
     * @param croppedH Cropped bounding height of the sub-image.
     */
    fun confirmCropAndProcess(
        context: Context,
        pixelLeft: Int,
        pixelTop: Int,
        croppedW: Int,
        croppedH: Int,
    ) {
        val bitmap = originalBitmap ?: return
        val finalW = targetWidthInput.toIntOrNull() ?: croppedW
        val finalH = targetHeightInput.toIntOrNull() ?: croppedH

        viewModelScope.launch {
            _loadingState.value = true
            try {
                val processed =
                    withContext(Dispatchers.IO) {
                        val safeLeft = pixelLeft.coerceIn(0, bitmap.width - 1)
                        val safeTop = pixelTop.coerceIn(0, bitmap.height - 1)
                        val safeW = croppedW.coerceAtMost(bitmap.width - safeLeft).coerceAtLeast(1)
                        val safeH = croppedH.coerceAtMost(bitmap.height - safeTop).coerceAtLeast(1)

                        val croppedBitmap = Bitmap.createBitmap(bitmap, safeLeft, safeTop, safeW, safeH)
                        val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, finalW, finalH, true)

                        val compressedBytes: ByteArray
                        val isCustomMode = !isAutoCompress && sizeLimitInput.isNotBlank()
                        val inputNum = sizeLimitInput.toIntOrNull()

                        if (!isCustomMode || inputNum == null || inputNum <= 0) {
                            // Auto mode or invalid target value: compress once with high-quality
                            val baos = ByteArrayOutputStream()
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                            compressedBytes = baos.toByteArray()
                        } else {
                            // Custom target size mode matching user's KB/MB constraint
                            val targetKb = if (sizeLimitUnit == "MB") inputNum * 1024 else inputNum
                            val targetBytes = targetKb * 1024

                            var quality = 95
                            var tempBytes = ByteArray(0)
                            var currentLength = 0

                            do {
                                val baos = ByteArrayOutputStream()
                                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                                tempBytes = baos.toByteArray()
                                currentLength = tempBytes.size
                                quality -= 8
                            } while (currentLength > targetBytes && quality >= 15)

                            if (currentLength > targetBytes) {
                                var scale = 0.9f
                                var secondaryBitmap = scaledBitmap
                                while (currentLength > targetBytes && scale > 0.2f) {
                                    val nw = (finalW * scale).toInt().coerceAtLeast(1)
                                    val nh = (finalH * scale).toInt().coerceAtLeast(1)
                                    secondaryBitmap = Bitmap.createScaledBitmap(scaledBitmap, nw, nh, true)
                                    val baos = ByteArrayOutputStream()
                                    secondaryBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                                    tempBytes = baos.toByteArray()
                                    currentLength = tempBytes.size
                                    scale -= 0.1f
                                }
                            } else if (currentLength < targetBytes) {
                                var scale = 1.1f
                                var lastValidBytes = tempBytes
                                var lastValidScale = 1.0f

                                while (scale <= 5.0f) {
                                    val nw = (finalW * scale).toInt().coerceAtLeast(1)
                                    val nh = (finalH * scale).toInt().coerceAtLeast(1)
                                    val upscaledBmp = Bitmap.createScaledBitmap(scaledBitmap, nw, nh, true)

                                    val baos = ByteArrayOutputStream()
                                    upscaledBmp.compress(Bitmap.CompressFormat.JPEG, 95, baos)
                                    val upscaledBytes = baos.toByteArray()

                                    if (upscaledBytes.size <= targetBytes) {
                                        lastValidBytes = upscaledBytes
                                        lastValidScale = scale
                                        scale += 0.2f
                                    } else {
                                        var fineQuality = 90
                                        var fineBytes = upscaledBytes
                                        while (fineBytes.size > targetBytes && fineQuality >= 40) {
                                            val fBaos = ByteArrayOutputStream()
                                            upscaledBmp.compress(Bitmap.CompressFormat.JPEG, fineQuality, fBaos)
                                            fineBytes = fBaos.toByteArray()
                                            fineQuality -= 5
                                        }
                                        if (fineBytes.size <= targetBytes) {
                                            lastValidBytes = fineBytes
                                            lastValidScale = scale
                                        }
                                        break
                                    }
                                }
                                tempBytes = lastValidBytes
                                currentLength = tempBytes.size
                            }
                            compressedBytes = tempBytes
                        }

                        val finalBitmap =
                            BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
                        ProcessedResult(finalBitmap, compressedBytes.size.toLong())
                    }

                processedBitmap = processed.bitmap
                processedSizeBytes = processed.sizeBytes
                _screenState.value = ScreenState.Preview
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast
                        .makeText(context, "Processing error: " + e.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            } finally {
                _loadingState.value = false
            }
        }
    }

    private data class ProcessedResult(
        val bitmap: Bitmap,
        val sizeBytes: Long,
    )
}
