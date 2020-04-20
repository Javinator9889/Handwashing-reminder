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
 * Created by Javinator9889 on 19/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.appintro.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.paolorotolo.appintro.AppIntroBaseFragment
import com.google.android.play.core.splitcompat.SplitCompat
import com.javinator9889.handwashingreminder.appintro.R
import com.javinator9889.handwashingreminder.appintro.timeconfig.TimeConfigAdapter
import com.javinator9889.handwashingreminder.appintro.timeconfig.TimeConfigContent
import com.javinator9889.handwashingreminder.appintro.timeconfig.TimeConfigViewHolder
import com.javinator9889.handwashingreminder.listeners.ViewHolder
import com.javinator9889.handwashingreminder.utils.TimeConfig
import com.javinator9889.handwashingreminder.utils.notNull
import kotlinx.android.synthetic.main.time_config.view.*
import com.javinator9889.handwashingreminder.R as RBase

class TimeConfigIntroFragment : AppIntroBaseFragment() {
    private lateinit var rvAdapter: TimeConfigAdapter
    lateinit var rvItems: Array<TimeConfigContent>
    lateinit var recyclerView: RecyclerView
    lateinit var fromActivity: AppCompatActivity
    var bgColor: Int = Color.WHITE
    var listener: ViewHolder.OnItemClickListener? = null
    val viewItems = SparseArray<TimeConfigViewHolder>(3)


    override fun onAttach(context: Context) {
        super.onAttach(context)
        SplitCompat.installActivity(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val breakfast = getString(R.string.breakfast)
        val lunch = getString(R.string.lunch)
        val dinner = getString(R.string.dinner)
        rvItems = arrayOf(
            TimeConfigContent(
                getString(R.string.time_config_title_tpl, breakfast),
                TimeConfig.BREAKFAST_ID
            ),
            TimeConfigContent(
                getString(R.string.time_config_title_tpl, lunch),
                TimeConfig.LUNCH_ID
            ),
            TimeConfigContent(
                getString(R.string.time_config_title_tpl, dinner),
                TimeConfig.DINNER_ID
            )
        )
        rvAdapter =
            TimeConfigAdapter(
                rvItems,
                listener,
                viewItems
            )
    }

    @Suppress("unchecked_cast")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState.notNull {
            val restoredItems = it.getParcelableArray("rvItems")
                    as Array<TimeConfigContent>
            restoredItems.forEachIndexed { i, timeConfigContent ->
                rvItems[i].hours = timeConfigContent.hours
                rvItems[i].minutes = timeConfigContent.minutes
            }
            viewItems.forEach { _, value -> value.loadContentToTextViews() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArray("rvItems", rvItems)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layoutId, container, false)
        val rvManager = LinearLayoutManager(context)
        fromActivity.setSupportActionBar(view.findViewById(RBase.id.toolbar))
        recyclerView =
            view.cardsView.apply {
                setHasFixedSize(true)
                layoutManager = rvManager
                adapter = rvAdapter
            }
        recyclerView.setBackgroundColor(bgColor)

        return view
    }

    override fun getLayoutId(): Int = R.layout.time_config
}