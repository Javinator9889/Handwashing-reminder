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
 * Created by Javinator9889 on 13/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.fragments.washinghands

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import kotlinx.android.synthetic.main.how_to_wash_hands_layout.view.*
import kotlinx.android.synthetic.main.privacy_terms.*
import timber.log.Timber

internal const val NUM_PAGES = 7

class WashingHandsFragment : BaseFragmentView() {
    override val layoutId: Int = R.layout.how_to_wash_hands_layout
    private val items = arrayOfNulls<Fragment>(NUM_PAGES)
//    private val viewModel: VideoModel by viewModels()
//    private var files: List<File> = emptyList()

//
//    override fun onBackPressed() {
//        if (pager.currentItem == 0)
//            super.onBackPressed()
//        else
//            --pager.currentItem
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = FragmentAdapter(requireActivity())
        view.pager.adapter = adapter
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.d("Visibility changed: $hidden")
        items[pager.currentItem]?.onHiddenChanged(hidden)
    }

    private inner class FragmentAdapter(fa: FragmentActivity) :
        FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            with(SliderView(position)) {
                items[position] = this
                return this
            }
        }
    }
}