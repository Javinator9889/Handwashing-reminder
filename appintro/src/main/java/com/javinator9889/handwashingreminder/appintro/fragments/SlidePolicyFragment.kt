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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.github.paolorotolo.appintro.AppIntroBaseFragment
import com.github.paolorotolo.appintro.ISlidePolicy
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.javinator9889.handwashingreminder.appintro.R

class SlidePolicyFragment : AppIntroBaseFragment(), ISlidePolicy {
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
        firebaseAnalytics = view.findViewById(R.id.firstSwitch)
        firebasePerformance = view.findViewById(R.id.secondSwitch)
        slidePolicyCheckBox = view.findViewById(R.id.policyCheckbox)
        val title = view.findViewById<TextView>(R.id.title)
        val image = view.findViewById<ImageView>(R.id.image)
        layout = view.findViewById(R.id.main)

        this.title?.let { title.text = it }
        this.titleColor?.let { title.setTextColor(it) }
        bgColor?.let { layout.setBackgroundColor(it) }
        imageDrawable?.let { image.setImageResource(it) }
        val text = getString(R.string.slide_policy_text, "Firebase")
        slidePolicyCheckBox.text = HtmlCompat.fromHtml(
            text,
            FROM_HTML_MODE_LEGACY
        )
        return view
    }

    override fun getLayoutId(): Int = R.layout.slide_policy

    override fun isPolicyRespected(): Boolean = slidePolicyCheckBox.isChecked

    override fun onUserIllegallyRequestedNextPage() {
        Snackbar.make(layout, R.string.accept_policy, Snackbar.LENGTH_LONG)
            .show()
    }
}