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

import android.net.Uri

// Screen representations for State Machine
sealed interface ScreenState {
    object Welcome : ScreenState

    object Configure : ScreenState

    object Cropping : ScreenState

    object Preview : ScreenState
}

// Data Model to represent Loaded Image Metadata
data class ImageMetadata(
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val uri: Uri,
    val originalName: String? = null,
    val format: String = "JPG",
)
