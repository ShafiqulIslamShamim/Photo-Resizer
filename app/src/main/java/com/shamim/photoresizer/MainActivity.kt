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
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shamim.photoresizer.ui.ConfigureScreen
import com.shamim.photoresizer.ui.CroppingScreen
import com.shamim.photoresizer.ui.PreviewScreen
import com.shamim.photoresizer.ui.WelcomeScreen
import com.shamim.photoresizer.ui.theme.ApplicationTheme

class MainActivity : ComponentActivity() {
    /**
     * Called when the main activity is starting.
     * Initializes the edge-to-edge layout, initiates background OTA updates,
     * and mounts the Jetpack Compose-based App container.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        OTAUpdateHelper.checkForUpdatesIfDue(this)
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager.getInstance(context) }
            val themeMode by settingsManager.themeMode.collectAsState()
            val isAmoled by settingsManager.isAmoled.collectAsState()
            val selectedPalette by settingsManager.selectedPalette.collectAsState()

            ApplicationTheme(
                darkModeSetting = themeMode,
                isAmoledSetting = isAmoled,
                selectedPaletteSetting = selectedPalette,
            ) {
                PhotoResizerApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

/**
 * Root Composable representation of the Photo Resizer application.
 * Manages permission launchers, image selectors, screen state routing (Welcome, Configure, Crop, Preview),
 * and handles full-screen loading spinners during image processing.
 */
@Composable
fun PhotoResizerApp() {
    val context = LocalContext.current
    val viewModel: ViewModel = viewModel()
    val screenState by viewModel.screenState.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val settingsManager = remember { SettingsManager.getInstance(context) }

    val activity = context as? ComponentActivity
    LaunchedEffect(activity?.intent) {
        val intent = activity?.intent
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            if (uri != null) {
                viewModel.setImage(context, uri)
                // Clear the action so rotation or configuration change doesn't reload it
                intent.action = null
            }
        }
    }

    // Temporary camera image captures Uri tracker
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            if (uri != null) {
                viewModel.setImage(context, uri)
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = TakePictureWithPermission(),
        ) { success: Boolean ->
            val uri = tempCameraUri
            if (success && uri != null) {
                viewModel.setImage(context, uri)
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                try {
                    val uri = Util.createTempPhotoUri(context)
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                } catch (e: Exception) {
                    Toast
                        .makeText(context, "Failed to create directory for photo", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast
                    .makeText(
                        context,
                        "Camera permission is required to capture photos",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (screenState) {
            is ScreenState.Welcome -> {
                WelcomeScreen(
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onCameraClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                    onSettingsClick = {
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    },
                )
            }

            is ScreenState.Configure -> {
                BackHandler {
                    viewModel.reset()
                }
                ConfigureScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.reset() },
                    onSettingsClick = {
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    },
                )
            }

            is ScreenState.Cropping -> {
                BackHandler {
                    viewModel.cancelCropping()
                }
                CroppingScreen(
                    viewModel = viewModel,
                    onSettingsClick = {
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    },
                )
            }

            is ScreenState.Preview -> {
                BackHandler {
                    viewModel.backToConfigure()
                }
                PreviewScreen(
                    viewModel = viewModel,
                    onSettingsClick = {
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    },
                )
            }
        }

        // High performance progress overlays
        if (loadingState) {
            Box(
                modifier =
                    Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).pointerInput(
                        Unit,
                    ) {}, // prevent touch propagation
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(32.dp).widthIn(max = 280.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Optimizing Image Quality",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Running direct iterations to target exact limits.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

/**
 * A custom ActivityResultContract for capturing an image from the camera that explicitly
 * grants temporary read/write URI permission flags and package visibility hooks for custom emulators/devices.
 */
class TakePictureWithPermission : ActivityResultContract<Uri, Boolean>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, input)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            val resInfoList = context.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    input,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == android.app.Activity.RESULT_OK
    }
}

