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
package com.javinator9889.handwashingreminder.activities.views.viewmodels

import android.content.Context
import android.util.SparseArray
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.core.util.set
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.ARG_CURRENT_ITEM
import com.javinator9889.handwashingreminder.activities.IDS
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.DiseasesFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.news.NewsFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.SettingsView
import com.javinator9889.handwashingreminder.activities.views.fragments.washinghands.WashingHandsFragment
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

internal const val ARG_FRAGMENTS_ARRAY = "args:handle:fragments"

class MainActivityViewModel(private val handle: SavedStateHandle) :
    ViewModel() {
    private val fragments: SparseArray<WeakReference<Fragment>> =
        handle.get(ARG_FRAGMENTS_ARRAY)
            ?: SparseArray(4)

    @IdRes
    var activeFragmentId: Int =
        handle.get(ARG_CURRENT_ITEM) ?: R.id.diseases
        set(@IdRes id) {
            if (id !in IDS)
                throw IllegalArgumentException("id not in ids")
            field = id
            handle.set(ARG_CURRENT_ITEM, activeFragmentId)
        }
    val activeFragment: Fragment
        get() = loadFragment(activeFragmentId)

    init {
        for (id in IDS)
            createFragmentForId(id)
        handle.set(ARG_FRAGMENTS_ARRAY, fragments)
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

    fun loadFragment(@IdRes id: Int) =
        fragments[id, null]?.get() ?: createFragmentForId(id)

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

    companion object Factory : ViewModelAssistedFactory<MainActivityViewModel> {
        override fun create(handle: SavedStateHandle) =
            MainActivityViewModel(handle)
    }
}