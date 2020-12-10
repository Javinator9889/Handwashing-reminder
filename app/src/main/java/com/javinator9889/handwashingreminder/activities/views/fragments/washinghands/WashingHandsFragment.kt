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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.base.LayoutVisibilityChange
import com.javinator9889.handwashingreminder.databinding.HowToWashHandsLayoutBinding
import timber.log.Timber
import java.lang.ref.WeakReference

internal const val NUM_PAGES = 8

class WashingHandsFragment : BaseFragmentView<HowToWashHandsLayoutBinding>(), LayoutVisibilityChange {
    override val layoutId: Int = R.layout.how_to_wash_hands_layout
    private val items = arrayOfNulls<WeakReference<Fragment>>(NUM_PAGES)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding = HowToWashHandsLayoutBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = FragmentAdapter(requireActivity())
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabPager, binding.pager) { _, _ -> }.attach()
        binding.pager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.d("Current position: $position")
                when (position) {
                    0 -> {
                        binding.previousButton.visibility = View.INVISIBLE
                        binding.nextButton.visibility = View.VISIBLE
                    }
                    NUM_PAGES - 1 -> {
                        binding.nextButton.visibility = View.INVISIBLE
                        binding.previousButton.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.previousButton.visibility = View.VISIBLE
                        binding.nextButton.visibility = View.VISIBLE
                    }
                }
            }
        })
        binding.previousButton.setOnClickListener { binding.pager.currentItem-- }
        binding.nextButton.setOnClickListener { binding.pager.currentItem++ }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.d("Visibility changed: $hidden")
        items[binding.pager.currentItem]?.get()?.onHiddenChanged(hidden)
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
                arguments = args
                items[position] = WeakReference(this)
                return this
            }
        }
    }

    override fun onVisibilityChanged(visibility: Int) {}
}