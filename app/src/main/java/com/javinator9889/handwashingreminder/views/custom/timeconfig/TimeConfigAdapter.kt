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
 * Created by Javinator9889 on 18/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.views.custom.timeconfig

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.listeners.ViewHolder

class TimeConfigAdapter(
    private val dataset: Array<TimeConfigContent>,
    private val listener: ViewHolder.OnItemClickListener?,
    private val fromActivity: AppCompatActivity?,
    private val viewItems: HashMap<Int, RecyclerView.ViewHolder>?
) :
    RecyclerView.Adapter<TimeConfigViewHolder>() {
    private var height = 0
    private lateinit var context: Context
//    private val items: ArrayList<TimeConfigViewHolder> = ArrayList(3)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeConfigViewHolder {
        context = parent.context
        val timeConfig = LayoutInflater.from(parent.context)
            .inflate(R.layout.time_card_view, parent, false)
        height = parent.measuredHeight / 3
//        height = cHeight - navbarHeight()
        //        if (!items.contains(viewHolder))
//            items.add(viewHolder)
        return TimeConfigViewHolder(timeConfig)
    }

    override fun onBindViewHolder(holder: TimeConfigViewHolder, position: Int) {
        holder.bind(
            dataset[position].title,
            dataset[position].id,
            listener,
            height
        )
        viewItems?.set(dataset[position].id.toInt(), holder)
    }

    override fun getItemCount(): Int = dataset.size

    private fun frameLayoutHeight(): Int {
        return fromActivity?.resources?.getDimensionPixelSize(
            com.github.paolorotolo.appintro.R.dimen.appIntro2BottomBarHeight
        ) ?: return 0
    }

    private fun navbarHeight(): Int {
        val resources = fromActivity?.resources ?: return 0
        val resId = resources.getIdentifier(
            "navigation_bar_height", "dimen", "android"
        )
        return if (resId > 0) resources.getDimensionPixelSize(resId) else 0
    }
}