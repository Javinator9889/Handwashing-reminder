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
 * Created by Javinator9889 on 24/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.appintro.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.paolorotolo.appintro.AppIntroBaseFragment
import com.github.paolorotolo.appintro.ISlidePolicy
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.javinator9889.handwashingreminder.activities.PrivacyTermsActivity
import com.javinator9889.handwashingreminder.appintro.R
import com.javinator9889.handwashingreminder.utils.notNull
import kotlinx.android.synthetic.main.slide_policy.view.*
import com.javinator9889.handwashingreminder.R as RBase

class SlidePolicyFragment : AppIntroBaseFragment(), ISlidePolicy {
    companion object {
        const val FIREBASE_ANALYTICS_CHECKED = "switch:fa:status"
        const val FIREBASE_PERFORMANCE_CHECKED = "switch:fp:status"
        const val PRIVACY_TERMS_CHECKED = "checkbox:pr_tos:status"
    }

    private lateinit var layout: ConstraintLayout
    private lateinit var firebaseAnalytics: SwitchMaterial
    private lateinit var firebasePerformance: SwitchMaterial
    private lateinit var slidePolicyCheckBox: MaterialCheckBox
    var title: String? = null
    var titleColor: Int? = null
    var imageDrawable: Int? = null
    var bgColor: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layoutId, container, false)
        firebaseAnalytics = view.firstSwitch
        firebasePerformance = view.secondSwitch
        slidePolicyCheckBox = view.policyCheckbox
        layout = view.main

        val image = view.image
        val title = view.title
        this.title?.let { title.text = it }
        this.titleColor?.let { title.setTextColor(it) }
        bgColor?.let { layout.setBackgroundColor(it) }
        imageDrawable?.let { image.setImageResource(it) }
        slidePolicyCheckBox.text = getString(R.string.slide_policy_text)
        slidePolicyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val intent = Intent(context, PrivacyTermsActivity::class.java)
                startActivity(intent)
            }
        }
        firebaseAnalytics.text =
            getString(RBase.string.firebase_analytics_policy)
        firebasePerformance.text =
            getString(RBase.string.firebase_performance_policy)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putBoolean(FIREBASE_ANALYTICS_CHECKED, firebaseAnalytics.isChecked)
            putBoolean(
                FIREBASE_PERFORMANCE_CHECKED,
                firebasePerformance.isChecked
            )
            putBoolean(PRIVACY_TERMS_CHECKED, slidePolicyCheckBox.isChecked)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        savedInstanceState.notNull {
            firebaseAnalytics.isChecked = it.getBoolean(
                FIREBASE_ANALYTICS_CHECKED, false
            )
            firebasePerformance.isChecked = it.getBoolean(
                FIREBASE_PERFORMANCE_CHECKED, false
            )
            slidePolicyCheckBox.isChecked = it.getBoolean(
                PRIVACY_TERMS_CHECKED, false
            )
        }
    }

    override fun getLayoutId(): Int = R.layout.slide_policy

    override fun isPolicyRespected(): Boolean = slidePolicyCheckBox.isChecked

    override fun onUserIllegallyRequestedNextPage() {
        Snackbar.make(layout, R.string.accept_policy, Snackbar.LENGTH_LONG)
            .show()
    }
}