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
 * Created by Javinator9889 on 9/06/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.data

import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.DiseasesFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.news.NewsFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.SettingsView
import com.javinator9889.handwashingreminder.activities.views.fragments.washinghands.WashingHandsFragment
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.ref.WeakReference

internal const val ARG_CURRENT_ITEM = "bundle:args:current_item"
internal val IDS =
    arrayOf(R.id.diseases, R.id.handwashing, R.id.news, R.id.settings)


class MainActivityDataHandler(@IdRes var activeFragmentId: Int = R.id.diseases) {
    private val fragments = SparseArray<WeakReference<Fragment>>(4)
    val activeFragment: Fragment
        get() = this[activeFragmentId]

    operator fun get(@IdRes id: Int): Fragment =
        fragments[id, null]?.get() ?: createFragmentForId(id)

    fun onSaveInstanceState(
        outState: Bundle,
        fragmentManager: FragmentManager
    ) {
        fragments.forEach { id, reference ->
            reference.get()?.let {
                fragmentManager.putFragment(outState, id.toString(), it)
            }
        }
        outState.putInt(ARG_CURRENT_ITEM, activeFragmentId)
    }

    fun onRestoreInstanceState(
        savedInstanceState: Bundle,
        fragmentManager: FragmentManager
    ) {
        for (id in IDS) {
            val restoredFragment =
                fragmentManager.getFragment(savedInstanceState, id.toString())
                    ?: createFragmentForId(id)
            fragments[id] = WeakReference(restoredFragment)
        }
        activeFragmentId = savedInstanceState.getInt(ARG_CURRENT_ITEM)
    }

    suspend fun setMenuIcons(
        view: BottomNavigationView,
        context: Context
    ) {
        view.menu.forEach { item: MenuItem ->
            when (item.itemId) {
                R.id.diseases -> IconicsDrawable(
                    context,
                    Ionicons.Icon.ion_ios_medkit
                )
                R.id.news -> IconicsDrawable(
                    context,
                    GoogleMaterial.Icon.gmd_chrome_reader_mode
                )
                R.id.settings -> IconicsDrawable(
                    context,
                    Ionicons.Icon.ion_android_settings
                )
                else -> null
            }?.run {
                withContext(Dispatchers.Main) {
                    item.icon = this@run
                }
            }
        }
    }

    fun loadFragmentView(fragmentManager: FragmentManager) {
        with(fragmentManager.beginTransaction()) {
            IDS.forEach { id ->
                get(id).also {
                    add(R.id.mainContent, it)
                    hide(it)
                }
            }
            show(activeFragment)
            commit()
        }
    }

    fun clear() = fragments.clear()

    private fun createFragmentForId(@IdRes id: Int): Fragment {
        if (id !in IDS)
            throw IllegalArgumentException("id not in IDs")
        Timber.d("Creating fragment for ID $id")
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