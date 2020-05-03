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
import android.widget.FrameLayout
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.utils.notNull
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class Ads : AbstractItem<Ads.ViewHolder>() {
    override val layoutRes: Int = R.layout.simple_frame_view
    override val type: Int = R.id.adsContainer

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(v: View) : FastAdapter.ViewHolder<Ads>(v) {
        private val ads = HandwashingApplication.instance.adLoader
        private val container = v.findViewById<FrameLayout>(R.id.adsContainer)

        override fun bindView(item: Ads, payloads: List<Any>) {
            ads.notNull {
                it.loadAdForViewGroup(container, false)
            }
        }

        override fun unbindView(item: Ads) {
            ads.notNull {
                it.destroy()
            }
        }
    }
}