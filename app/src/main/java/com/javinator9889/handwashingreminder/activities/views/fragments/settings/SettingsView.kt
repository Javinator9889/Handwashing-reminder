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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.emoji.text.EmojiCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.afollestad.materialdialogs.MaterialDialog
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.gms.ads.AdsEnabler
import com.javinator9889.handwashingreminder.gms.splitservice.SplitInstallService
import com.javinator9889.handwashingreminder.utils.Ads
import com.javinator9889.handwashingreminder.utils.Preferences
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

class SettingsView : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {
    private lateinit var firebaseAnalyticsPreference:
            WeakReference<SwitchPreference>
    private lateinit var firebasePerformancePreference:
            WeakReference<SwitchPreference>
    private lateinit var adsPreference: WeakReference<SwitchPreference>
    private lateinit var emojiCompat: EmojiCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().setTheme(R.style.AppTheme_MaterialDialogs)
        thread(start = true) {
            emojiCompat = runBlocking {
                EmojiLoader.get(requireContext()).await()
            }
        }
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        val firebaseAnalytics = findPreference<SwitchPreference>(
            Preferences.ANALYTICS_ENABLED
        )
        val firebasePerformance = findPreference<SwitchPreference>(
            Preferences.PERFORMANCE_ENABLED
        )
        val ads = findPreference<SwitchPreference>(Preferences.ADS_ENABLED)
        firebaseAnalytics?.let {
            it.onPreferenceChangeListener = this
            firebaseAnalyticsPreference = WeakReference(it)
        }
        firebasePerformance?.let {
            it.onPreferenceChangeListener = this
            firebasePerformancePreference = WeakReference(it)
        }
        ads?.let {
            it.onPreferenceChangeListener = this
            adsPreference = WeakReference(it)
        }
        return view
    }

    override fun onPreferenceChange(
        preference: Preference?,
        newValue: Any?
    ): Boolean {
        val app = HandwashingApplication.getInstance()
        return when {
            ::firebaseAnalyticsPreference.isInitialized &&
                    preference == firebaseAnalyticsPreference.get() -> {
                val enabled = newValue as Boolean
//                if (enabled)
                //TODO app.firebaseAnalytics.enable()
//                else
                //TODO app.firebaseAnalytics.disable()
                true
            }
            ::firebasePerformancePreference.isInitialized &&
                    preference == firebasePerformancePreference.get() -> {
                val enabled = newValue as Boolean
//                if (enabled)
                //TODO app.firebasePerformance.enable()
//                else
                //TODO app.firebasePerformance.disable()
                true
            }
            ::adsPreference.isInitialized &&
                    preference == adsPreference.get() -> {
                val enabled = newValue as Boolean
                var ret = false
                val adEnabler = AdsEnabler(app)
                if (enabled) {
                    adEnabler.enableAds()
                    with(SplitInstallService.getInstance(app)) {
                        deferredInstall(Ads.MODULE_NAME)
                    }
                    ret = true
                } else {
                    MaterialDialog(requireContext()).show {
                        title(R.string.ads_explanation_title)
                        message(
                            text = emojiCompat.process(
                                context.getText(R.string.ads_explanation_desc)
                            )
                        )
                        positiveButton(android.R.string.cancel) { ret = false }
                        negativeButton(R.string.disable) {
                            ret = true;
                            adEnabler.disableAds()
                            app.adLoader = null
                            with(SplitInstallService.getInstance(app)) {
                                deferredUninstall(Ads.MODULE_NAME)
                            }
                            (preference as SwitchPreference).isChecked = false
                        }
                        cancelOnTouchOutside(false)
                        cancelable(false)
                    }
                }
                Timber.d("Disabling ads? $ret")
                ret
            }
            else -> true
        }
    }


    /*override fun onDisplayPreferenceDialog(preference: Preference?) {
        val dialogFragment = if (preference is TimePreference)
            TimePreferenceFragment.newInstance(preference.key)
        else
            null
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, null)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }*/
}