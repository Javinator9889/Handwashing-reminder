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

import android.util.SparseArray
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.core.content.edit
import androidx.core.util.set
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenStarted
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.support.ActionBarBase
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.DiseasesFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.news.NewsFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.SettingsView
import com.javinator9889.handwashingreminder.activities.views.fragments.washinghands.WashingHandsFragment
import com.javinator9889.handwashingreminder.activities.views.viewmodels.MainActivityViewModel
import com.javinator9889.handwashingreminder.activities.views.viewmodels.SavedViewModelFactory
import com.javinator9889.handwashingreminder.custom.libraries.AppRate
import com.javinator9889.handwashingreminder.firebase.Auth
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.isDebuggable
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.how_to_wash_hands_layout.*
import kotlinx.coroutines.*
import timber.log.Timber
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import java.lang.ref.WeakReference

internal const val ARG_CURRENT_ITEM = "bundle:args:current_item"
internal val IDS =
    arrayOf(R.id.diseases, R.id.handwashing, R.id.news, R.id.settings)

class MainActivity : ActionBarBase(),
    BottomNavigationView.OnNavigationItemSelectedListener {
    override val layoutId: Int = R.layout.activity_main
    private val fragments: SparseArray<WeakReference<Fragment>> = SparseArray(4)
//    private var activeFragment by Delegates.notNull<@IdRes Int>()
    private val activityViewModel by viewModels<MainActivityViewModel> {
        SavedViewModelFactory(MainActivityViewModel.Factory, this)
    }

    init {
        lifecycleScope.launch {
            whenCreated {
                val deferreds = mutableSetOf<Deferred<Any>>()
                with(Firebase.remoteConfig) { fetchAndActivate() }
                deferreds.add(async {
                    activityViewModel.setMenuIcons(menu, this@MainActivity)
                })
//                deferreds.add(async { delegateMenuIcons(menu) })
                menu.setOnNavigationItemSelectedListener(this@MainActivity)
                deferreds.add(async { loadTutorial() })
                deferreds.add(async { suggestRating() })
                /*for (id in IDS)
                    deferreds.add(async { createFragmentForId(id) })
                activeFragment = R.id.diseases*/
                deferreds.awaitAll()
            }
            whenStarted {
                with(FirebaseAnalytics.getInstance(this@MainActivity)) {
                    setCurrentScreen(this@MainActivity, "Main view", null)
                }
                withContext(Dispatchers.Main) {
                    initFragmentView()
                }
            }
        }
    }

    /*override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.notNull {
            for (id in IDS) {
                val fragment = supportFragmentManager.getFragment(
                    savedInstanceState, id.toString()
                ) ?: createFragmentForId(id)
                fragments[id] = WeakReference(fragment)
            }
            activeFragment = savedInstanceState.getInt(ARG_CURRENT_ITEM)
        }
    }*/

    override fun onDestroy() {
        try {
            Auth.logout()
        } finally {
            super.onDestroy()
        }
    }

    private suspend fun delegateMenuIcons(menu: BottomNavigationView) {
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
            withContext(Dispatchers.Main) {
                icon?.let { item.icon = it }
            }
        }
    }

    override fun onBackPressed() {
        if (activityViewModel.activeFragmentId != R.id.diseases &&
            activityViewModel.activeFragmentId != R.id.handwashing
        ) {
            menu.selectedItemId = R.id.diseases
            onNavigationItemSelected(menu.menu.findItem(R.id.diseases))
        } else {
            if (activityViewModel.activeFragmentId == R.id.diseases) {
                with(activityViewModel.activeFragment as DiseasesFragment) {
                    onBackPressed()
                }
                fragments.clear()
                super.onBackPressed()
                finish()
            } else {
                val washingHandsFragment =
                    activityViewModel.activeFragment as WashingHandsFragment
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

    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragments.forEach { id, reference ->
            reference.get()?.let {
                supportFragmentManager.putFragment(
                    outState, id.toString(), it
                )
            }
        }
        outState.putInt(ARG_CURRENT_ITEM, activeFragment)
    }*/

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
            for (id in IDS) {
                activityViewModel.loadFragment(id).also {
                    if (supportFragmentManager.findFragmentByTag(id.toString()) == null) {
                        add(R.id.mainContent, it, id.toString())
                        hide(it)
                    }
                }
            }
            show(activityViewModel.activeFragment)
            commit()
        }
    }

    private fun loadFragment(@IdRes id: Int) {
        if (id == activityViewModel.activeFragmentId)
            return
        with(supportFragmentManager.beginTransaction()) {
            show(activityViewModel.loadFragment(id))
            hide(activityViewModel.activeFragment)
            activityViewModel.activeFragmentId = id
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            disallowAddToBackStack()
        }.commit()
    }

    private suspend fun loadTutorial() {
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
            withContext(Dispatchers.Main) {
                start()
            }
        }
    }

    private suspend fun suggestRating() {
        withContext(Dispatchers.Main) {
            with(AppRate(this@MainActivity)) {
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
