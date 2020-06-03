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
package com.javinator9889.handwashingreminder.activities.views.fragments.news

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.views.fragments.news.adapter.News
import com.javinator9889.handwashingreminder.activities.views.viewmodels.NewsViewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.GenericItemAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.scroll.EndlessRecyclerOnScrollListener
import com.mikepenz.fastadapter.ui.items.ProgressItem
import kotlinx.android.synthetic.main.loading_recycler_view.*
import kotlinx.android.synthetic.main.loading_recycler_view.view.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class NewsFragment : BaseFragmentView() {
    @LayoutRes
    override val layoutId: Int = R.layout.loading_recycler_view
    private lateinit var fastAdapter: FastAdapter<GenericItem>
    private lateinit var footerAdapter: GenericItemAdapter
    private val newsAdapter = ItemAdapter<News>()
    private val newsViewModel: NewsViewModel by viewModels()
    private val activeItems = mutableSetOf<String>()

    init {
        lifecycleScope.launch {
            whenStarted {
                loading.visibility = View.VISIBLE
                async { newsViewModel.populateData() }
                newsViewModel.newsData.observe(viewLifecycleOwner, Observer {
                    if (it.id !in activeItems) {
                        val newsObject = News(
                            title = it.title,
                            short = "${it.text.take(100)}…",
                            url = it.url,
                            publishDate = it.publishDate,
                            imageUrl = it.elements[0].url,
                            website = it.website?.name,
                            websiteImageUrl = it.website?.iconURL
                        )
                        newsAdapter.add(newsObject)
                        if (::footerAdapter.isInitialized)
                            footerAdapter.clear()
                        loading.visibility = View.INVISIBLE
                        container.visibility = View.VISIBLE
                        activeItems.add(it.id)
                    }
                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        footerAdapter = ItemAdapter.items()
        fastAdapter = FastAdapter.with(listOf(newsAdapter, footerAdapter))
        val rvManager = LinearLayoutManager(context)
        val scrollListener =
            object : EndlessRecyclerOnScrollListener(footerAdapter) {
                override fun onLoadMore(currentPage: Int) {
                    footerAdapter.clear()
                    val progressItem = ProgressItem()
                    progressItem.isEnabled = true
                    footerAdapter.add(progressItem)
                    lifecycleScope.async {
                        newsViewModel.populateData(
                            from = newsAdapter.adapterItemCount
                        )
                    }
                }
            }
        with(view.container) {
            layoutManager = rvManager
            adapter = fastAdapter
            itemAnimator = DefaultItemAnimator()
            addOnScrollListener(scrollListener)
        }
        fastAdapter.addEventHooks(listOf(NewsClickHook(), ShareClickHook()))
        fastAdapter.withSavedInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fastAdapter.saveInstanceState(outState)
    }

    private inner class NewsClickHook : ClickEventHook<News>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder) =
            if (viewHolder is News.ViewHolder) viewHolder.cardContainer
            else null

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<News>,
            item: News
        ) {
            TODO("Not yet implemented - open web browser")
        }
    }

    private inner class ShareClickHook : ClickEventHook<News>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder) =
            if (viewHolder is News.ViewHolder) viewHolder.shareImage
            else null

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<News>,
            item: News
        ) {
            TODO("Not yet implemented - share intent")
        }
    }
}