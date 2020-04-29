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

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.util.set
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.paolorotolo.appintro.AppIntroBaseFragment
import com.javinator9889.handwashingreminder.appintro.R
import com.javinator9889.handwashingreminder.appintro.TIME_CONFIG_REQUEST_CODE
import com.javinator9889.handwashingreminder.appintro.timeconfig.TimeConfigActivity
import com.javinator9889.handwashingreminder.appintro.timeconfig.TimeConfigItem
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.TimeConfig
import com.javinator9889.handwashingreminder.utils.isAtLeast
import com.javinator9889.handwashingreminder.utils.isViewVisible
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.time_card_view.view.*
import kotlinx.android.synthetic.main.time_config.view.*

class TimeConfigIntroFragment : AppIntroBaseFragment() {
    var bgColor: Int = Color.WHITE
    var isInitialized = false
    val propertyContainer = SparseArray<TimeContainer>(3)
    lateinit var recyclerView: RecyclerView
    lateinit var fastAdapter: FastAdapter<TimeConfigItem>
    lateinit var itemAdapter: ItemAdapter<TimeConfigItem>

    init {
        propertyContainer[TimeConfig.BREAKFAST_ID.toInt()] = TimeContainer()
        propertyContainer[TimeConfig.LUNCH_ID.toInt()] = TimeContainer()
        propertyContainer[TimeConfig.DINNER_ID.toInt()] = TimeContainer()
    }

    @Suppress("unchecked_cast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val breakfast = getString(R.string.breakfast)
        val lunch = getString(R.string.lunch)
        val dinner = getString(R.string.dinner)
        itemAdapter = ItemAdapter<TimeConfigItem>().apply {
            val items = listOf(
                TimeConfigItem(
                    getString(R.string.time_config_title_tpl, breakfast),
                    TimeConfig.BREAKFAST_ID,
                    propertyContainer[TimeConfig.BREAKFAST_ID.toInt()].hours,
                    propertyContainer[TimeConfig.BREAKFAST_ID.toInt()].minutes
                ),
                TimeConfigItem(
                    getString(R.string.time_config_title_tpl, lunch),
                    TimeConfig.LUNCH_ID,
                    propertyContainer[TimeConfig.LUNCH_ID.toInt()].hours,
                    propertyContainer[TimeConfig.LUNCH_ID.toInt()].minutes
                ),
                TimeConfigItem(
                    getString(R.string.time_config_title_tpl, dinner),
                    TimeConfig.DINNER_ID,
                    propertyContainer[TimeConfig.DINNER_ID.toInt()].hours,
                    propertyContainer[TimeConfig.DINNER_ID.toInt()].minutes
                )
            )
            setNewList(items)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val manager = LinearLayoutManager(context)
        fastAdapter = FastAdapter.with(itemAdapter)
        recyclerView = view.cardsView.apply {
            setHasFixedSize(true)
            layoutManager = manager
            adapter = fastAdapter
            setBackgroundColor(bgColor)
        }
        isInitialized = true
        fastAdapter.addEventHook(object : ClickEventHook<TimeConfigItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder) =
                if (viewHolder is TimeConfigItem.ViewHolder) viewHolder.cardView
                else null

            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<TimeConfigItem>,
                item: TimeConfigItem
            ) {
                val intent = Intent(context, TimeConfigActivity::class.java)
                val options = if (isAtLeast(AndroidVersion.LOLLIPOP)) {
                    val pairs = mutableListOf<Pair<View, String>>()
                    val items = HashMap<String, View>(6).apply {
                        this[TimeConfigActivity.VIEW_TITLE_NAME] = v.title
                        this[TimeConfigActivity.INFO_IMAGE_NAME] = v.infoImage
                        this[TimeConfigActivity.USER_TIME_ICON] = v.clockIcon
                        this[TimeConfigActivity.USER_TIME_HOURS] = v.hours
                        this[TimeConfigActivity.USER_DDOT] = v.ddot
                        this[TimeConfigActivity.USER_TIME_MINUTES] = v.minutes
                    }
                    items.onEach {
                        if (it.value.isViewVisible(recyclerView))
                            pairs.add(Pair.create(it.value, it.key))
                    }
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        *pairs.toTypedArray()
                    )
                } else {
                    null
                }
                intent.apply {
                    putExtra("title", v.title.text)
                    putExtra("hours", v.hours.text)
                    putExtra("minutes", v.minutes.text)
                    putExtra("id", item.id)
                    putExtra("position", position)
                }
                ActivityCompat.startActivityForResult(
                    requireActivity(),
                    intent,
                    TIME_CONFIG_REQUEST_CODE,
                    options?.toBundle()
                )
            }
        })
    }

    override fun getLayoutId(): Int = R.layout.time_config
}

data class TimeContainer(val hours: String? = "", val minutes: String? = "")