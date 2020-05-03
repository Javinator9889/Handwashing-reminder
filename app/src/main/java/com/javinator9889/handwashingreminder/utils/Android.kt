/*
 * Copyright © 2020 - present | Handwashing reminder by Javinator9889
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 * Created by Javinator9889 on 11/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.utils

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.annotation.AnyRes
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.javinator9889.handwashingreminder.BuildConfig
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


fun isAtLeast(version: AndroidVersion): Boolean =
    Build.VERSION.SDK_INT >= version.code

fun isHighPerformingDevice(): Boolean {
    with(HandwashingApplication.instance) {
        val activityManager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val isLowRamDevice =
            if (isAtLeast(AndroidVersion.KITKAT_WATCH))
                !activityManager.isLowRamDevice
            else true
        return isLowRamDevice &&
                Runtime.getRuntime().availableProcessors() >= 4 &&
                activityManager.memoryClass >= 128
    }
}

fun isConnected(): Boolean {
    val connectivityManager = HandwashingApplication.instance
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (isAtLeast(AndroidVersion.M)) {
        val network = connectivityManager.activeNetwork ?: return false
        val actNet =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            actNet.hasTransport(TRANSPORT_WIFI) ||
                    actNet.hasTransport(TRANSPORT_CELLULAR) ||
                    actNet.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        with(connectivityManager.activeNetworkInfo) {
            return this?.isConnected ?: false
        }
    }
}

fun isDebuggable(): Boolean =
    (0 != HandwashingApplication.instance.applicationInfo.flags and
            ApplicationInfo.FLAG_DEBUGGABLE)

fun getDeviceInfo(): String = with(StringBuilder()) {
    append("Model: "); append(Build.MODEL); append("\n")
    append("ID: "); append(Build.ID); append("\n")
    append("Manufacturer: "); append(Build.MANUFACTURER); append("\n")
    append("Brand: "); append(Build.BRAND); append("\n")
    append("Incremental: "); append(Build.VERSION.INCREMENTAL); append("\n")
    append("SDK: "); append(Build.VERSION.SDK_INT); append("\n")
    append("Board: "); append(Build.BOARD); append("\n")
    append("Release version: "); append(Build.VERSION.RELEASE); append("\n")
    append("Product: "); append(Build.PRODUCT); append("\n")
    append("App version: "); append(BuildConfig.VERSION_NAME); append(" (");
    append(BuildConfig.VERSION_CODE); append(")\n");
    append("------------------------------------------------------------------")
    append("\n")
    toString()
}

fun getUriFromRes(context: Context, @AnyRes resId: Int): Uri =
    with(context.resources) {
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(getResourcePackageName(resId))
            .appendPath(getResourceTypeName(resId))
            .appendPath(getResourceEntryName(resId))
            .build()
    }

fun isModuleInstalled(context: Context, module: String): Boolean =
    with(SplitInstallManagerFactory.create(context)) {
        isModuleInstalled(this, module)
    }

fun isModuleInstalled(manager: SplitInstallManager, module: String): Boolean =
    module in manager.installedModules

// https://github.com/romannurik/muzei/blob/master/extensions/src/main/java/com/google/android/apps/muzei/util/BroadcastReceiverExt.kt
fun BroadcastReceiver.goAsync(
    coroutineScope: CoroutineScope = GlobalScope,
    block: suspend () -> Unit
) {
    val result = goAsync()
    coroutineScope.launch {
        try {
            block()
        } finally {
            // Always call finish, even if the coroutine scope was cancelled
            result.finish()
        }
    }
}

inline fun <T> trace(name: String, block: (Trace) -> T): T {
    with(FirebasePerformance.startTrace(name)) {
        return try {
            block(this)
        } finally {
            stop()
        }
    }
}

fun <T : View> T.isViewVisible(container: View?): Boolean {
    if (container == null) return true
    val scrollBounds = Rect()
    container.getDrawingRect(scrollBounds)

    val bottom = y + height
    return scrollBounds.top < y && scrollBounds.bottom > bottom
}
