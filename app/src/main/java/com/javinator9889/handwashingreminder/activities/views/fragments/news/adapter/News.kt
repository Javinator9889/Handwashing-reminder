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
 * Created by Javinator9889 on 3/06/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.fragments.news.adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.graphics.GlideApp
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import java.text.DateFormat
import java.util.*

data class News(
    val title: String,
    val short: String,
    val url: String,
    val publishDate: Date?,
    val imageUrl: String?,
    val website: String?,
    val websiteImageUrl: String?,
    override val layoutRes: Int = R.layout.news_card_view,
    override val type: Int = 1
) : AbstractItem<News.ViewHolder>() {
    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(private val view: View) :
        FastAdapter.ViewHolder<News>(view) {
        private val title: TextView = view.findViewById(R.id.title)
        private val description: TextView = view.findViewById(R.id.description)
        private val imageHeader: ImageView = view.findViewById(R.id.imageHeader)
        private val websiteLogo: ImageView = view.findViewById(R.id.ws_logo)
        private val websiteName: TextView = view.findViewById(R.id.ws_name)
        private val publishDate: TextView = view.findViewById(R.id.date)
        val cardContainer: MaterialCardView = view.findViewById(R.id.root)
        val shareImage: ImageView = view.findViewById(R.id.share)

        @SuppressLint("SetTextI18n")
        override fun bindView(item: News, payloads: List<Any>) {
            val formatter = DateFormat.getDateTimeInstance()
            title.text = item.title
            description.text = item.short
            if (item.imageUrl != null) {
                GlideApp.with(view)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_handwashing_icon)
                    .centerCrop()
                    .into(imageHeader)
            } else imageHeader.visibility = View.GONE
            if (item.websiteImageUrl != null) {
                GlideApp.with(view)
                    .load(item.websiteImageUrl)
                    .centerCrop()
                    .into(websiteLogo)
            } else websiteLogo.visibility = View.GONE
            if (item.website != null)
                websiteName.text = item.website
            else websiteName.visibility = View.GONE
            if (item.publishDate != null)
                publishDate.text = formatter.format(item.publishDate)
            else
                publishDate.visibility = View.GONE
        }

        override fun unbindView(item: News) {
            title.text = null
            description.text = null
            imageHeader.setImageDrawable(null)
            websiteLogo.setImageDrawable(null)
            websiteName.text = null
            publishDate.text = null
        }
    }
}