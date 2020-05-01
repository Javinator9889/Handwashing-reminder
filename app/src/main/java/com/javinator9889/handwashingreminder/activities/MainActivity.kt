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
 * Created by Javinator9889 on 15/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities

import android.os.Bundle
import android.util.SparseArray
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.core.content.edit
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.metrics.AddTrace
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.support.ActionBarBase
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.DiseasesFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.news.NewsFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.SettingsView
import com.javinator9889.handwashingreminder.activities.views.fragments.washinghands.WashingHandsFragment
import com.javinator9889.handwashingreminder.custom.libraries.AppRate
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.isDebuggable
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.how_to_wash_hands_layout.*
import timber.log.Timber
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import java.lang.ref.WeakReference
import kotlin.concurrent.thread
import kotlin.properties.Delegates

internal const val ARG_CURRENT_ITEM = "bundle:args:current_item"

class MainActivity : ActionBarBase(),
    BottomNavigationView.OnNavigationItemSelectedListener {
    override val layoutId: Int = R.layout.activity_main
    private val fragments: SparseArray<WeakReference<Fragment>> = SparseArray(4)
    private var activeFragment by Delegates.notNull<@IdRes Int>()

    @AddTrace(name = "onCreateMainView")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(FirebaseAnalytics.getInstance(this)) {
            setCurrentScreen(this@MainActivity, "Main view", null)
        }
        with(Firebase.remoteConfig) {
            fetchAndActivate()
        }
        delegateMenuIcons(menu)
        val ids =
            arrayOf(R.id.diseases, R.id.handwashing, R.id.news, R.id.settings)
        menu.setOnNavigationItemSelectedListener(this)
        loadTutorial()
        suggestRating()
        if (savedInstanceState != null) {
            for (id in ids) {
                val fragment = supportFragmentManager.getFragment(
                    savedInstanceState,
                    id.toString()
                ) ?: createFragmentForId(id)
                fragments[id] = WeakReference(fragment)
            }
            activeFragment = savedInstanceState.getInt(ARG_CURRENT_ITEM)
        } else {
            for (id in ids)
                createFragmentForId(id)
            activeFragment = R.id.diseases
            initFragmentView()
        }
    }

    protected fun delegateMenuIcons(menu: BottomNavigationView) {
        thread(start = true) {
            menu.menu.forEach { item ->
                val icon = when (item.itemId) {
                    R.id.diseases ->
                        IconicsDrawable(
                            this, Ionicons.Icon.ion_ios_medkit
                        )
                    R.id.news ->
                        IconicsDrawable(
                            this, GoogleMaterial.Icon.gmd_chrome_reader_mode
                        )
                    R.id.settings ->
                        IconicsDrawable(
                            this, Ionicons.Icon.ion_android_settings
                        )
                    else -> null
                }
                icon?.let { runOnUiThread { item.icon = it } }
            }
        }
    }

    override fun onBackPressed() {
        if (activeFragment != R.id.diseases &&
            activeFragment != R.id.handwashing
        ) {
            menu.selectedItemId = R.id.diseases
            onNavigationItemSelected(menu.menu.findItem(R.id.diseases))
        } else {
            if (activeFragment == R.id.diseases) {
                with(fragments[activeFragment].get()!! as DiseasesFragment) {
                    onBackPressed()
                }
                fragments.clear()
                super.onBackPressed()
                finish()
            } else {
                val washingHandsFragment = fragments[activeFragment].get()
                    ?: createFragmentForId(R.id.handwashing)
                            as WashingHandsFragment
                if (washingHandsFragment.pager.currentItem != 0)
                    washingHandsFragment.pager.currentItem--
                else {
                    menu.selectedItemId = R.id.diseases
                    onNavigationItemSelected(menu.menu.findItem(R.id.diseases))
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val screenTitle = when (item.itemId) {
            R.id.diseases -> "diseases"
            R.id.handwashing -> "handwashing"
            R.id.news -> "news"
            R.id.settings -> "settings"
            else -> "Main view"
        }
        with(FirebaseAnalytics.getInstance(this)) {
            setCurrentScreen(this@MainActivity, screenTitle, null)
        }
        return onItemSelected(item.itemId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragments.forEach { id, reference ->
            reference.get()?.let {
                supportFragmentManager.putFragment(
                    outState, id.toString(), it
                )
            }
        }
        outState.putInt(ARG_CURRENT_ITEM, activeFragment)
    }

    protected fun onItemSelected(@IdRes id: Int): Boolean {
        return try {
            loadFragment(id)
            if (id == R.id.handwashing)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            else
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            true
        } catch (e: Exception) {
            Timber.e(e, "Unexpected exception")
            false
        }
    }

    private fun initFragmentView() {
        with(supportFragmentManager.beginTransaction()) {
            fragments.forEach { id, reference ->
                val fragment = reference.get() ?: createFragmentForId(id)
                add(R.id.mainContent, fragment)
                hide(fragment)
            }
            show(
                fragments[activeFragment].get() ?: createFragmentForId(
                    activeFragment
                )
            )
            commit()
        }
    }

    private fun loadFragment(@IdRes id: Int) {
        if (id == activeFragment)
            return
        val fragment = fragments[id].get() ?: return
        val displayedFragment = fragments[activeFragment].get()!!
        with(supportFragmentManager.beginTransaction()) {
            show(fragment)
            hide(displayedFragment)
            activeFragment = id
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            disallowAddToBackStack()
        }.commit()
    }

    private fun loadTutorial() {
        val preferences =
            with(PreferenceManager.getDefaultSharedPreferences(this)) {
                if (getBoolean(Preferences.INITIAL_TUTORIAL_DONE, false))
                    return
                else this
            }
        val config = ShowcaseConfig()
        config.delay = 500L
        with(MaterialShowcaseSequence(this)) {
            setConfig(config)
            val dismissText = getString(R.string.got_it)
            val diseasesText = getString(R.string.diseases_intro)
            val handwashingText = getString(R.string.handwashing_intro)
            val newsText = getString(R.string.news_intro)
            val settingsText = getString(R.string.settings_intro)
            addSequenceItem(
                findViewById(R.id.diseases),
                diseasesText,
                dismissText
            )
            addSequenceItem(
                findViewById(R.id.handwashing),
                handwashingText,
                dismissText
            )
            addSequenceItem(
                findViewById(R.id.news),
                newsText,
                dismissText
            )
            addSequenceItem(
                findViewById(R.id.settings),
                settingsText,
                dismissText
            )
            var itemCount = 0
            setOnItemDismissedListener { _, _ ->
                if (itemCount++ == 3)
                    preferences.edit {
                        putBoolean(Preferences.INITIAL_TUTORIAL_DONE, true)
                    }
            }
            start()
        }
    }

    private fun suggestRating() {
        with(AppRate(this)) {
            if (!isDebuggable()) {
                setMinDaysUntilPrompt(2L)
                setMinLaunchesUntilPrompt(5)
            }
            dialogTitle = R.string.rate_text_title
            dialogMessage = R.string.rate_app_message
            positiveButtonText = R.string.rate_text
            negativeButtonText = R.string.rate_do_not_show
            init()
        }
    }

    private fun createFragmentForId(@IdRes id: Int): Fragment {
        val fragment = when (id) {
            R.id.diseases -> DiseasesFragment()
            R.id.handwashing -> WashingHandsFragment()
            R.id.news -> NewsFragment()
            R.id.settings -> SettingsView()
            else -> Fragment()  // this should never happen
        }
        fragments[id] = WeakReference(fragment)
        return fragment
    }
}
