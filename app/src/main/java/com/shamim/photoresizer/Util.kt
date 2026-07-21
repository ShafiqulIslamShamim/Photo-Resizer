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
    /**
     * Compresses, scales, and limits the size of a Bitmap to meet a given file size limit (if specified) and output format.
     *
     * @param bitmap Source bitmap to compress.
     * @param quality Quality percentage of compression.
     * @param formatStr Export file format ("JPG", "PNG", or "WEBP").
     * @param sizeLimitKb Optional file size target limit in Kilobytes.
     * @return Compressed byte array.
     */
    fun compressBitmapToBytes(
        bitmap: Bitmap,
        quality: Int = 90,
        formatStr: String = "JPG",
        sizeLimitKb: Int? = null,
    ): ByteArray {
        val isLossy = when (formatStr.uppercase()) {
            "PNG" -> false
            "WEBP" -> {
                // If a size limit is specified, use WEBP_LOSSY on Android R+ or lossy WEBP in pre-R so we can compress
                sizeLimitKb != null && sizeLimitKb > 0
            }
            else -> true
        }

        val format = when (formatStr.uppercase()) {
            "PNG" -> Bitmap.CompressFormat.PNG
            "WEBP" -> {
                if (isLossy) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSY
                    } else {
                        @Suppress("DEPRECATION")
                        Bitmap.CompressFormat.WEBP
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSLESS
                    } else {
                        @Suppress("DEPRECATION")
                        Bitmap.CompressFormat.WEBP
                    }
                }
            }
            else -> Bitmap.CompressFormat.JPEG
        }

        var tempBytes: ByteArray
        if (sizeLimitKb != null && sizeLimitKb > 0) {
            val limitBytes = sizeLimitKb * 1024
            var finalBytes = ByteArray(0)

            if (isLossy) {
                // Binary search for optimal quality in range [1, 100] to meet file size target
                var low = 1
                var high = 100
                var bestQuality = 1
                var bestBytes = ByteArray(0)

                while (low <= high) {
                    val mid = (low + high) / 2
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(format, mid, baos)
                    val compressed = baos.toByteArray()
                    if (compressed.size <= limitBytes) {
                        bestQuality = mid
                        bestBytes = compressed
                        low = mid + 1
                    } else {
                        high = mid - 1
                    }
                }

                if (bestBytes.isEmpty()) {
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(format, 1, baos)
                    bestBytes = baos.toByteArray()
                }
                finalBytes = bestBytes
            } else {
                // Lossless formats (PNG, lossless WEBP): quality has no effect, compress once
                val baos = ByteArrayOutputStream()
                bitmap.compress(format, 100, baos)
                finalBytes = baos.toByteArray()
            }

            tempBytes = finalBytes
        } else {
            // Auto mode: preserve high quality compression without size reduction
            val baos = ByteArrayOutputStream()
            bitmap.compress(format, quality, baos)
            tempBytes = baos.toByteArray()
        }

        return tempBytes
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
        originalUri: Uri? = null,
        originalName: String? = null,
    ): Uri? {
        val ext = formatStr.lowercase()
        val mimeType =
            when (formatStr.uppercase()) {
                "PNG" -> "image/png"
                "WEBP" -> "image/webp"
                else -> "image/jpeg"
            }

        val actualExt = if (ext == "jpeg") "jpg" else ext
        val baseName = if (!originalName.isNullOrBlank()) {
            val dotIndex = originalName.lastIndexOf('.')
            if (dotIndex != -1) originalName.substring(0, dotIndex) else originalName
        } else {
            "Photo-Resizer_" + System.currentTimeMillis()
        }
        val name = "$baseName.$actualExt"

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

        val tempFile = java.io.File(context.cacheDir, "temp_process_" + System.currentTimeMillis() + "." + actualExt)
        return try {
            val rawBytes = compressBitmapToBytes(bitmap, quality, formatStr, sizeLimitKb)
            tempFile.writeBytes(rawBytes)

            // Copy EXIF attributes if originalUri is provided and destination format supports EXIF
            if (originalUri != null && formatStr.uppercase() != "PNG") {
                try {
                    context.contentResolver.openInputStream(originalUri)?.use { inputStream ->
                        val srcExif = android.media.ExifInterface(inputStream)
                        val destExif = android.media.ExifInterface(tempFile.absolutePath)
                        val tags = arrayOf(
                            android.media.ExifInterface.TAG_MAKE,
                            android.media.ExifInterface.TAG_MODEL,
                            android.media.ExifInterface.TAG_F_NUMBER,
                            android.media.ExifInterface.TAG_DATETIME,
                            android.media.ExifInterface.TAG_DATETIME_DIGITIZED,
                            android.media.ExifInterface.TAG_DATETIME_ORIGINAL,
                            android.media.ExifInterface.TAG_EXPOSURE_TIME,
                            android.media.ExifInterface.TAG_FLASH,
                            android.media.ExifInterface.TAG_FOCAL_LENGTH,
                            android.media.ExifInterface.TAG_GPS_ALTITUDE,
                            android.media.ExifInterface.TAG_GPS_ALTITUDE_REF,
                            android.media.ExifInterface.TAG_GPS_DATESTAMP,
                            android.media.ExifInterface.TAG_GPS_LATITUDE,
                            android.media.ExifInterface.TAG_GPS_LATITUDE_REF,
                            android.media.ExifInterface.TAG_GPS_LONGITUDE,
                            android.media.ExifInterface.TAG_GPS_LONGITUDE_REF,
                            android.media.ExifInterface.TAG_GPS_PROCESSING_METHOD,
                            android.media.ExifInterface.TAG_GPS_TIMESTAMP,
                            android.media.ExifInterface.TAG_ISO_SPEED_RATINGS,
                            android.media.ExifInterface.TAG_LIGHT_SOURCE,
                            android.media.ExifInterface.TAG_WHITE_BALANCE,
                            android.media.ExifInterface.TAG_SUBSEC_TIME,
                            android.media.ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
                            android.media.ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
                            android.media.ExifInterface.TAG_OFFSET_TIME,
                            android.media.ExifInterface.TAG_OFFSET_TIME_DIGITIZED,
                            android.media.ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
                            android.media.ExifInterface.TAG_APERTURE_VALUE,
                            android.media.ExifInterface.TAG_SHUTTER_SPEED_VALUE,
                            android.media.ExifInterface.TAG_EXPOSURE_PROGRAM,
                            android.media.ExifInterface.TAG_EXPOSURE_MODE,
                            android.media.ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
                            android.media.ExifInterface.TAG_CONTRAST,
                            android.media.ExifInterface.TAG_SATURATION,
                            android.media.ExifInterface.TAG_SHARPNESS,
                            android.media.ExifInterface.TAG_METERING_MODE,
                            android.media.ExifInterface.TAG_EXIF_VERSION,
                            "LensMake",
                            "LensModel",
                            "LensSpecification",
                            "Software"
                        )
                        for (tag in tags) {
                            val value = srcExif.getAttribute(tag)
                            if (value != null) {
                                destExif.setAttribute(tag, value)
                            }
                        }
                        destExif.saveAttributes()
                    }
                } catch (exifEx: Exception) {
                    exifEx.printStackTrace()
                }
            }

            resolver.openOutputStream(targetUri)?.use { stream ->
                tempFile.inputStream().use { input ->
                    input.copyTo(stream)
                }
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
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
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
            var originalName: String? = null
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        sizeBytes = cursor.getLong(sizeIndex)
                    }
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        originalName = cursor.getString(nameIndex)
                    }
                }
            }
            if (originalName == null) {
                originalName = uri.lastPathSegment
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

            val mimeType = options.outMimeType ?: context.contentResolver.getType(uri)
            val detectedFormat = when {
                mimeType?.contains("png", ignoreCase = true) == true -> "PNG"
                mimeType?.contains("webp", ignoreCase = true) == true -> "WEBP"
                originalName?.endsWith(".png", ignoreCase = true) == true -> "PNG"
                originalName?.endsWith(".webp", ignoreCase = true) == true -> "WEBP"
                else -> "JPG"
            }

            if (options.outWidth > 0 && options.outHeight > 0) {
                ImageMetadata(
                    width = options.outWidth,
                    height = options.outHeight,
                    sizeBytes = sizeBytes,
                    uri = uri,
                    originalName = originalName,
                    format = detectedFormat,
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

    /**
     * Applies fine rotation and 3D perspective to a given Bitmap.
     */
    fun applyPerspectiveAndRotation(
        bitmap: Bitmap,
        rotationZ: Float,
        rotationX: Float,
        rotationY: Float
    ): Bitmap {
        if (rotationZ == 0f && rotationX == 0f && rotationY == 0f) return bitmap

        val width = bitmap.width
        val height = bitmap.height

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)

        val camera = android.graphics.Camera()
        val matrix = android.graphics.Matrix()

        camera.save()
        camera.rotateX(-rotationX)
        camera.rotateY(rotationY)
        camera.rotateZ(-rotationZ)
        camera.getMatrix(matrix)
        camera.restore()

        val centerX = width / 2f
        val centerY = height / 2f
        matrix.preTranslate(-centerX, -centerY)
        matrix.postTranslate(centerX, centerY)

        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, matrix, paint)

        return result
    }

    /**
     * Warps a given Bitmap from a source quadrilateral defined by 4 normalized points (top-left, top-right,
     * bottom-right, bottom-left) to a rectified destination rectangle.
     */
    fun apply4PointPerspectiveWarp(
        bitmap: Bitmap,
        perspectivePoints: FloatArray
    ): Bitmap {
        val w = bitmap.width.toFloat()
        val h = bitmap.height.toFloat()

        val tlX = perspectivePoints[0] * w
        val tlY = perspectivePoints[1] * h
        val trX = perspectivePoints[2] * w
        val trY = perspectivePoints[3] * h
        val brX = perspectivePoints[4] * w
        val brY = perspectivePoints[5] * h
        val blX = perspectivePoints[6] * w
        val blY = perspectivePoints[7] * h

        val topWidth = Math.hypot((trX - tlX).toDouble(), (trY - tlY).toDouble())
        val bottomWidth = Math.hypot((brX - blX).toDouble(), (brY - blY).toDouble())
        val leftHeight = Math.hypot((blX - tlX).toDouble(), (blY - tlY).toDouble())
        val rightHeight = Math.hypot((brX - trX).toDouble(), (brY - trY).toDouble())

        val destWidth = maxOf(topWidth, bottomWidth).toInt().coerceIn(100, 10000)
        val destHeight = maxOf(leftHeight, rightHeight).toInt().coerceIn(100, 10000)

        val srcPoints = floatArrayOf(
            tlX, tlY,
            trX, trY,
            brX, brY,
            blX, blY
        )
        val dstPoints = floatArrayOf(
            0f, 0f,
            destWidth.toFloat(), 0f,
            destWidth.toFloat(), destHeight.toFloat(),
            0f, destHeight.toFloat()
        )

        val matrix = android.graphics.Matrix()
        matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)

        val result = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, matrix, paint)

        return result
    }
}
