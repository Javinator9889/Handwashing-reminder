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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.emoji.text.EmojiCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.afollestad.materialdialogs.MaterialDialog
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.google.firebase.analytics.FirebaseAnalytics
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.PrivacyTermsActivity
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.gms.ads.AdsEnabler
import com.javinator9889.handwashingreminder.gms.splitservice.SplitInstallService
import com.javinator9889.handwashingreminder.listeners.OnPurchaseFinishedListener
import com.javinator9889.handwashingreminder.utils.*
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

class SettingsView : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener, OnPurchaseFinishedListener {
    private lateinit var firebaseAnalyticsPreference:
            WeakReference<SwitchPreference>
    private lateinit var firebasePerformancePreference:
            WeakReference<SwitchPreference>
    private lateinit var adsPreference: WeakReference<SwitchPreference>
    private lateinit var donationsPreference: WeakReference<ListPreference>
    private lateinit var emojiCompat: EmojiCompat
    private val app = HandwashingApplication.getInstance()

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
        val donations = findPreference<ListPreference>(Preferences.DONATIONS)
        val translations = findPreference<Preference>("translate")
        val suggestions = findPreference<Preference>("send_suggestions")
        val libraries = findPreference<Preference>("opensource_libs")
        val privacyAndTerms = findPreference<Preference>("tos_privacy")
        share?.let {
            it.icon = icon(Ionicons.Icon.ion_android_share)
            it.setOnPreferenceClickListener {
                app.firebaseAnalytics.logEvent(
                    FirebaseAnalytics.Event.SHARE, null
                )
                with(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, getText(R.string.share_text))
                    putExtra(Intent.EXTRA_TITLE, getText(R.string.share_title))
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
        playStore?.let {
            it.icon = icon(Ionicons.Icon.ion_android_playstore)
            it.setOnPreferenceClickListener {
                openWebsite(PLAYSTORE_URL, "Play Store")
                true
            }
        }
        telegram?.let {
            it.setOnPreferenceClickListener {
                openWebsite(TELEGRAM_URL, "Telegram")
                true
            }
        }
        github?.let {
            it.icon = icon(Ionicons.Icon.ion_social_github)
            it.setOnPreferenceClickListener {
                openWebsite(GITHUB_URL, R.string.browser_err)
                true
            }
        }
        twitter?.let {
            it.icon = icon(Ionicons.Icon.ion_social_twitter)
            it.setOnPreferenceClickListener {
                openWebsite(TWITTER_URL, "Twitter")
                true
            }
        }
        linkedIn?.let {
            it.icon = icon(Ionicons.Icon.ion_social_linkedin)
            it.setOnPreferenceClickListener {
                openWebsite(LINKEDIN_URL, R.string.browser_err)
                true
            }
        }
        breakfast?.let {
            it.icon = icon(Ionicons.Icon.ion_coffee)
        }
        lunch?.let {
            it.icon = icon(Ionicons.Icon.ion_android_restaurant)
        }
        dinner?.let {
            it.icon = icon(Ionicons.Icon.ion_ios_moon_outline)
        }
        firebaseAnalytics?.let {
            it.onPreferenceChangeListener = this
            it.icon = icon(Ionicons.Icon.ion_arrow_graph_up_right)
            firebaseAnalyticsPreference = WeakReference(it)
        }
        firebasePerformance?.let {
            it.onPreferenceChangeListener = this
            it.icon = icon(Ionicons.Icon.ion_speedometer)
            firebasePerformancePreference = WeakReference(it)
        }
        ads?.let {
            it.onPreferenceChangeListener = this
            it.icon = icon(Ionicons.Icon.ion_ios_barcode_outline)
            adsPreference = WeakReference(it)
        }
        donations?.let {
            it.onPreferenceChangeListener = this
            it.entryValues = if (isDebuggable())
                resources.getTextArray(R.array.in_app_donations_debug)
            else
                resources.getTextArray(R.array.in_app_donations)
            it.icon = icon(Ionicons.Icon.ion_card)
            app.billingService.addOnPurchaseFinishedListener(this)
            donationsPreference = WeakReference(it)
        }
        translations?.let {
            it.icon = icon(Ionicons.Icon.ion_chatbox_working)
            it.setOnPreferenceClickListener {
                openWebsite(TRANSLATE_URL, R.string.browser_err)
                true
            }
        }
        suggestions?.let {
            it.setOnPreferenceClickListener {
                with(Intent(Intent.ACTION_SEND)) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(Email.TO))
                    putExtra(Intent.EXTRA_SUBJECT, Email.SUBJECT)
                    putExtra(Intent.EXTRA_TEXT, getDeviceInfo())
                    type = "message/rfc822"
                    startActivity(
                        Intent.createChooser(
                            this, getString(R.string.send_email_client)
                        )
                    )
                }
                true
            }
            it.icon = icon(Ionicons.Icon.ion_chatbubbles)
        }
        libraries?.let {
            it.setOnPreferenceClickListener {
                val bundle = Bundle(1).apply { putString("view", "libs") }
                app.firebaseAnalytics.logEvent(
                    FirebaseAnalytics.Event.VIEW_ITEM, bundle
                )
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
        return view
    }

    override fun onPreferenceChange(
        preference: Preference?,
        newValue: Any?
    ): Boolean {
        return when {
            ::firebaseAnalyticsPreference.isInitialized &&
                    preference == firebaseAnalyticsPreference.get() -> {
                val enabled = newValue as Boolean
                app.firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
                if (!enabled)
                    app.firebaseAnalytics.setCurrentScreen(
                        requireActivity(), null, null
                    )
                true
            }
            ::firebasePerformancePreference.isInitialized &&
                    preference == firebasePerformancePreference.get() -> {
                val enabled = newValue as Boolean
                app.firebasePerformance.isPerformanceCollectionEnabled = enabled
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
                app.billingService.doPurchase(purchaseId, requireActivity())
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
                MaterialDialog(context)
                    .title(R.string.donation_thanks)
                    .message(
                        text = emojiCompat
                            .process(context.getText(R.string.donation_desc))
                    )
                    .positiveButton(android.R.string.ok)
            }
            BillingResponseCode.USER_CANCELED -> {
                MaterialDialog(context)
                    .title(R.string.donation_cancelled)
                    .message(
                        text = emojiCompat.process(
                            context.getText(R.string.donation_cancelled_desc)
                        )
                    )
                    .positiveButton(android.R.string.ok)
            }
            else -> {
                MaterialDialog(context)
                    .title(R.string.donation_error)
                    .message(
                        text = emojiCompat.process(
                            context.getText(R.string.donation_error_desc)
                        )
                    )
                    .positiveButton(android.R.string.ok)
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
        app.firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.VIEW_ITEM,
            bundle
        )
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