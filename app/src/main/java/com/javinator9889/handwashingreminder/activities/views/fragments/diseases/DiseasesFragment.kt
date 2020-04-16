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
package com.javinator9889.handwashingreminder.activities.views.fragments.diseases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.adapter.Ads
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.adapter.Disease
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

class DiseasesFragment : BaseFragmentView() {
    override val layoutId: Int = R.layout.diseases_list
    private lateinit var adapters: Collection<IAdapter<out GenericItem>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val adapter = ItemAdapter<Disease>()
        val ads1 = ItemAdapter<Ads>()
        val ads2 = ItemAdapter<Ads>()
        val items = listOf(
            Disease(
                R.raw.corona_virus,
                R.string.disease,
                R.string.disease_desc,
                R.layout.disease_card_layout,
                0
            ),
            Disease(
                R.raw.virus_loader,
                R.string.disease,
                R.string.disease_desc,
                R.layout.disease_card_alt_layout,
                1
            ),
            Disease(
                R.raw.corona_virus,
                R.string.disease,
                R.string.disease_desc,
                R.layout.disease_card_layout,
                2
            )
        )
        val adsItems = listOf(Ads())
        val ads2Items = listOf(Ads())
        adapter.add(items)
        ads1.add(adsItems)
        ads2.add(ads2Items)

        adapters = listOf(ads1, adapter, ads2)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val fastAdapter = FastAdapter.with(adapters)
        val rvManager = LinearLayoutManager(context)
        with(view.findViewById<RecyclerView>(R.id.diseasesContainer)) {
            layoutManager = rvManager
            adapter = fastAdapter
        }
        return view
    }
}