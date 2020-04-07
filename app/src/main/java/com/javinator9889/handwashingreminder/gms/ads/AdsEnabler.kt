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
 * Created by Javinator9889 on 7/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.gms.ads

import android.content.ComponentName
import android.content.pm.PackageManager
import com.javinator9889.handwashingreminder.application.HandwashingApplication


const val PACKAGE_NAME = "com.google.android.gms.ads"
const val CLASS_NAME = "MobileAdsInitProvider"

class AdsEnabler(private val app: HandwashingApplication) {
    private val packageManager = app.packageManager
    private val componentName =
        ComponentName(app, "${PACKAGE_NAME}.${CLASS_NAME}")

    fun disableAds() {
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun enableAds() {
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}