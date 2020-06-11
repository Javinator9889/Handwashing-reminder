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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.emoji.text.EmojiCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.PrivacyTermsActivity
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.SettingsView
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.TimePickerPreference
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.jobs.alarms.Alarms
import com.javinator9889.handwashingreminder.utils.*
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.lang.ref.WeakReference


class SettingsLoaderBak(private val view: SettingsView) {
    private val emojiLoader = EmojiLoader.get(view.requireContext())
    private lateinit var emojiCompat: EmojiCompat

    suspend fun loadViews() {
        with(view) {
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
            val deferreds = mutableSetOf<Deferred<Any?>>()
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
                        it.onPreferenceChangeListener = this@with
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

    private fun icon(icon: IIcon): IconicsDrawable =
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
                        text = view.getString(
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