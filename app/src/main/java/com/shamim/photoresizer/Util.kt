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

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

object Util {
    /**
     * Formats binary byte sizes to a human-readable string representation (e.g., "1.52 MB" or "450.2 KB").
     *
     * @param bytes File size in bytes.
     * @return Human-readable formatted size string.
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val sizeKb = bytes / 1024f
        return if (sizeKb >= 1024f) {
            val sizeMb = sizeKb / 1024f
            val roundedMb = (sizeMb * 100).toInt() / 100f
            "$roundedMb MB"
        } else {
            val roundedKb = (sizeKb * 100).toInt() / 100f
            "$roundedKb KB"
        }
    }

    /**
     * Evaluates and formats the aspect ratio for given dimensions (e.g., "16:9" or "4:3")
     * based on their greatest common divisor.
     *
     * @param width Width dimension of the photo.
     * @param height Height dimension of the photo.
     * @return A string representing the aspect ratio, or "N/A" if input is invalid.
     */
    fun getAspectRatioText(
        width: Int,
        height: Int,
    ): String {
        if (width <= 0 || height <= 0) return "N/A"
        val gcdVal = findGcd(width, height)
        val wp = width / gcdVal
        val hp = height / gcdVal
        return "$wp:$hp"
    }

    /**
     * Calculates the Greatest Common Divisor (GCD) between two integers using the Euclidean algorithm.
     *
     * @param a The first integer.
     * @param b The second integer.
     * @return The greatest common divisor of a and b.
     */
    private fun findGcd(
        a: Int,
        b: Int,
    ): Int {
        var n1 = a
        var n2 = b
        while (n2 != 0) {
            val remainder = n1 % n2
            n1 = n2
            n2 = remainder
        }
        return n1
    }

    /**
     * Creates a temporary image file under cache directory for capturing photos from the camera
     * and returns its secure FileProvider content URI hook.
     *
     * @param context Context used to resolve local cache directories and the FileProvider authority.
     * @return Secure content Uri for the newly created temporary photo file.
     */
    fun createTempPhotoUri(context: Context): Uri {
        val directory =
            File(context.cacheDir, "camera_cache").apply {
                if (!exists()) mkdirs()
            }
        val file =
            File(directory, "captured_smart_resizer.jpg").apply {
                if (exists()) delete()
                createNewFile()
            }
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file,
        )
    }

    /**
     * Compresses, scales, limits size if target limit specified, and writes the processed Bitmap to
     * public system storage under the "Pictures/Photo-Resizer" directory using MediaStore API.
     *
     * @param context Application/Activity context.
     * @param bitmap Source bitmap to save.
     * @param quality Quality percentage of compression.
     * @param formatStr Export file format ("JPG", "PNG", or "WEBP").
     * @param sizeLimitKb Optional file size target limit in Kilobytes. If specified, the helper
     * recursively compresses or downscales the image to stay below the limit.
     * @return Output media Uri if saved successfully, null otherwise.
     */
    fun saveBitmapToPublicStorage(
        context: Context,
        bitmap: Bitmap,
        quality: Int = 90,
        formatStr: String = "JPG",
        sizeLimitKb: Int? = null,
    ): Uri? {
        val ext = formatStr.lowercase()
        val mimeType =
            when (formatStr.uppercase()) {
                "PNG" -> "image/png"
                "WEBP" -> "image/webp"
                else -> "image/jpeg"
            }

        val format =
            when (formatStr.uppercase()) {
                "PNG" -> Bitmap.CompressFormat.PNG
                "WEBP" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSLESS
                    } else {
                        @Suppress("DEPRECATION")
                        Bitmap.CompressFormat.WEBP
                    }
                }
                else -> Bitmap.CompressFormat.JPEG
            }

        val actualExt = if (ext == "jpeg") "jpg" else ext
        val name = "Photo-Resizer_" + System.currentTimeMillis() + "." + actualExt
        val contentValues =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Photo-Resizer")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

        val resolver = context.contentResolver
        val targetUri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null

        return try {
            resolver.openOutputStream(targetUri)?.use { stream ->
                val rawBytes: ByteArray
                if (sizeLimitKb != null && sizeLimitKb > 0) {
                    // Iterative search for quality configuration conforming to KB target
                    var currentQuality = quality
                    var tempBytes = ByteArray(0)
                    val limitBytes = sizeLimitKb * 1024
                    var streamSize = 0

                    do {
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(format, currentQuality, baos)
                        tempBytes = baos.toByteArray()
                        streamSize = tempBytes.size
                        currentQuality -= 10
                    } while (streamSize > limitBytes && currentQuality >= 15)

                    // Progressive dimensions downscale if quality reduction alone fails
                    if (streamSize > limitBytes) {
                        var scale = 0.9f
                        var secondaryBmp = bitmap
                        while (streamSize > limitBytes && scale > 0.15f) {
                            val sW = (bitmap.width * scale).toInt().coerceAtLeast(1)
                            val sH = (bitmap.height * scale).toInt().coerceAtLeast(1)
                            secondaryBmp = Bitmap.createScaledBitmap(bitmap, sW, sH, true)
                            val baos = ByteArrayOutputStream()
                            secondaryBmp.compress(format, 70, baos)
                            tempBytes = baos.toByteArray()
                            streamSize = tempBytes.size
                            scale -= 0.1f
                        }
                    }
                    rawBytes = tempBytes
                } else {
                    // Auto mode: preserve high quality compression without size reduction
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(format, quality, baos)
                    rawBytes = baos.toByteArray()
                }

                stream.write(rawBytes)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(targetUri, contentValues, null, null)
            }
            targetUri
        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(targetUri, null, null)
            null
        }
    }

    /**
     * Resolves and extracts high-level metadata (width, height, and file size in bytes)
     * of a media resource referenced by its external content Uri.
     *
     * @param context Context used to open content resolvers.
     * @param uri Source media Uri.
     * @return ImageMetadata representing the media details, or null if reading failed.
     */
    fun loadMetadataFromUri(
        context: Context,
        uri: Uri,
    ): ImageMetadata? =
        try {
            var sizeBytes = 0L
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && cursor.moveToFirst()) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
            if (sizeBytes <= 0) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    sizeBytes = stream.available().toLong()
                }
            }

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            if (options.outWidth > 0 && options.outHeight > 0) {
                ImageMetadata(
                    width = options.outWidth,
                    height = options.outHeight,
                    sizeBytes = sizeBytes,
                    uri = uri,
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    /**
     * Decodes a Bitmap from the given external content Uri, automatically resolving and correcting
     * any orientation/rotation adjustments found in its Exif metadata (e.g. captured camera orientation).
     *
     * @param context Context used to resolve input stream.
     * @param uri Source media Uri.
     * @return Rotated/adjusted Bitmap, or null if decoding fails.
     */
    fun loadCorrectedBitmap(
        context: Context,
        uri: Uri,
    ): Bitmap? {
        return try {
            val original =
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: return null

            var rotation = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exifInterface = android.media.ExifInterface(stream)
                    val orientation =
                        exifInterface.getAttributeInt(
                            android.media.ExifInterface.TAG_ORIENTATION,
                            android.media.ExifInterface.ORIENTATION_NORMAL,
                        )
                    rotation =
                        when (orientation) {
                            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                            else -> 0
                        }
                }
            }
            if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
            } else {
                original
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
