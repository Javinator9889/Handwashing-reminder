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
 * Created by Javinator9889 on 30/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities

import android.graphics.Color
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.support.ActionBarBase
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.collections.PrivacyTermsCollectionAdapter

class PrivacyTermsActivity : ActionBarBase() {
    override val layoutId: Int = R.layout.privacy_terms
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(Color.BLACK)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        with(HandwashingApplication.getInstance()) {
            val bundle = Bundle(1).apply { putString("view", "privacy") }
            firebaseAnalytics.logEvent(
                FirebaseAnalytics.Event.VIEW_ITEM, bundle
            )
        }

        tabLayout = findViewById(R.id.privacy_terms_tab)
        viewPager = findViewById(R.id.pager)
        val adapter = PrivacyTermsCollectionAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.privacy_policy_title)
                1 -> tab.text = getString(R.string.tos_title)
            }
        }.attach()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}