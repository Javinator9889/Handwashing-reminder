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
import androidx.annotation.LayoutRes
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.views.viewmodels.ParsedHTMLText
import kotlinx.android.synthetic.main.simple_text_view.*

internal const val ARG_SYMPTOMS = "bundle:symptoms"
internal const val ARG_PREVENTION = "bundle:prevention"

class DiseaseExtraInformationFragment(
    private val position: Int,
    private val parsedHTMLText: ParsedHTMLText
) : BaseFragmentView() {
    @get:LayoutRes
    override val layoutId: Int = R.layout.simple_text_view

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (position == 1)
            outState.putCharSequence(ARG_SYMPTOMS, text.text)
        else if (position == 2)
            outState.putCharSequence(ARG_PREVENTION, text.text)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null)
            text.text = when (position) {
                1 -> savedInstanceState.getCharSequence(
                    ARG_SYMPTOMS
                )
                2 -> savedInstanceState.getCharSequence(
                    ARG_PREVENTION
                )
                else -> ""
            }
        else
            text.text = when (position) {
                1 -> parsedHTMLText.symptoms
                2 -> parsedHTMLText.prevention
                else -> ""
            }
    }
}