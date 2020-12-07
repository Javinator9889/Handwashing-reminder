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
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenResumed
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.support.ActionBarBase
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.DiseasesFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.news.NewsFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.washinghands.WashingHandsFragment
import com.javinator9889.handwashingreminder.custom.libraries.AppRate
import com.javinator9889.handwashingreminder.data.MainActivityDataHandler
import com.javinator9889.handwashingreminder.databinding.ActivityMainBinding
import com.javinator9889.handwashingreminder.firebase.Auth
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence


class MainActivity : ActionBarBase<ActivityMainBinding>(),
    BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener {
    override val layoutId: Int = R.layout.activity_main
    private val dataHandler = MainActivityDataHandler()
    private lateinit var deferredRating: Deferred<AppRate>
    private lateinit var deferredShowcase: Deferred<MaterialShowcaseSequence?>

    init {
        lifecycleScope.launch {
            whenCreated {
                with(Firebase.remoteConfig) { fetchAndActivate() }
                launch {
                    dataHandler.setMenuIcons(
                        binding.menu,
                        this@MainActivity
                    )
                }
                binding.menu.setOnNavigationItemSelectedListener(this@MainActivity)
                binding.menu.setOnNavigationItemReselectedListener(this@MainActivity)
                deferredShowcase = dataHandler.loadShowcaseAsync(
                    activity = this@MainActivity,
                    lifecycleOwner = this@MainActivity
                )
                deferredRating = dataHandler.suggestRatingAsync(
                    activity = this@MainActivity,
                    lifecycleOwner = this@MainActivity
                )
            }
            whenResumed {
                with(FirebaseAnalytics.getInstance(this@MainActivity)) {
                    setCurrentScreen(this@MainActivity, "Main view", null)
                }
                deferredShowcase.await()?.let {
                    withContext(Dispatchers.Main) {
                        it.start()
                    }
                }
                deferredRating.await().run {
                    withContext(Dispatchers.Main) {
                        init()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null)
            lifecycleScope.launch {
                dataHandler.loadFragmentView(supportFragmentManager)
            }
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
        } catch (e: IllegalStateException) {
            Timber.w(e, "Auth client was not initialized")
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
                if (washingHandsFragment.binding.pager.currentItem != 0)
                    washingHandsFragment.binding.pager.currentItem--
                else {
                    binding.menu.selectedItemId = R.id.diseases
                    onItemSelected(R.id.diseases)
                }
            }
            else -> {
                binding.menu.selectedItemId = R.id.diseases
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

    override fun onNavigationItemReselected(item: MenuItem) {
        if (item.itemId == R.id.news)
            with(dataHandler.activeFragment as NewsFragment) {
                goTop()
            }
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
        supportFragmentManager.commit {
            show(dataHandler[id])
            dataHandler.onShow(id)
            Timber.d("Showing fragment: ${dataHandler[id]}")
            hide(dataHandler.activeFragment)
            dataHandler.onHide(id)
            Timber.d("Hiding fragment: ${dataHandler.activeFragment}")
            dataHandler.activeFragmentId = id
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            disallowAddToBackStack()
        }
    }

    override fun inflateLayout(): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater).also { binding = it }
}
