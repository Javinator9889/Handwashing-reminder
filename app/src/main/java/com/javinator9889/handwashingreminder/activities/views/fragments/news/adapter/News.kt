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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.size.Scale
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.card.MaterialCardView
import com.javinator9889.handwashingreminder.R
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DateFormat
import java.util.*

data class News(
    val title: String,
    val short: String,
    val url: String,
    val discoverDate: Date?,
    val imageUrl: String?,
    val website: String?,
    val websiteImageUrl: String?,
    val lifecycleOwner: LifecycleOwner,
    override val layoutRes: Int = R.layout.news_card_view,
    override val type: Int = 1
) : AbstractItem<News.ViewHolder>() {
    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(private val view: View) :
        FastAdapter.ViewHolder<News>(view) {
        private val title: TextView = view.findViewById(R.id.title)
        private val description: TextView = view.findViewById(R.id.description)
        private val imageHeader: LottieAnimationView =
            view.findViewById(R.id.imageHeader)
        private val websiteLogo: ImageView = view.findViewById(R.id.ws_logo)
        private val websiteName: TextView = view.findViewById(R.id.ws_name)
        private val publishDate: TextView = view.findViewById(R.id.date)
        val cardContainer: MaterialCardView = view.findViewById(R.id.root)
        val shareImage: ImageView = view.findViewById(R.id.share)

        @SuppressLint("SetTextI18n")
        override fun bindView(item: News, payloads: List<Any>) {
            val formatter = DateFormat.getDateTimeInstance()
            val context = view.context
            imageHeader.setAnimation(R.raw.downloading)
            item.lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                title.text = item.title
                description.text = item.short
                if (item.imageUrl != null) {
                    imageHeader.load(item.imageUrl) {
                        scale(Scale.FILL)
                        lifecycle(item.lifecycleOwner)
                    }
                } else imageHeader.visibility = View.GONE
                if (item.websiteImageUrl != null) {
                    websiteLogo.load(item.websiteImageUrl) {
                        scale(Scale.FILL)
                        lifecycle(item.lifecycleOwner)
                    }
                } else websiteLogo.visibility = View.GONE
                websiteName.text = item.website
                    ?: context.getString(R.string.no_website)
                publishDate.text =
                    item.discoverDate?.let { formatter.format(it) }
                        ?: context.getString(R.string.no_date)
            }
        }

        override fun unbindView(item: News) {
            Timber.d("Called 'unbind'")
            title.text = null
            description.text = null
            imageHeader.setImageDrawable(null)
            websiteLogo.setImageDrawable(null)
            websiteName.text = null
            publishDate.text = null
        }
    }
}