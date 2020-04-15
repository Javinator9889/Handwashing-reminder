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
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.os.Build
import com.javinator9889.handwashingreminder.application.HandwashingApplication

fun isAtLeast(version: AndroidVersion): Boolean =
    Build.VERSION.SDK_INT >= version.code

fun isHighPerformingDevice(): Boolean {
    with(HandwashingApplication.getInstance()) {
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
    val connectivityManager = HandwashingApplication.getInstance()
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
    (0 != HandwashingApplication.getInstance().applicationInfo.flags and
            ApplicationInfo.FLAG_DEBUGGABLE)
