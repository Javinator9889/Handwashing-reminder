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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.adapter.Ads
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.adapter.Disease
import com.javinator9889.handwashingreminder.activities.views.viewmodels.DiseaseInformationFactory
import com.javinator9889.handwashingreminder.activities.views.viewmodels.DiseaseInformationViewModel
import com.javinator9889.handwashingreminder.activities.views.viewmodels.ParsedHTMLText
import com.javinator9889.handwashingreminder.activities.views.viewmodels.SavedViewModelFactory
import com.javinator9889.handwashingreminder.collections.DiseaseDescriptionFragment
import com.javinator9889.handwashingreminder.graphics.LottieAdaptedPerformanceAnimationView
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.isAtLeast
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.diseases_list.*
import kotlinx.android.synthetic.main.diseases_list.view.*
import kotlinx.coroutines.launch

class DiseasesFragment : BaseFragmentView() {
    override val layoutId: Int = R.layout.diseases_list
    private lateinit var parsedHTMLTexts: List<ParsedHTMLText>
    private val upperAdsAdapter: ItemAdapter<Ads> = ItemAdapter()
    private val lowerAdsAdapter: ItemAdapter<Ads> = ItemAdapter()
    private val diseasesAdapter: ItemAdapter<Disease> = ItemAdapter()
    private lateinit var fastAdapter: FastAdapter<GenericItem>
    private val informationFactory = DiseaseInformationFactory()
    private val informationViewModel: DiseaseInformationViewModel by viewModels {
        SavedViewModelFactory(informationFactory, this)
    }

    init {
        lifecycleScope.launch {
            whenStarted {
                loading.visibility = View.VISIBLE
                informationViewModel.parsedHTMLText
                    .observe(viewLifecycleOwner, Observer {
                        parsedHTMLTexts = it
                        upperAdsAdapter.add(Ads())
                        lowerAdsAdapter.add(Ads())
                        it.forEachIndexed { i, parsedText ->
                            val animation =
                                if (i % 2 == 0) R.raw.virus_red
                                else R.raw.virus_loader
                            val layoutId =
                                if (i % 2 == 0) R.layout.disease_card_layout
                                else R.layout.disease_card_alt_layout
                            diseasesAdapter.add(
                                Disease(animation, parsedText, layoutId, i)
                            )
                        }
                        loading.visibility = View.INVISIBLE
                        diseasesContainer.visibility = View.VISIBLE
                    })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*val adapter = ItemAdapter<Disease>()
        val upperAds = ItemAdapter<Ads>()
        val lowerAds = ItemAdapter<Ads>()
        val items = if (diseasesData?.diseases != null) {
            val diseases = ArrayList<Disease>(diseasesData.diseases.size)
            diseasesData.diseases.forEachIndexed { i, diseasesInformation ->
                val animation = if (i % 2 == 0) R.raw.virus_red
                else R.raw.virus_loader
                diseases += Disease(
                    animation
                )
            }
            diseases.toList()
        } else {
            listOf<Disease>()
        }
        val items = ArrayList<Disease>(diseasesData?.diseases?.size)
        diseasesData?.diseases?.forEachIndexed { i, diseasesInformation ->
            items += Disease(

            )
        }
        val items = listOf(
            Disease(
                R.raw.virus_red,
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
                R.raw.virus_red,
                R.string.disease,
                R.string.disease_desc,
                R.layout.disease_card_layout,
                2
            )
        )
        val adsItems = listOf(Ads())
        val ads2Items = listOf(Ads())
        adapter.add(items)
        upperAds.add(adsItems)
        lowerAds.add(ads2Items)

        adapters = listOf(upperAds, adapter, lowerAds)*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val upperAdsAdapter = GenericItemAdapter()
//        val diseasesListAdapter = GenericItemAdapter()
//        val lowerAdsAdapter = GenericItemAdapter()
        val adapters = listOf(upperAdsAdapter, diseasesAdapter, lowerAdsAdapter)
        fastAdapter = FastAdapter.with(adapters)
        val rvManager = LinearLayoutManager(context)
        with(view.diseasesContainer) {
            layoutManager = rvManager
            adapter = fastAdapter
        }
        fastAdapter.addEventHook(DiseaseClickEventHook())
    }

    private inner class DiseaseClickEventHook : ClickEventHook<Disease>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return if (viewHolder is Disease.ViewHolder)
                viewHolder.cardContainer
            else null
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<Disease>,
            item: Disease
        ) {
            val parsedHTMLText = item.information
            val animId = item.animation
            val intent = Intent(
                requireContext(),
                DiseaseExpandedView::class.java
            )
            val options =
                if (isAtLeast(AndroidVersion.LOLLIPOP)) {
                    val pairs = mutableListOf<Pair<View, String>>(
                        Pair.create(
                            v.findViewById<TextView>(R.id.title),
                            DiseaseDescriptionFragment.DISEASE_NAME
                        ),
                        Pair.create(
                            v.findViewById<LottieAdaptedPerformanceAnimationView>(
                                R.id.image
                            ),
                            DiseaseDescriptionFragment.DISEASE_ANIM
                        ),
                        Pair.create(
                            v.findViewById<TextView>(R.id.shortDescription),
                            DiseaseDescriptionFragment.DISEASE_DESCRIPTION
                        )
                    )
                    ActivityOptionsCompat
                        .makeSceneTransitionAnimation(
                            requireActivity(),
                            *pairs.toTypedArray()
                        )
                } else null
            intent.putExtra(ARG_ANIMATION, animId)
            intent.putExtra(ARG_PARSED_TEXT, parsedHTMLText)
            ActivityCompat.startActivity(
                requireContext(),
                intent,
                options?.toBundle()
            )
        }
    }
}