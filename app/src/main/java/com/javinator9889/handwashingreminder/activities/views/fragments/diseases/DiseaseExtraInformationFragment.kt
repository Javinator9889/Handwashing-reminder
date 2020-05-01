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

import android.os.Bundle
import androidx.annotation.LayoutRes
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.views.viewmodels.ParsedHTMLText
import kotlinx.android.synthetic.main.simple_text_view.*
import kotlin.properties.Delegates

internal const val ARG_SYMPTOMS = "bundle:symptoms"
internal const val ARG_PREVENTION = "bundle:prevention"
internal const val ARG_POSITION = "bundle:item:position"

class DiseaseExtraInformationFragment : BaseFragmentView() {
    @get:LayoutRes
    override val layoutId: Int = R.layout.simple_text_view
    private var position by Delegates.notNull<Int>()
    private lateinit var parsedHTMLText: ParsedHTMLText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (position == 1)
            outState.putCharSequence(ARG_SYMPTOMS, text.text)
        else if (position == 2)
            outState.putCharSequence(ARG_PREVENTION, text.text)
        outState.putInt(ARG_POSITION, position)
        outState.putParcelable(ARG_HTML_TEXT, parsedHTMLText)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null || arguments != null) {
            val data = (savedInstanceState ?: arguments)!!
            position = data.getInt(ARG_POSITION)
            parsedHTMLText = data.getParcelable(ARG_HTML_TEXT)!!
            text.text = when (position) {
                1 -> data.getCharSequence(ARG_SYMPTOMS)
                    ?: parsedHTMLText.symptoms
                2 -> data.getCharSequence(ARG_PREVENTION)
                    ?: parsedHTMLText.prevention
                else -> ""
            }
        }
    }
}