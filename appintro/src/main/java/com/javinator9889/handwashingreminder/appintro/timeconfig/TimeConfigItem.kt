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
 * Created by Javinator9889 on 26/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.appintro.timeconfig

import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.cardview.widget.CardView
import com.javinator9889.handwashingreminder.appintro.R
import com.javinator9889.handwashingreminder.graphics.GlideApp
import com.javinator9889.handwashingreminder.graphics.RecyclingImageView
import com.javinator9889.handwashingreminder.utils.TimeConfig
import com.javinator9889.handwashingreminder.utils.notNull
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.iconics.view.IconicsImageView

class TimeConfigItem(
    val title: CharSequence,
    val id: Long,
    var hours: String? = "",
    var minutes: String? = ""
) : AbstractItem<TimeConfigItem.ViewHolder>() {
    @LayoutRes
    override val layoutRes: Int = R.layout.time_card_view
    override val type: Int = R.id.timeCard

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(private val view: View) :
        FastAdapter.ViewHolder<TimeConfigItem>(view) {
        private val title: TextView = view.findViewById(R.id.title)
        private val hours: TextView = view.findViewById(R.id.hours)
        private val ddot: TextView = view.findViewById(R.id.ddot)
        private val minutes: TextView = view.findViewById(R.id.minutes)
        private val image: RecyclingImageView = view.findViewById(R.id.infoImage)
        private val clockIcon: IconicsImageView = view.findViewById(R.id.clockIcon)
        val cardView: CardView = view.findViewById(R.id.timeCard)

        override fun bindView(item: TimeConfigItem, payloads: List<Any>) {
            title.text = item.title
            hours.text = item.hours
            minutes.text = item.minutes
            ddot.text = view.context.getString(R.string.double_dot)
            clockIcon.icon =
                IconicsDrawable(view.context, Ionicons.Icon.ion_android_time)
                    .apply { sizeDp = 16 }
            when (item.id) {
                TimeConfig.BREAKFAST_ID -> R.drawable.ic_breakfast
                TimeConfig.LUNCH_ID -> R.drawable.ic_lunch
                TimeConfig.DINNER_ID -> R.drawable.ic_dinner
                else -> null
            }.notNull {
                GlideApp.with(view)
                    .load(it)
                    .centerInside()
                    .into(image)
                image.savedDrawableRes = it
            }
        }

        override fun unbindView(item: TimeConfigItem) {
            title.text = null
            hours.text = null
            ddot.text = null
            minutes.text = null
            image.onDetachedFromWindow()
            clockIcon.icon = null
        }
    }
}