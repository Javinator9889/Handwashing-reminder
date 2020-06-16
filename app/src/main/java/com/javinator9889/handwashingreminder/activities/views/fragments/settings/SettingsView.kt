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
import androidx.emoji.text.EmojiCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.afollestad.materialdialogs.MaterialDialog
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.LayoutVisibilityChange
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.data.SettingsLoader
import com.javinator9889.handwashingreminder.gms.ads.AdsEnabler
import com.javinator9889.handwashingreminder.gms.splitservice.SplitInstallService
import com.javinator9889.handwashingreminder.gms.vendor.BillingService
import com.javinator9889.handwashingreminder.listeners.OnPurchaseFinishedListener
import com.javinator9889.handwashingreminder.utils.Ads
import com.javinator9889.handwashingreminder.utils.isConnected
import timber.log.Timber
import java.lang.ref.WeakReference

class SettingsView : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener, OnPurchaseFinishedListener,
    LayoutVisibilityChange {
    lateinit var firebaseAnalyticsPreference:
            WeakReference<Preference>
    lateinit var firebasePerformancePreference:
            WeakReference<Preference>
    lateinit var adsPreference: WeakReference<Preference>
    lateinit var donationsPreference: WeakReference<ListPreference>
    private lateinit var emojiCompat: EmojiCompat
    lateinit var billingService: BillingService
    private val loader = SettingsLoader(view = this, lifecycleOwner =  this)
    private val app = HandwashingApplication.instance

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
    ): Boolean {
        return when {
            ::firebaseAnalyticsPreference.isInitialized &&
                    preference == firebaseAnalyticsPreference.get() -> {
                val enabled = newValue as Boolean
                with(FirebaseAnalytics.getInstance(requireContext())) {
                    setAnalyticsCollectionEnabled(enabled)
                    if (!enabled)
                        setCurrentScreen(requireActivity(), null, null)
                }
                true
            }
            ::firebasePerformancePreference.isInitialized &&
                    preference == firebasePerformancePreference.get() -> {
                val enabled = newValue as Boolean
                with(FirebasePerformance.getInstance()) {
                    isPerformanceCollectionEnabled = enabled
                }
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
                        try {
                            message(
                                text = emojiCompat.process(
                                    context.getText(R.string.ads_explanation_desc)
                                )
                            )
                        } catch (_: IllegalStateException) {
                            message(R.string.ads_explanation_desc)
                        }
                        positiveButton(android.R.string.cancel) {
                            ret = false
                        }
                        negativeButton(R.string.disable) {
                            ret = true
                            adEnabler.disableAds()
                            app.adLoader = null
                            with(SplitInstallService.getInstance(app)) {
                                deferredUninstall(Ads.MODULE_NAME)
                            }
                            (preference as SwitchPreference).isChecked =
                                false
                        }
                        cancelOnTouchOutside(false)
                        cancelable(false)
                    }
                }
                ret
            }
            ::donationsPreference.isInitialized &&
                    preference == donationsPreference.get() -> {
                Timber.d("Purchase clicked - $newValue")
                val purchaseId = newValue as String
                if (isConnected())
                    billingService.doPurchase(purchaseId, requireActivity())
                else {
                    if (context == null)
                        return false
                    MaterialDialog(requireContext()).show {
                        title(R.string.no_internet_connection)
                        message(R.string.no_internet_connection_long)
                        positiveButton(android.R.string.ok)
                        cancelable(true)
                        cancelOnTouchOutside(true)
                    }
                }
                false
            }
            else -> true
        }
    }

    override fun onPurchaseFinished(token: String, resultCode: Int) {
        if (context == null)
            return
        val context = requireContext()
        when (resultCode) {
            BillingResponseCode.OK -> {
                try {
                    MaterialDialog(context)
                        .title(R.string.donation_thanks)
                        .message(
                            text = emojiCompat
                                .process(context.getText(R.string.donation_desc))
                        )
                        .positiveButton(android.R.string.ok)
                } catch (_: IllegalStateException) {
                    MaterialDialog(context)
                        .title(R.string.donation_thanks)
                        .message(R.string.donation_desc)
                        .positiveButton(android.R.string.ok)
                }
            }
            BillingResponseCode.USER_CANCELED -> {
                try {
                    MaterialDialog(context)
                        .title(R.string.donation_cancelled)
                        .message(
                            text = emojiCompat.process(
                                context.getText(R.string.donation_cancelled_desc)
                            )
                        )
                        .positiveButton(android.R.string.ok)
                } catch (_: IllegalStateException) {
                    MaterialDialog(context)
                        .title(R.string.donation_cancelled)
                        .message(R.string.donation_cancelled_desc)
                        .positiveButton(android.R.string.ok)
                }
            }
            else -> {
                try {
                    MaterialDialog(context)
                        .title(R.string.donation_error)
                        .message(
                            text = emojiCompat.process(
                                context.getText(R.string.donation_error_desc)
                            )
                        )
                        .positiveButton(android.R.string.ok)
                } catch (_: IllegalStateException) {
                    MaterialDialog(context)
                        .title(R.string.donation_error)
                        .message(R.string.donation_error_desc)
                        .positiveButton(android.R.string.ok)
                }
            }
        }.show()
    }

    override fun onVisibilityChanged(visibility: Int) {
        if (visibility == View.VISIBLE)
            loader.loadViews()
    }
}