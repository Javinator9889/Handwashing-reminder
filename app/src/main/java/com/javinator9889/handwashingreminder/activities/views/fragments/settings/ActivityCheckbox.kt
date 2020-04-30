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
 * Created by Javinator9889 on 16/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.fragments.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.preference.CheckBoxPreference
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.isAtLeast
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import com.mikepenz.iconics.utils.sizeDp

class ActivityCheckbox : CheckBoxPreference {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private var firstCheck = true

    init {
        var isViewDisabled = false
        with(GoogleApiAvailability.getInstance()) {
            if (isGooglePlayServicesAvailable(context) !=
                ConnectionResult.SUCCESS
            ) {
                isViewDisabled = true
                summaryOff = context.getText(R.string.google_api_not_found)
                isChecked = false
            }
        }
        if (!isViewDisabled) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACTIVITY_RECOGNITION
                ) == PERMISSION_DENIED && isAtLeast(AndroidVersion.Q)
            ) {
                isViewDisabled = true
                summaryOff =
                    context.getText(R.string.permission_permanently_denied)
                isChecked = false
            } else {
                summaryOff = context.getText(R.string.activity_disabled)
                summaryOn = context.getText(R.string.activity_enabled)
            }
        }
        isEnabled = !isViewDisabled
        icon = IconicsDrawable(
            context, Ionicons.Icon.ion_android_walk
        ).apply { sizeDp = 20 }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        if (isEnabled)
            super.onSetInitialValue(defaultValue)
    }

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        if (firstCheck) {
            firstCheck = false
            return
        }
        with(HandwashingApplication.instance) {
            if (checked) {
                activityHandler.startTrackingActivity()
            } else {
                activityHandler.disableActivityTracker()
            }
        }
    }
}