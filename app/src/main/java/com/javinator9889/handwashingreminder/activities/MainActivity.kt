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
import androidx.core.util.forEach
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.DiseasesFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.news.NewsFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.washinghands.WashingHandsFragment
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import javinator9889.localemanager.activity.BaseAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import kotlin.concurrent.thread

class MainActivity : BaseAppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {
    private val fragments: SparseArray<Fragment> = SparseArray(4)
    private lateinit var activeFragment: Fragment
    private lateinit var app: HandwashingApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = HandwashingApplication.getInstance()
        delegateMenuIcons(menu)
        fragments.apply {
            put(R.id.diseases, DiseasesFragment())
            put(R.id.handwashing, WashingHandsFragment())
            put(R.id.news, NewsFragment())
            put(R.id.settings, DiseasesFragment())
        }
        activeFragment = fragments[R.id.diseases]
        menu.setOnNavigationItemSelectedListener(this)
//        menu.setOnNavigationItemReselectedListener { onItemSelected(it.itemId) }
        initFragmentView()
    }

    protected fun delegateMenuIcons(menu: BottomNavigationView) {
        thread(start = true) {
            menu.menu.forEach { item ->
                val icon = when (item.itemId) {
                    R.id.diseases ->
                        IconicsDrawable(
                            this, GoogleMaterial.Icon.gmd_feedback
                        )
                    R.id.news ->
                        IconicsDrawable(
                            this, GoogleMaterial.Icon.gmd_chrome_reader_mode
                        )
                    R.id.settings ->
                        IconicsDrawable(
                            this, GoogleMaterial.Icon.gmd_settings
                        )
                    else -> null
                }
                icon?.let { runOnUiThread { item.icon = it } }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean =
        onItemSelected(item.itemId)

    protected fun onItemSelected(@IdRes id: Int): Boolean {
        return try {
            loadFragment(fragments[id])
            true
        } catch (e: Exception) {
            Timber.e(e, "Unexpected exception")
            false
        }
    }

    private fun initFragmentView() {
        with(supportFragmentManager.beginTransaction()) {
            fragments.forEach { _, fragment ->
                add(R.id.mainContent, fragment)
                hide(fragment)
            }
            show(activeFragment)
            commit()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        if (fragment == activeFragment)
            return
        with(supportFragmentManager.beginTransaction()) {
            show(fragment)
            hide(activeFragment)
            activeFragment = fragment
            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            disallowAddToBackStack()
        }.commit()
    }
}
