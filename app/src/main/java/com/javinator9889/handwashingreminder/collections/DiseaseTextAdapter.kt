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
 * Created by Javinator9889 on 19/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.collections

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.ARG_ANIMATION_ID
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.ARG_HTML_TEXT
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.ARG_POSITION
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.DiseaseDescriptionFragment
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.DiseaseExtraInformationFragment
import com.javinator9889.handwashingreminder.activities.views.viewmodels.ParsedHTMLText

class DiseaseTextAdapter(
    fm: FragmentActivity,
    private val animId: Int,
    private val parsedHTMLText: ParsedHTMLText
) : FragmentStateAdapter(fm) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> DiseaseDescriptionFragment()
        1, 2 -> DiseaseExtraInformationFragment()
        else -> Fragment()
    }.apply {
        val bundle = Bundle(3)
        bundle.putInt(ARG_POSITION, position)
        bundle.putInt(ARG_ANIMATION_ID, animId)
        bundle.putParcelable(ARG_HTML_TEXT, parsedHTMLText)
        arguments = bundle
    }
}