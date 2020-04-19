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
package com.javinator9889.handwashingreminder.activities.views.fragments.diseases

import android.graphics.Color
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.support.ActionBarBase
import com.javinator9889.handwashingreminder.activities.views.viewmodels.ParsedHTMLText
import com.javinator9889.handwashingreminder.collections.DiseaseTextAdapter
import kotlinx.android.synthetic.main.disease_view_expanded.*
import kotlin.properties.Delegates

internal const val ARG_ANIMATION = "card:animation"
internal const val ARG_PARSED_TEXT = "text:HTML:parsed"

class DiseaseExpandedView : ActionBarBase() {
    override val layoutId: Int = R.layout.disease_view_expanded
    private var animId by Delegates.notNull<Int>()
    private var parsedHTMLText: ParsedHTMLText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar.setTitleTextColor(Color.BLACK)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        if (savedInstanceState != null || intent.extras != null) {
            val data = savedInstanceState ?: intent.extras
            animId = data!!.getInt(ARG_ANIMATION)
            parsedHTMLText = data.getParcelable(ARG_PARSED_TEXT)
            if (parsedHTMLText != null) {
                val adapter = DiseaseTextAdapter(this, animId, parsedHTMLText!!)
                pager.adapter = adapter
                TabLayoutMediator(diseaseInfoTab, pager) { tab, position ->
                    when (position) {
                        0 -> tab.text = getText(R.string.description)
                        1 -> tab.text = getText(R.string.symptoms)
                        2 -> tab.text = getText(R.string.prevention)
                    }
                }.attach()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_ANIMATION, animId)
        outState.putParcelable(ARG_PARSED_TEXT, parsedHTMLText)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}