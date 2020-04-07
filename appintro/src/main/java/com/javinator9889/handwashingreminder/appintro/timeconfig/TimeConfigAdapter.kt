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
package com.javinator9889.handwashingreminder.appintro.timeconfig

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import com.javinator9889.handwashingreminder.appintro.R
import com.javinator9889.handwashingreminder.listeners.ViewHolder

class TimeConfigAdapter(
    private val dataset: Array<TimeConfigContent>,
    private val listener: ViewHolder.OnItemClickListener?,
    private val viewItems: SparseArray<TimeConfigViewHolder>
) :
    RecyclerView.Adapter<TimeConfigViewHolder>() {
    private var height = 0
    private lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeConfigViewHolder {
        context = parent.context
        val timeConfig = LayoutInflater.from(parent.context)
            .inflate(R.layout.time_card_view, parent, false)
        height = parent.measuredHeight / 3
        return TimeConfigViewHolder(timeConfig)
    }

    override fun onBindViewHolder(holder: TimeConfigViewHolder, position: Int) {
        holder.bind(
            dataset[position].title,
            dataset[position].id,
            listener,
            height,
            dataset[position]
        )
        viewItems[dataset[position].id.toInt()] = holder
    }

    override fun getItemCount(): Int = dataset.size
}