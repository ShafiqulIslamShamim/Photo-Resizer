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

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

@Suppress("DEPRECATION")
object OTAUpdateHelper {
    private const val RC_APP_UPDATE = 9001

    /**
     * Checks if active internet network connectivity is available.
     * Uses modern NetworkCapabilities API for Android Q and above, and legacy checks for older versions.
     *
     * @param context The application or activity context.
     * @return True if internet is active and reachable, false otherwise.
     */
    fun isInternetAvailable(context: Context): Boolean {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            checkInternetConnectionLegacy(cm)
        }
    }

    /**
     * Performs a legacy fallback connectivity check on pre-Android Q devices.
     *
     * @param cm The ConnectivityManager service instance.
     * @return True if connected, false otherwise.
     */
    private fun checkInternetConnectionLegacy(cm: ConnectivityManager): Boolean {
        var activeNetwork = cm.getNetworkInfo(cm.activeNetwork)
        if (activeNetwork == null) {
            activeNetwork = cm.activeNetworkInfo
        }
        return activeNetwork != null && activeNetwork.isConnected
    }

    /**
     * Resolves the current activity context from a given Context, unwrapping it if necessary.
     *
     * @param context The base context.
     * @return The underlying Activity context, or null if it cannot be determined.
     */
    private fun getActivity(context: Context): Activity? {
        if (context is Activity) {
            return context
        } else if (context is ContextWrapper) {
            return getActivity(context.baseContext)
        }
        return null
    }

    /**
     * Explicitly checks for updates when triggered manually by a settings item click.
     * Displays a progress dialog during check, and triggers Google Play update flow or displays a dialog if up-to-date.
     * Fallback to opening Google Play Store in browser/app on update manager failures.
     *
     * @param context Context used to start update flows and UI dialogs.
     */
    fun hookPreference(context: Context) {
        val activity = getActivity(context)
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Toast
                .makeText(
                    context,
                    "Unable to check for updates: Activity context is not available.",
                    Toast.LENGTH_SHORT,
                ).show()
            return
        }

        if (!isInternetAvailable(activity)) {
            Toast.makeText(activity, "No internet connection available.", Toast.LENGTH_LONG).show()
            return
        }

        val progressDialog =
            AlertDialog
                .Builder(activity)
                .setTitle("Checking for Updates")
                .setMessage("Please wait, searching for the latest version...")
                .setCancelable(false)
                .create()
        progressDialog.show()

        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }

                if (
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            activity,
                            AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                            RC_APP_UPDATE,
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast
                            .makeText(
                                activity,
                                "Failed to launch update: ${e.message}",
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                } else {
                    val appLabel =
                        try {
                            activity.applicationInfo.loadLabel(activity.packageManager).toString()
                        } catch (ex: Exception) {
                            "Photo Resizer"
                        }
                    AlertDialog
                        .Builder(activity)
                        .setTitle("Up to Date")
                        .setMessage("You are already using the latest version of $appLabel.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }.addOnFailureListener { e ->
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }

                try {
                    val packageName = activity.packageName
                    val intent =
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
                        )
                    activity.startActivity(intent)
                } catch (ex: Exception) {
                    Toast.makeText(activity, "Failed to open Google Play Store.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Checks for available application updates in the background.
     * Uses a 24-hour throttling mechanism to avoid excessive network requests on every app startup.
     *
     * @param context Context used to instantiate the update manager.
     */
    fun checkForUpdatesIfDue(context: Context) {
        val activity = getActivity(context)
        if (activity == null || activity.isFinishing || activity.isDestroyed) return

        if (!isInternetAvailable(activity)) return

        val appUpdateManager = AppUpdateManagerFactory.create(activity)

        // Check if there is an update already in progress to resume it immediately
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (
                appUpdateInfo.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                        RC_APP_UPDATE,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return@addOnSuccessListener
            }

            // Throttle auto-checking to once every 24 hours
            val prefName = "update_pref"
            val keyLastCheck = "last_check_time"
            val checkInterval = 24L * 60 * 60 * 1000 // 24 hours

            val prefs = activity.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            val lastCheck = prefs.getLong(keyLastCheck, 0)
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastCheck >= checkInterval) {
                prefs.edit().putLong(keyLastCheck, currentTime).apply()

                if (
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    val appLabel =
                        try {
                            activity.applicationInfo.loadLabel(activity.packageManager).toString()
                        } catch (ex: Exception) {
                            "Photo Resizer"
                        }

                    AlertDialog
                        .Builder(activity)
                        .setTitle("Update Available")
                        .setMessage(
                            "A new version of $appLabel is available. Update now to receive the latest features and bug fixes.",
                        ).setPositiveButton("Update Now") { _, _ ->
                            try {
                                appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    activity,
                                    AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                                    RC_APP_UPDATE,
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }.setNegativeButton("Later", null)
                        .setCancelable(true)
                        .show()
                }
            }
        }
    }
}
