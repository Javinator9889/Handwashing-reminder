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

import android.util.SparseArray
import androidx.annotation.IdRes
import androidx.core.util.set
import androidx.fragment.app.Fragment
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.IDS
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.DiseasesFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.news.NewsFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.settings.SettingsView
import com.javinator9889.handwashingreminder.activities.views.fragments.washinghands.WashingHandsFragment
import java.lang.ref.WeakReference

class MainActivityDataHandler(@IdRes var activeFragmentId: Int = R.id.diseases) {
    private val fragments = SparseArray<WeakReference<Fragment>>(4)
    val activeFragment: Fragment
        get() = this[activeFragmentId]

    operator fun get(@IdRes id: Int): Fragment =
        fragments[id, null]?.get() ?: createFragmentForId(id)

    private fun createFragmentForId(@IdRes id: Int): Fragment {
        if (id !in IDS)
            throw IllegalArgumentException("id not in IDs")
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