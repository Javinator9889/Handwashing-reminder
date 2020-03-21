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
package com.javinator9889.handwashingreminder.views.custom

import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.javinator9889.handwashingreminder.R


class TimeConfigViewHolder(val view: View) :
    RecyclerView.ViewHolder(view),
    View.OnClickListener {
    val title: TextView = view.findViewById(R.id.title)
    val hours: TextView = view.findViewById(R.id.hours)
    val minutes: TextView = view.findViewById(R.id.minutes)
    private val card = view.findViewById<MaterialCardView>(R.id.timeCard)
    private val container = view.findViewById<ConstraintLayout>(R.id.cardCtr)
    private var listener: AdapterView.OnItemClickListener? = null
    private var height: Int? = null
    var id = 0L

    init {
        card.setOnClickListener(this)
        title.setOnClickListener(this)
        hours.setOnClickListener(this)
        minutes.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            card, title, hours, minutes -> this.listener?.onItemClick(
                null,
                card,
                adapterPosition,
                id
            )
        }
    }

    fun bind(
        title: CharSequence,
        id: Long,
        listener: AdapterView.OnItemClickListener?,
        height: Int?
    ) {
        this.id = id
        this.title.text = title
        this.listener = listener
        this.height = height
        adaptCardHeight()
    }

    private fun adaptCardHeight() {
        if (height == null)
            return
        card.minimumHeight = height!! / 3
    }
}