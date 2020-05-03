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

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.emoji.text.EmojiCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.afollestad.materialdialogs.MaterialDialog
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.PrivacyTermsActivity
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.gms.ads.AdsEnabler
import com.javinator9889.handwashingreminder.gms.splitservice.SplitInstallService
import com.javinator9889.handwashingreminder.gms.vendor.BillingService
import com.javinator9889.handwashingreminder.jobs.alarms.Alarms
import com.javinator9889.handwashingreminder.listeners.OnPurchaseFinishedListener
import com.javinator9889.handwashingreminder.utils.*
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.ref.WeakReference

class SettingsView : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener, OnPurchaseFinishedListener {
    private lateinit var firebaseAnalyticsPreference:
            WeakReference<SwitchPreference>
    private lateinit var firebasePerformancePreference:
            WeakReference<SwitchPreference>
    private lateinit var adsPreference: WeakReference<SwitchPreference>
    private lateinit var donationsPreference: WeakReference<ListPreference>
    private lateinit var emojiCompat: EmojiCompat
    private lateinit var billingService: BillingService
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
        viewLifecycleOwner.lifecycleScope.launch {
            val emojiLoader = EmojiLoader.get(view.context)
            val share = findPreference<Preference>("share")
            val playStore = findPreference<Preference>("playstore")
            val telegram = findPreference<Preference>("telegram")
            val github = findPreference<Preference>("github")
            val linkedIn = findPreference<Preference>("linkedin")
            val twitter = findPreference<Preference>("twitter")
            val breakfast = findPreference<TimePickerPreference>(
                Preferences.BREAKFAST_TIME
            )
            val lunch = findPreference<TimePickerPreference>(
                Preferences.LUNCH_TIME
            )
            val dinner = findPreference<TimePickerPreference>(
                Preferences.DINNER_TIME
            )
            val firebaseAnalytics = findPreference<SwitchPreference>(
                Preferences.ANALYTICS_ENABLED
            )
            val firebasePerformance = findPreference<SwitchPreference>(
                Preferences.PERFORMANCE_ENABLED
            )
            val ads = findPreference<SwitchPreference>(Preferences.ADS_ENABLED)
            val donations =
                findPreference<ListPreference>(Preferences.DONATIONS)
            val translations = findPreference<Preference>("translate")
            val suggestions = findPreference<Preference>("send_suggestions")
            val libraries = findPreference<Preference>("opensource_libs")
            val privacyAndTerms = findPreference<Preference>("tos_privacy")
            val deferreds = mutableListOf<Deferred<Unit?>>()
            deferreds.add(
                async(Dispatchers.Main) {
                    share?.let {
                        it.icon = icon(Ionicons.Icon.ion_android_share)
                        it.setOnPreferenceClickListener {
                            with(FirebaseAnalytics.getInstance(requireContext())) {
                                logEvent(FirebaseAnalytics.Event.SHARE, null)
                            }
                            with(Intent.createChooser(Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    getText(R.string.share_text)
                                )
                                putExtra(
                                    Intent.EXTRA_TITLE,
                                    getText(R.string.share_title)
                                )
                                ClipData.Item(
                                    getUriFromRes(
                                        requireContext(),
                                        R.drawable.handwashing_app_logo
                                    )
                                )
                                clipData = ClipData(
                                    ClipDescription(
                                        getString(R.string.share_label),
                                        arrayOf("image/*")
                                    ),
                                    ClipData.Item(
                                        getUriFromRes(
                                            requireContext(),
                                            R.drawable.handwashing_app_logo
                                        )
                                    )
                                )
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                type = "text/plain"
                            }, null)) {
                                startActivity(this)
                            }
                            true
                        }
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    playStore?.let {
                        it.icon = icon(Ionicons.Icon.ion_android_playstore)
                        it.setOnPreferenceClickListener {
                            openWebsite(PLAYSTORE_URL, R.string.playstore_err)
                            true
                        }
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    telegram?.let {
                        it.setOnPreferenceClickListener {
                            openWebsite(TELEGRAM_URL, R.string.telegram_err)
                            true
                        }
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    github?.let {
                        it.icon = icon(Ionicons.Icon.ion_social_github)
                        it.setOnPreferenceClickListener {
                            openWebsite(GITHUB_URL, R.string.browser_err)
                            true
                        }
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    twitter?.let {
                        it.icon = icon(Ionicons.Icon.ion_social_twitter)
                        it.setOnPreferenceClickListener {
                            openWebsite(TWITTER_URL, R.string.twitter_err)
                            true
                        }
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    linkedIn?.let {
                        it.icon = icon(Ionicons.Icon.ion_social_linkedin)
                        it.setOnPreferenceClickListener {
                            openWebsite(LINKEDIN_URL, R.string.browser_err)
                            true
                        }
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    firebaseAnalytics?.let {
                        it.onPreferenceChangeListener = this@SettingsView
                        it.icon = icon(Ionicons.Icon.ion_arrow_graph_up_right)
                        firebaseAnalyticsPreference = WeakReference(it)
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    firebasePerformance?.let {
                        it.onPreferenceChangeListener = this@SettingsView
                        it.icon = icon(Ionicons.Icon.ion_speedometer)
                        firebasePerformancePreference = WeakReference(it)
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    ads?.let {
                        it.onPreferenceChangeListener = this@SettingsView
                        it.icon = icon(Ionicons.Icon.ion_ios_barcode_outline)
                        adsPreference = WeakReference(it)
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    donations?.let {
                        it.onPreferenceChangeListener = this@SettingsView
                        it.entryValues = if (isDebuggable())
                            resources.getTextArray(R.array.in_app_donations_debug)
                        else
                            resources.getTextArray(R.array.in_app_donations)
                        it.icon = icon(Ionicons.Icon.ion_card)
                        billingService.addOnPurchaseFinishedListener(this@SettingsView)
                        donationsPreference = WeakReference(it)
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    translations?.let {
                        it.icon = icon(Ionicons.Icon.ion_chatbox_working)
                        it.setOnPreferenceClickListener {
                            openWebsite(TRANSLATE_URL, R.string.browser_err)
                            true
                        }
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    suggestions?.let {
                        it.setOnPreferenceClickListener {
                            with(Intent(Intent.ACTION_SENDTO)) {
                                type = "*/*"
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(Email.TO))
                                putExtra(Intent.EXTRA_SUBJECT, Email.SUBJECT)
                                putExtra(Intent.EXTRA_TEXT, getDeviceInfo())
                                if (resolveActivity(requireContext().packageManager)
                                    != null
                                ) {
                                    startActivity(this)
                                } else {
                                    MaterialDialog(requireContext()).show {
                                        title(R.string.no_app)
                                        message(
                                            text = getString(
                                                R.string.no_app_long,
                                                getString(R.string.sending_email)
                                            )
                                        )
                                        positiveButton(android.R.string.ok)
                                        cancelable(true)
                                        cancelOnTouchOutside(true)
                                    }
                                }
                            }
                            true
                        }
                        it.icon = icon(Ionicons.Icon.ion_chatbubbles)
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    libraries?.let {
                        it.setOnPreferenceClickListener {
                            val bundle = Bundle(1).apply {
                                putString("view", "libs")
                            }
                            with(FirebaseAnalytics.getInstance(requireContext())) {
                                logEvent(
                                    FirebaseAnalytics.Event.VIEW_ITEM,
                                    bundle
                                )
                            }
                            LibsBuilder()
                                .withAutoDetect(true)
                                .withFields(R.string::class.java.fields)
                                .withCheckCachedDetection(true)
                                .withSortEnabled(true)
                                .withAboutVersionShown(true)
                                .withAboutVersionShownCode(true)
                                .withAboutVersionShownName(true)
                                .withShowLoadingProgress(true)
                                .withActivityTitle(getString(R.string.app_name))
                                .start(requireContext())
                            true
                        }
                        it.icon = icon(Ionicons.Icon.ion_code)
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    privacyAndTerms?.let {
                        it.setOnPreferenceClickListener {
                            Intent(
                                requireContext(),
                                PrivacyTermsActivity::class.java
                            ).run {
                                startActivity(this)
                            }
                            true
                        }
                        it.icon = icon(Ionicons.Icon.ion_android_cloud_done)
                    }
                })
            deferreds.add(
                async(Dispatchers.Main) {
                    emojiCompat = emojiLoader.await()
                    breakfast?.let {
                        it.icon = icon(Ionicons.Icon.ion_coffee)
                        it.alarm = Alarms.BREAKFAST_ALARM
                        try {
                            it.title =
                                emojiCompat.process(getText(R.string.breakfast_pref_title))
                            it.summaryText =
                                emojiCompat.process(getText(R.string.breakfast_pref_summ))
                        } catch (_: IllegalStateException) {
                            it.title = getText(R.string.breakfast_pref_title)
                            it.summaryText =
                                getText(R.string.breakfast_pref_summ)
                        } finally {
                            it.updateSummary()
                        }
                    }
                    lunch?.let {
                        it.icon = icon(Ionicons.Icon.ion_android_restaurant)
                        it.alarm = Alarms.LUNCH_ALARM
                        try {
                            it.title =
                                emojiCompat.process(getText(R.string.lunch_pref_title))
                            it.summaryText =
                                emojiCompat.process(getText(R.string.lunch_pref_summ))
                        } catch (_: IllegalStateException) {
                            it.title = getText(R.string.lunch_pref_title)
                            it.summaryText = getText(R.string.lunch_pref_summ)
                        } finally {
                            it.updateSummary()
                        }
                    }
                    dinner?.let {
                        it.icon = icon(Ionicons.Icon.ion_ios_moon_outline)
                        it.alarm = Alarms.DINNER_ALARM
                        try {
                            it.title =
                                emojiCompat.process(getText(R.string.dinner_pref_title))
                            it.summaryText =
                                emojiCompat.process(getText(R.string.dinner_pref_summ))
                        } catch (_: IllegalStateException) {
                            it.title = getText(R.string.dinner_pref_title)
                            it.summaryText = getText(R.string.dinner_pref_summ)
                        } finally {
                            it.updateSummary()
                        }
                    }
                })
            deferreds.awaitAll()
        }
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

    private fun icon(icon: IIcon): IconicsDrawable =
        IconicsDrawable(requireContext(), icon).apply { sizeDp = 20 }

    private fun openWebsite(url: String, @StringRes onErrString: Int) {
        openWebsite(url, getString(onErrString))
    }

    private fun openWebsite(url: String, onErrString: String) {
        if (context == null)
            return
        val website = Uri.parse(url)
        val bundle = Bundle(1).apply { putString("url", url) }
        with(FirebaseAnalytics.getInstance(requireContext())) {
            logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        }
        with(Intent(Intent.ACTION_VIEW, website)) {
            if (resolveActivity(requireContext().packageManager) !=
                null
            ) startActivity(this)
            else
                MaterialDialog(requireContext()).show {
                    title(R.string.no_app)
                    message(
                        text = getString(
                            R.string.no_app_long,
                            onErrString
                        )
                    )
                    positiveButton(android.R.string.ok)
                    cancelable(true)
                    cancelOnTouchOutside(true)
                }
        }
    }
}