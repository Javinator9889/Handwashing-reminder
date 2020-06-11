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
import androidx.annotation.StringRes
import androidx.emoji.text.EmojiCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.SettingsView
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.utils.*
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class SettingsLoader(
    private val view: SettingsView,
    private val lifecycleOwner: LifecycleOwner
) {
    private val emojiLoader = EmojiLoader.get(view.requireContext())
    private lateinit var emojiCompat: EmojiCompat

    fun loadViews() {
        val deferreds = mutableSetOf<Deferred<Any?>>()
        with(view) {
            lifecycleOwner.lifecycleScope.launch {
                setupPreferenceAsync(
                    "playstore",
                    Ionicons.Icon.ion_android_playstore,
                    onClickListener = {
                        openWebsite(PLAYSTORE_URL, R.string.playstore_err)
                        true
                    }).let { deferreds.add(it) }
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
                    }).let { deferreds.add(it) }
                setupPreferenceAsync("telegram", onClickListener = {
                    openWebsite(TELEGRAM_URL, R.string.telegram_err)
                    true
                }).let { deferreds.add(it) }
                setupPreferenceAsync(
                    "github",
                    Ionicons.Icon.ion_social_github,
                    onClickListener = {
                        openWebsite(GITHUB_URL, R.string.browser_err)
                        true
                    }).let { deferreds.add(it) }
                setupPreferenceAsync(
                    "twitter",
                    Ionicons.Icon.ion_social_twitter,
                    onClickListener = {
                        openWebsite(TWITTER_URL, R.string.twitter_err)
                        true
                    }).let { deferreds.add(it) }
                setupPreferenceAsync(
                    "linkedin",
                    Ionicons.Icon.ion_social_linkedin,
                    onClickListener = {
                        openWebsite(LINKEDIN_URL, R.string.browser_err)
                        true
                    }).let { deferreds.add(it) }
                setupPreferenceAsync(
                    Preferences.ANALYTICS_ENABLED,
                    Ionicons.Icon.ion_arrow_graph_up_right,
                    onChangeListener = this@with,
                    action = { firebaseAnalyticsPreference = WeakReference(it) }
                ).let { deferreds.add(it) }
                deferreds.awaitAll()
            }
        }
    }

    private fun setupPreferenceAsync(
        name: String,
        icon: IIcon? = null,
        onClickListener: ((Preference) -> Boolean)? = null,
        onChangeListener: Preference.OnPreferenceChangeListener? = null,
        action: ((Preference) -> Unit)? = null
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
            action.notNull { action ->
                action(it)
            }
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

/*
setupPreference("share", Ionicons.Icon.ion_android_share, {
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
                })
 */