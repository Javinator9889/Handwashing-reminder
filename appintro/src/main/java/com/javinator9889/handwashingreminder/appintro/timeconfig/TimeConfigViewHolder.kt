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

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.javinator9889.handwashingreminder.appintro.R
import com.javinator9889.handwashingreminder.graphics.GlideApp
import com.javinator9889.handwashingreminder.listeners.ViewHolder
import com.javinator9889.handwashingreminder.utils.TimeConfig
import com.mikepenz.iconics.view.IconicsImageView
import timber.log.Timber


class TimeConfigViewHolder(private val view: View) :
    RecyclerView.ViewHolder(view),
    View.OnClickListener {
    val title: TextView = view.findViewById(R.id.title)
    val hours: TextView = view.findViewById(R.id.hours)
    val ddot: TextView = view.findViewById(R.id.ddot)
    val minutes: TextView = view.findViewById(R.id.minutes)
    val image: ImageView = view.findViewById(R.id.infoImage)
    val clockIcon: IconicsImageView = view.findViewById(R.id.clockIcon)
    private val card = view.findViewById<MaterialCardView>(R.id.timeCard)
    private var listener: ViewHolder.OnItemClickListener? = null
    private var height: Int? = null
    private lateinit var content: TimeConfigContent
    var id = 0L

    init {
        card.setOnClickListener(this)
        title.setOnClickListener(this)
        hours.setOnClickListener(this)
        minutes.setOnClickListener(this)
        image.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            card, title, hours, minutes, image -> {
                saveContentToTextViews()
                this.listener?.onItemClick(
                    this,
                    card,
                    adapterPosition,
                    id
                )
            }
        }
    }


    fun bind(
        title: CharSequence,
        id: Long,
        listener: ViewHolder.OnItemClickListener?,
        height: Int?,
        content: TimeConfigContent
    ) {
        this.id = id
        this.title.text = title
        this.listener = listener
        this.height = height
        this.content = content
        val imageRes = when (id) {
            TimeConfig.BREAKFAST_ID -> R.drawable.ic_breakfast
            TimeConfig.LUNCH_ID -> R.drawable.ic_lunch
            TimeConfig.DINNER_ID -> R.drawable.ic_dinner
            else -> null
        }
        adaptCardHeight()
        loadImageView(imageRes)
        loadContentToTextViews()
    }

    fun loadContentToTextViews() {
        hours.text = content.hours
        minutes.text = content.minutes
    }

    fun saveContentToTextViews() {
        content.hours = hours.text.toString()
        content.minutes = minutes.text.toString()
    }

    private fun adaptCardHeight() {
        if (height == null)
            return
        card.minimumHeight = height as Int
    }

    private fun loadImageView(@DrawableRes imageRes: Int?) {
        if (imageRes != null)
            try {
                GlideApp.with(view)
                    .load(imageRes)
                    .centerInside()
                    .into(image)
            } catch (e: Exception) {
                Timber.e(e, "Error while loading Glide view")
                image.setImageResource(imageRes)
            }
    }
}