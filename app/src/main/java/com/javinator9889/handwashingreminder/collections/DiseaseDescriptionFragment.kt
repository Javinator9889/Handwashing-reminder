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
import androidx.core.view.ViewCompat
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.views.viewmodels.ParsedHTMLText
import kotlinx.android.synthetic.main.disease_description.*

internal const val ARG_TITLE = "bundle:title"
internal const val ARG_SDESC = "bundle:description:short"
internal const val ARG_LDESC = "bundle:description:long"
internal const val ARG_PROVIDER = "bundle:provider"
internal const val ARG_WEBSITE = "bundle:website"

class DiseaseDescriptionFragment(
    private val parsedHTMLText: ParsedHTMLText,
    private val animId: Int
) : BaseFragmentView() {
    @get:LayoutRes
    override val layoutId: Int = R.layout.disease_description

    companion object Transitions {
        const val DISEASE_NAME = "detail:header:title"
        const val DISEASE_DESCRIPTION = "detail:short_description"
        const val DISEASE_ANIM = "detail:header:animation"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(ARG_TITLE, title.text)
        outState.putCharSequence(ARG_SDESC, shortDescription.text)
        outState.putCharSequence(ARG_LDESC, longDescription.text)
        outState.putCharSequence(ARG_PROVIDER, provider.text)
        outState.putCharSequence(ARG_WEBSITE, website.text)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        animatedView.setAnimation(animId)
        if (savedInstanceState != null) {
            title.text = savedInstanceState.getCharSequence(ARG_TITLE)
            shortDescription.text =
                savedInstanceState.getCharSequence(ARG_SDESC)
            longDescription.text = savedInstanceState.getCharSequence(ARG_LDESC)
            provider.text = savedInstanceState.getCharSequence(ARG_PROVIDER)
            website.text = savedInstanceState.getCharSequence(ARG_WEBSITE)
        } else {
            title.text = parsedHTMLText.name
            shortDescription.text = parsedHTMLText.shortDescription
            longDescription.text = parsedHTMLText.longDescription
            provider.text =
                getString(R.string.written_by, parsedHTMLText.provider)
            website.text =
                getString(R.string.available_at, parsedHTMLText.website)
        }
        ViewCompat.setTransitionName(title, DISEASE_NAME)
        ViewCompat.setTransitionName(shortDescription, DISEASE_DESCRIPTION)
        ViewCompat.setTransitionName(animatedView, DISEASE_ANIM)
    }
}