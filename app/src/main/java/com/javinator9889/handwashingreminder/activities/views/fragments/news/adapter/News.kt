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

import android.view.View
import com.javinator9889.handwashingreminder.R
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import java.util.*

data class News(
    val title: String,
    val short: String,
    val url: String,
    val publishDate: Date,
    val imageUrl: String?,
    val website: String,
    val websiteImageUrl: String,
    override val layoutRes: Int = R.layout.news_card_view,
    override val type: Int = 1
) : AbstractItem<News.ViewHolder>() {
    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<News>(view) {

        override fun bindView(item: News, payloads: List<Any>) {
            TODO("Not yet implemented")
        }

        override fun unbindView(item: News) {
            TODO("Not yet implemented")
        }

    }
}