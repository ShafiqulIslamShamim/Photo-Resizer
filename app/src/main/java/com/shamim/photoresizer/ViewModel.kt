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
    var selectedPresetSize by mutableStateOf("None")

    // Custom mm state properties for Custom preset size
    var customMmWidth by mutableStateOf("")
    var customMmHeight by mutableStateOf("")

    // Custom ratio state properties for Custom aspect ratio preset
    var customRatioWidth by mutableStateOf("")
    var customRatioHeight by mutableStateOf("")

    var presetResolutionMode by mutableStateOf("Original")

    fun changePresetResolutionMode(mode: String) {
        presetResolutionMode = mode
        updatePresetSizeResolution()
    }

    fun updateCustomMmWidth(mmStr: String) {
        customMmWidth = mmStr.filter { it.isDigit() || it == '.' }
        updatePresetSizeResolution()
    }

    fun updateCustomMmHeight(mmStr: String) {
        customMmHeight = mmStr.filter { it.isDigit() || it == '.' }
        updatePresetSizeResolution()
    }

    fun updateCustomRatioWidth(ratioStr: String) {
        customRatioWidth = ratioStr.filter { it.isDigit() || it == '.' }
        recalculateCustomRatio()
    }

    fun updateCustomRatioHeight(ratioStr: String) {
        customRatioHeight = ratioStr.filter { it.isDigit() || it == '.' }
        recalculateCustomRatio()
    }

    private fun recalculateCustomRatio() {
        val rw = customRatioWidth.toDoubleOrNull()
        val rh = customRatioHeight.toDoubleOrNull()
        val meta = originalMetadata ?: return
        val currentW = meta.width
        val currentH = meta.height
        if (rw != null && rh != null && rw > 0.0 && rh > 0.0) {
            val targetH = Math.round(currentW * rh / rw).toInt()
            targetWidthInput = currentW.toString()
            targetHeightInput = targetH.toString()
        } else {
            targetWidthInput = currentW.toString()
            targetHeightInput = currentH.toString()
        }
    }

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
        selectedPresetSize = "None"
        presetResolutionMode = "Original"
        customMmWidth = ""
        customMmHeight = ""
        customRatioWidth = ""
        customRatioHeight = ""
        _screenState.value = ScreenState.Welcome
    }

    /**
     * Navigates back to the configure screen.
     */
    fun backToConfigure() {
        _screenState.value = ScreenState.Configure
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
        selectedPresetSize = "None"
        val meta = originalMetadata ?: return
        val currentW = meta.width
        val currentH = meta.height
        when (ratioName) {
            "Original" -> {
                targetWidthInput = currentW.toString()
                targetHeightInput = currentH.toString()
            }

            "Freeform" -> {
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

            "9:16" -> {
                val targetH = (currentW * 16) / 9
                targetWidthInput = currentW.toString()
                targetHeightInput = targetH.toString()
            }

            "2:3" -> {
                val targetH = (currentW * 3) / 2
                targetWidthInput = currentW.toString()
                targetHeightInput = targetH.toString()
            }

            "3:2" -> {
                val targetH = (currentW * 2) / 3
                targetWidthInput = currentW.toString()
                targetHeightInput = targetH.toString()
            }

            "21:9" -> {
                val targetH = (currentW * 9) / 21
                targetWidthInput = currentW.toString()
                targetHeightInput = targetH.toString()
            }

            "Custom ratio" -> {
                recalculateCustomRatio()
            }
        }
    }

    /**
     * Selects a standard document/photo preset size and updates
     * the target width/height inputs in pixels.
     *
     * @param presetName The name of the standard preset size.
     */
    fun selectPresetSize(presetName: String) {
        selectedPresetSize = presetName
        selectedPresetRatio = "Custom"
        if (presetName == "None") {
            return
        }
        updatePresetSizeResolution()
    }

    fun updatePresetSizeResolution() {
        val presetName = selectedPresetSize
        if (presetName == "None") return
        val meta = originalMetadata ?: return
        val origW = meta.width
        val origH = meta.height

        val (presetW, presetH) = when (presetName) {
            "25 × 30 mm" -> Pair(295, 354)
            "25 × 35 mm" -> Pair(295, 413)
            "30 × 40 mm" -> Pair(354, 472)
            "30 × 45 mm" -> Pair(354, 531)
            "33 × 48 mm" -> Pair(390, 567)
            "35 × 45 mm" -> Pair(413, 531)
            "40 × 50 mm" -> Pair(472, 591)
            "45 × 45 mm" -> Pair(531, 531)
            "50 × 50 mm" -> Pair(591, 591)
            "51 × 51 mm" -> Pair(602, 602)
            "Custom preset" -> {
                val mmW = customMmWidth.toDoubleOrNull() ?: 1.0
                val mmH = customMmHeight.toDoubleOrNull() ?: 1.0
                val pxW = Math.round(mmW * 300.0 / 25.4).toInt()
                val pxH = Math.round(mmH * 300.0 / 25.4).toInt()
                Pair(pxW, pxH)
            }
            else -> Pair(origW, origH)
        }

        if (presetResolutionMode == "Maximum") {
            val presetRatio = presetW.toDouble() / presetH.toDouble()
            val origRatio = origW.toDouble() / origH.toDouble()
            if (presetRatio > origRatio) {
                val w = origW
                val h = Math.round(origW / presetRatio).toInt()
                targetWidthInput = w.toString()
                targetHeightInput = h.toString()
            } else {
                val h = origH
                val w = Math.round(origH * presetRatio).toInt()
                targetWidthInput = w.toString()
                targetHeightInput = h.toString()
            }
        } else {
            targetWidthInput = presetW.toString()
            targetHeightInput = presetH.toString()
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
                            originalName = oldMeta.originalName,
                            format = oldMeta.format,
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
        fineRotation: Float = 0f,
        perspectiveX: Float = 0f,
        perspectiveY: Float = 0f,
        perspectivePoints: FloatArray? = null,
    ) {
        val bitmap = originalBitmap ?: return

        viewModelScope.launch {
            _loadingState.value = true
            try {
                val processed =
                    withContext(Dispatchers.IO) {
                        val transformedBitmap = if (perspectivePoints != null) {
                            Util.apply4PointPerspectiveWarp(bitmap, perspectivePoints)
                        } else {
                            val safeLeft = pixelLeft.coerceIn(0, bitmap.width - 1)
                            val safeTop = pixelTop.coerceIn(0, bitmap.height - 1)
                            val safeW = croppedW.coerceAtMost(bitmap.width - safeLeft).coerceAtLeast(1)
                            val safeH = croppedH.coerceAtMost(bitmap.height - safeTop).coerceAtLeast(1)

                            val croppedBitmap = Bitmap.createBitmap(bitmap, safeLeft, safeTop, safeW, safeH)

                            val rotationOrPersp = if (fineRotation != 0f || perspectiveX != 0f || perspectiveY != 0f) {
                                Util.applyPerspectiveAndRotation(croppedBitmap, fineRotation, perspectiveX, perspectiveY)
                            } else {
                                croppedBitmap
                            }
                            if (rotationOrPersp != croppedBitmap) {
                                croppedBitmap.recycle()
                            }
                            rotationOrPersp
                        }

                        val targetW = targetWidthInput.toIntOrNull() ?: transformedBitmap.width
                        val targetH = targetHeightInput.toIntOrNull() ?: transformedBitmap.height

                        val scaledBitmap = Bitmap.createScaledBitmap(transformedBitmap, targetW, targetH, true)

                        if (transformedBitmap != scaledBitmap) {
                            transformedBitmap.recycle()
                        }

                        val isCustomMode = !isAutoCompress && sizeLimitInput.isNotBlank()
                        val inputNum = sizeLimitInput.toIntOrNull()
                        val formatStr = originalMetadata?.format ?: "JPG"
                        val sizeLimitKb = if (isCustomMode && inputNum != null && inputNum > 0) {
                            if (sizeLimitUnit == "MB") inputNum * 1024 else inputNum
                        } else {
                            null
                        }

                        val compressedBytes = Util.compressBitmapToBytes(
                            bitmap = scaledBitmap,
                            quality = 100,
                            formatStr = formatStr,
                            sizeLimitKb = sizeLimitKb
                        )

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
