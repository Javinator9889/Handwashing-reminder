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
 * Created by Javinator9889 on 12/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.fragments.diseases.adapter

import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.RawRes
import com.google.android.material.card.MaterialCardView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.views.viewmodels.ParsedHTMLText
import com.javinator9889.handwashingreminder.graphics.LottieAdaptedPerformanceAnimationView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

data class Disease(
    @RawRes val animation: Int,
    val information: ParsedHTMLText,
    @LayoutRes override val layoutRes: Int,
    override val type: Int
) : AbstractItem<Disease.ViewHolder>() {

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(private val view: View) :
        FastAdapter.ViewHolder<Disease>(view) {
        val cardContainer: MaterialCardView =
            view.findViewById(R.id.cardDiseaseContainer)
        private val title: TextView = view.findViewById(R.id.title)
        private val description: TextView =
            view.findViewById(R.id.shortDescription)
        private val animation: LottieAdaptedPerformanceAnimationView =
            view.findViewById(R.id.image)

        override fun bindView(item: Disease, payloads: List<Any>) {
            title.text = item.information.name
            description.text = item.information.shortDescription
            animation.setAnimation(item.animation)
        }

        override fun unbindView(item: Disease) {
            title.text = null
            description.text = null
            animation.clearAnimation()
        }
    }
}