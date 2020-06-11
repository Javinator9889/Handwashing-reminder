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
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.core.content.edit
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
import com.javinator9889.handwashingreminder.activities.views.fragments.washinghands.WashingHandsFragment
import com.javinator9889.handwashingreminder.custom.libraries.AppRate
import com.javinator9889.handwashingreminder.data.MainActivityDataHandler
import com.javinator9889.handwashingreminder.firebase.Auth
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.isDebuggable
import com.javinator9889.handwashingreminder.utils.notNull
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.how_to_wash_hands_layout.*
import kotlinx.coroutines.*
import timber.log.Timber
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig


class MainActivity : ActionBarBase(),
    BottomNavigationView.OnNavigationItemSelectedListener {
    override val layoutId: Int = R.layout.activity_main
    private val dataHandler = MainActivityDataHandler()

    init {
        lifecycleScope.launch {
            whenCreated {
                val deferreds = mutableSetOf<Deferred<Any>>()
                with(Firebase.remoteConfig) { fetchAndActivate() }
                deferreds.add(async {
                    dataHandler.setMenuIcons(menu, this@MainActivity)
                })
                menu.setOnNavigationItemSelectedListener(this@MainActivity)
                deferreds.add(async { loadTutorial() })
                deferreds.add(async { suggestRating() })
                deferreds.awaitAll()
            }
            whenStarted {
                with(FirebaseAnalytics.getInstance(this@MainActivity)) {
                    setCurrentScreen(this@MainActivity, "Main view", null)
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        savedInstanceState.notNull {
            Timber.d("Activity recreated")
        }
        if (savedInstanceState == null)
            dataHandler.loadFragmentView(supportFragmentManager)
    }

    override fun onDestroy() {
        dataHandler.clear()
        super.onDestroy()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        dataHandler.onRestoreInstanceState(
            savedInstanceState,
            supportFragmentManager
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dataHandler.onSaveInstanceState(outState, supportFragmentManager)
    }

    override fun finish() {
        try {
            Auth.logout()
        } finally {
            super.finish()
        }
    }

    override fun onBackPressed() {
        when (dataHandler.activeFragmentId) {
            R.id.diseases -> {
                (dataHandler.activeFragment as DiseasesFragment).onBackPressed()
                super.onBackPressed()
                finish()
            }
            R.id.handwashing -> {
                val washingHandsFragment =
                    dataHandler.activeFragment as WashingHandsFragment
                if (washingHandsFragment.pager.currentItem != 0)
                    washingHandsFragment.pager.currentItem--
                else {
                    menu.selectedItemId = R.id.diseases
                    onItemSelected(R.id.diseases)
                }
            }
            else -> {
                menu.selectedItemId = R.id.diseases
                onItemSelected(R.id.diseases)
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

    private fun onItemSelected(@IdRes id: Int): Boolean {
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

    private fun loadFragment(@IdRes id: Int) {
        Timber.d("$id - ${dataHandler.activeFragmentId} | ${id == dataHandler.activeFragmentId}")
        if (id == dataHandler.activeFragmentId)
            return
        with(supportFragmentManager.beginTransaction()) {
            show(dataHandler[id])
            dataHandler.onShow(id)
            Timber.d("Showing fragment: ${dataHandler[id]}")
            hide(dataHandler.activeFragment)
            dataHandler.onHide(id)
            Timber.d("Hiding fragment: ${dataHandler.activeFragment}")
            dataHandler.activeFragmentId = id
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
}
