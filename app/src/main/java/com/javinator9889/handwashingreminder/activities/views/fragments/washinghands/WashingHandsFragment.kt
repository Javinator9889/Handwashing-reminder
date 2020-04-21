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
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import kotlinx.android.synthetic.main.how_to_wash_hands_layout.view.*
import kotlinx.android.synthetic.main.privacy_terms.*
import timber.log.Timber
import java.lang.ref.WeakReference

internal const val NUM_PAGES = 8

class WashingHandsFragment : BaseFragmentView() {
    override val layoutId: Int = R.layout.how_to_wash_hands_layout
    private val items = arrayOfNulls<WeakReference<Fragment>>(NUM_PAGES)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = FragmentAdapter(requireActivity())
        view.pager.adapter = adapter
        TabLayoutMediator(view.tabPager, view.pager) { _, _ -> }.attach()
        view.pager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.d("Current position: $position")
                when (position) {
                    0 -> {
                        view.previousButton.visibility = View.INVISIBLE
                        view.nextButton.visibility = View.VISIBLE
                    }
                    NUM_PAGES - 1 -> {
                        view.nextButton.visibility = View.INVISIBLE
                        view.previousButton.visibility = View.VISIBLE
                    }
                    else -> {
                        view.previousButton.visibility = View.VISIBLE
                        view.nextButton.visibility = View.VISIBLE
                    }
                }
            }
        })
        view.previousButton.setOnClickListener { view.pager.currentItem-- }
        view.nextButton.setOnClickListener { view.pager.currentItem++ }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.d("Visibility changed: $hidden")
        items[pager.currentItem]?.get()?.onHiddenChanged(hidden)
    }

    private inner class FragmentAdapter(fa: FragmentActivity) :
        FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            if (position == 0) {
                return FirstSlide()
            }
            with(SliderView()) {
                val args = Bundle(1)
                args.putInt(ARG_POSITION, position - 1)
                this.arguments = args
                items[position] = WeakReference(this)
                return this
            }
        }
    }
}