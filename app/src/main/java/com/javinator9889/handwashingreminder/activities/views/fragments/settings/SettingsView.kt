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

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.LayoutVisibilityChange
import com.javinator9889.handwashingreminder.data.SettingsLoader
import com.javinator9889.handwashingreminder.gms.vendor.BillingService
import java.lang.ref.WeakReference

class SettingsView : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener,
    LayoutVisibilityChange {
    lateinit var firebaseAnalyticsPreference:
            WeakReference<Preference>
    lateinit var firebasePerformancePreference:
            WeakReference<Preference>
    lateinit var adsPreference: WeakReference<Preference>
    lateinit var donationsPreference: WeakReference<ListPreference>
    lateinit var billingService: BillingService
    private val loader = SettingsLoader(view = this, lifecycleOwner =  this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().setTheme(R.style.AppTheme_MaterialDialogs)
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        billingService = BillingService(view.context)
    }

    override fun onPreferenceChange(
        preference: Preference?,
        newValue: Any?
    ): Boolean = loader.onPreferenceChange(preference, newValue)

    override fun onVisibilityChanged(visibility: Int) {
        if (visibility == View.VISIBLE)
            loader.loadViews()
    }
}