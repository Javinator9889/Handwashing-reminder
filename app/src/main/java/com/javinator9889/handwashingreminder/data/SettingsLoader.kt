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
 * Created by Javinator9889 on 11/06/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.data

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.emoji.text.EmojiCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.afollestad.materialdialogs.MaterialDialog
import com.android.billingclient.api.BillingClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.PrivacyTermsActivity
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.SettingsView
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.TimePickerPreference
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.gms.ads.AdsEnabler
import com.javinator9889.handwashingreminder.gms.splitservice.SplitInstallService
import com.javinator9889.handwashingreminder.jobs.alarms.AlarmHandler
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
import java.util.concurrent.atomic.AtomicBoolean

class SettingsLoader(
    private val view: SettingsView,
    private val lifecycleOwner: LifecycleOwner
) : OnPurchaseFinishedListener, Preference.OnPreferenceChangeListener {
    private lateinit var emojiLoader: Deferred<EmojiCompat>
    private lateinit var emojiCompat: EmojiCompat
    private var arePreferencesInitialized = AtomicBoolean(false)

    fun loadViews() {
        if (arePreferencesInitialized.get())
            return
        val deferreds = mutableSetOf<Deferred<Any?>>()
        emojiLoader = EmojiLoader.loadAsync(view.requireContext())
        with(view) {
            lifecycleOwner.lifecycleScope.launch {
                setupPreferenceAsync(
                    "playstore",
                    Ionicons.Icon.ion_android_playstore,
                    onClickListener = {
                        openWebsite(PLAYSTORE_URL, R.string.playstore_err)
                        true
                    }).also { deferreds.add(it) }
                setupPreferenceAsync(
                    "share",
                    Ionicons.Icon.ion_android_share,
                    onClickListener = {
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
                    }).also { deferreds.add(it) }
                setupPreferenceAsync("telegram", onClickListener = {
                    openWebsite(TELEGRAM_URL, R.string.telegram_err)
                    true
                }).also { deferreds.add(it) }
                setupPreferenceAsync(
                    "github",
                    Ionicons.Icon.ion_social_github,
                    onClickListener = {
                        openWebsite(GITHUB_URL, R.string.browser_err)
                        true
                    }).also { deferreds.add(it) }
                setupPreferenceAsync(
                    "twitter",
                    Ionicons.Icon.ion_social_twitter,
                    onClickListener = {
                        openWebsite(TWITTER_URL, R.string.twitter_err)
                        true
                    }).also { deferreds.add(it) }
                setupPreferenceAsync(
                    "linkedin",
                    Ionicons.Icon.ion_social_linkedin,
                    onClickListener = {
                        openWebsite(LINKEDIN_URL, R.string.browser_err)
                        true
                    }).also { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.ANALYTICS_ENABLED,
                    Ionicons.Icon.ion_arrow_graph_up_right,
                    onChangeListener = this@with,
                    onInitialized = { it, _ ->
                        firebaseAnalyticsPreference = WeakReference(it)
                    }
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.PERFORMANCE_ENABLED,
                    Ionicons.Icon.ion_ios_speedometer_outline,
                    onChangeListener = this@with,
                    onInitialized = { it, _ ->
                        firebasePerformancePreference = WeakReference(it)
                    }
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.ADS_ENABLED,
                    Ionicons.Icon.ion_ios_barcode_outline,
                    onChangeListener = this@with,
                    onInitialized = { it, _ ->
                        adsPreference = WeakReference(it)
                    }
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.DONATIONS,
                    Ionicons.Icon.ion_card,
                    onChangeListener = this@with,
                    onInitialized = { it, _ ->
                        it as ListPreference
                        it.entryValues = if (isDebuggable())
                            requireContext().resources.getTextArray(R.array.in_app_donations_debug)
                        else
                            requireContext().resources.getTextArray(R.array.in_app_donations)
                        billingService.addOnPurchaseFinishedListener(this@SettingsLoader)
                        donationsPreference = WeakReference(it)
                    }
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    "translate",
                    Ionicons.Icon.ion_chatbox_working,
                    onClickListener = {
                        openWebsite(
                            TRANSLATE_URL,
                            R.string.browser_err
                        )
                        true
                    }
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    "send_suggestions",
                    Ionicons.Icon.ion_chatbubbles,
                    onClickListener = {
                        with(Intent(Intent.ACTION_SENDTO)) {
                            type = "text/plain"
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(Email.TO))
                            putExtra(Intent.EXTRA_SUBJECT, Email.SUBJECT)
                            putExtra(Intent.EXTRA_TEXT, getDeviceInfo())
                            putExtra(
                                Intent.EXTRA_HTML_TEXT,
                                getDeviceInfoHTML()
                            )
                            if (resolveActivity(requireContext().packageManager) != null)
                                startActivity(this)
                            else MaterialDialog(requireContext()).show {
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
                        true
                    }
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    "opensource_libs",
                    Ionicons.Icon.ion_code,
                    onClickListener = {
                        val bundle = Bundle(1).apply {
                            putString("view", "libs")
                        }
                        with(FirebaseAnalytics.getInstance(requireContext())) {
                            logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
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
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    "tos_privacy",
                    Ionicons.Icon.ion_android_cloud_done,
                    onClickListener = {
                        Intent(
                            requireContext(),
                            PrivacyTermsActivity::class.java
                        ).run { startActivity(this) }
                        true
                    }
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.BREAKFAST_TIME,
                    Ionicons.Icon.ion_coffee,
                    onInitialized = ::setupTimePickerDialog,
                    onInitializedArgs = setOf(
                        "title" to getText(R.string.breakfast_pref_title),
                        "summary" to getText(R.string.breakfast_pref_summ),
                        "alarm" to Alarms.BREAKFAST_ALARM
                    ),
                    dispatcher = Dispatchers.Main
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.LUNCH_TIME,
                    Ionicons.Icon.ion_android_restaurant,
                    onInitialized = ::setupTimePickerDialog,
                    onInitializedArgs = setOf(
                        "title" to getText(R.string.lunch_pref_title),
                        "summary" to getText(R.string.lunch_pref_summ),
                        "alarm" to Alarms.LUNCH_ALARM
                    ),
                    dispatcher = Dispatchers.Main
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.DINNER_TIME,
                    Ionicons.Icon.ion_ios_moon_outline,
                    onInitialized = ::setupTimePickerDialog,
                    onInitializedArgs = setOf(
                        "title" to getText(R.string.dinner_pref_title),
                        "summary" to getText(R.string.dinner_pref_summ),
                        "alarm" to Alarms.DINNER_ALARM
                    ),
                    dispatcher = Dispatchers.Main
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.ACTIVITY_MINIMUM_TIME,
                    Ionicons.Icon.ion_ios_stopwatch_outline,
                    onInitialized = { it, _ ->
                        runCatching {
                            it as EditTextPreference
                            val timeText = Integer.parseInt(it.text)
                            val minutes = resources.getQuantityString(
                                R.plurals.minutes,
                                timeText,
                                timeText
                            )
                            it.summary =
                                getString(R.string.minimum_time_summ, minutes)
                        }
                    },
                    onChangeListener = this@with
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.PERFORMANCE_ANIMATIONS,
                    Ionicons.Icon.ion_battery_low
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.INTRO_ANIMATIONS,
                    Ionicons.Icon.ion_play
                ).also { deferreds.add(it) }
                setupPreferenceAsync(
                    "notifications:settings",
                    onClickListener = {
                        val intent = when {
                            isAtLeast(AndroidVersion.O) -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                            }
                            isAtLeast(AndroidVersion.LOLLIPOP) -> Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                                putExtra("app_package", requireContext().packageName)
                                putExtra("app_uid", requireContext().applicationInfo.uid)
                            }
                            else -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                addCategory(Intent.CATEGORY_DEFAULT)
                                data = Uri.parse("package: ${requireContext().packageName}")
                            }
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        requireContext().startActivity(intent)
                        true
                    }
                ).also { deferreds.add(it) }
                deferreds.awaitAll()
                arePreferencesInitialized.set(true)
            }
        }
    }

    override fun onPurchaseFinished(token: String, resultCode: Int) {
        if (view.context == null)
            return
        val context = view.requireContext()
        if (!::emojiCompat.isInitialized)
            runBlocking { emojiCompat = emojiLoader.await() }
        when (resultCode) {
            BillingClient.BillingResponseCode.OK -> {
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
            BillingClient.BillingResponseCode.USER_CANCELED -> {
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

    private fun setupPreferenceAsync(
        name: String,
        icon: IIcon? = null,
        onClickListener: ((Preference) -> Boolean)? = null,
        onChangeListener: Preference.OnPreferenceChangeListener? = null,
        onInitialized: (suspend (Preference, Collection<Pair<Any, Any>>?) -> Unit)? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        onInitializedArgs: Collection<Pair<Any, Any>>? = null
    ): Deferred<Any?> = lifecycleOwner.lifecycleScope.async {
        view.findPreference<Preference>(name)?.let {
            icon.notNull { icon ->
                it.icon = setupIcon(icon)
            }
            onClickListener.notNull { listener ->
                it.setOnPreferenceClickListener(listener)
            }
            onChangeListener.notNull { listener ->
                it.onPreferenceChangeListener = listener
            }
            if (onInitialized != null)
                withContext(dispatcher) {
                    onInitialized(it, onInitializedArgs)
                }
        }
    }

    private suspend fun setupTimePickerDialog(
        preference: Preference,
        args: Collection<Pair<Any, Any>>?
    ) {
        if (args == null)
            return
        var title: CharSequence? = null
        var summary: CharSequence? = null
        var alarm: Alarms? = null
        for (arg in args)
            when (arg.first) {
                "title" -> title = arg.second as CharSequence
                "summary" -> summary = arg.second as CharSequence
                "alarm" -> alarm = arg.second as Alarms
            }
        if (title == null || summary == null || alarm == null)
            return
        if (!::emojiCompat.isInitialized)
            emojiCompat = emojiLoader.await()
        preference as TimePickerPreference
        try {
            preference.title = emojiCompat.process(title)
            preference.summaryText = emojiCompat.process(summary)
        } catch (_: IllegalStateException) {
            preference.title = title
            preference.summaryText = summary
        } finally {
            preference.alarm = alarm
            preference.updateSummary()
        }
    }

    private fun setupIcon(icon: IIcon): IconicsDrawable =
        IconicsDrawable(view.requireContext(), icon).apply { sizeDp = 20 }

    private fun openWebsite(url: String, @StringRes onErrString: Int) =
        openWebsite(url, view.getString(onErrString))

    private fun openWebsite(url: String, onErrString: String) {
        if (view.context == null)
            return
        val website = Uri.parse(url)
        val bundle = Bundle(1).apply { putString("url", url) }
        with(FirebaseAnalytics.getInstance(view.requireContext())) {
            logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        }
        with(Intent(Intent.ACTION_VIEW, website)) {
            if (resolveActivity(view.requireContext().packageManager) != null)
                view.startActivity(this)
            else
                MaterialDialog(view.requireContext()).show {
                    title(R.string.no_app)
                    message(
                        text = view.context.getString(
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

    override fun onPreferenceChange(
        preference: Preference?,
        newValue: Any?
    ): Boolean =
        when (preference?.key) {
            Preferences.ANALYTICS_ENABLED -> {
                val enabled = newValue as Boolean
                Timber.d("Analytics collection is $enabled")
                with(FirebaseAnalytics.getInstance(view.requireContext())) {
                    setAnalyticsCollectionEnabled(enabled)
                    if (!enabled)
                        setCurrentScreen(null, view.requireActivity()::class)
                }
                true
            }
            Preferences.PERFORMANCE_ENABLED -> {
                val enabled = newValue as Boolean
                Timber.d("Performance is $enabled")
                with(FirebasePerformance.getInstance()) {
                    isPerformanceCollectionEnabled = enabled
                }
                true
            }
            Preferences.ADS_ENABLED -> {
                val enabled = newValue as Boolean
                Timber.d("Ads are enabled $enabled")
                var ret = false
                val adEnabler = AdsEnabler(HandwashingApplication.instance)
                if (enabled) {
                    adEnabler.enableAds()
                    with(SplitInstallService.getInstance(view.requireContext())) {
                        deferredInstall(Ads.MODULE_NAME)
                    }
                    ret = true
                } else {
                    MaterialDialog(view.requireContext()).show {
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
                            HandwashingApplication.instance.adLoader = null
                            with(SplitInstallService.getInstance(view.context)) {
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
            Preferences.DONATIONS -> {
                Timber.d("Purchase clicked - $newValue")
                val purchaseId = newValue as String
                if (isConnected())
                    view.billingService.doPurchase(
                        purchaseId,
                        view.requireActivity()
                    )
                else {
                    MaterialDialog(view.requireContext()).show {
                        title(R.string.no_internet_connection)
                        message(R.string.no_internet_connection_long)
                        positiveButton(android.R.string.ok)
                        cancelable(true)
                        cancelOnTouchOutside(true)
                    }
                }
                false
            }
            Preferences.ACTIVITY_MINIMUM_TIME -> runCatching {
                preference as EditTextPreference
                Timber.d("Changing activity interval - $newValue")
                val timeText = Integer.parseInt(newValue as String)
                val minutes = view.resources.getQuantityString(
                    R.plurals.minutes,
                    timeText,
                    timeText
                )
                preference.summary =
                    view.getString(R.string.minimum_time_summ, minutes)

                // Cancel the old alarm and schedule a new one with the updated
                // time
                with(AlarmHandler(view.requireContext())) {
                    scheduleAlarm(Alarms.PENDING_ACTIVITY_ALARM)
                }
                true
            }.getOrElse { Timber.w(it); false }
            else -> true
        }
}
