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
import androidx.core.app.ActivityCompat
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
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.diseases_list.*
import kotlinx.android.synthetic.main.diseases_list.view.*
import kotlinx.coroutines.launch
import timber.log.Timber

class DiseasesFragment : BaseFragmentView() {
    override val layoutId: Int = R.layout.diseases_list
    private lateinit var parsedHTMLTexts: List<ParsedHTMLText>
    private lateinit var fastAdapter: FastAdapter<GenericItem>
    private val upperAdsAdapter: ItemAdapter<Ads> = ItemAdapter()
    private val lowerAdsAdapter: ItemAdapter<Ads> = ItemAdapter()
    private val diseasesAdapter: ItemAdapter<Disease> = ItemAdapter()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapters = listOf(upperAdsAdapter, diseasesAdapter, lowerAdsAdapter)
        fastAdapter = FastAdapter.with(adapters)
        val rvManager = LinearLayoutManager(context)
        with(view.diseasesContainer) {
            layoutManager = rvManager
            adapter = fastAdapter
        }
        fastAdapter.addEventHook(DiseaseClickEventHook())
        fastAdapter.withSavedInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fastAdapter.saveInstanceState(outState)
    }

    fun onBackPressed() {
        try {
            diseasesContainer.adapter = null
            diseasesAdapter.clear()
        } catch (e: Exception) {
            Timber.w(e, "Exception when calling 'onBackPressed'")
        } finally {
            onDestroy()
        }
    }

    private inner class DiseaseClickEventHook : ClickEventHook<Disease>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder) =
            if (viewHolder is Disease.ViewHolder) viewHolder.cardContainer
            else null

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
            intent.putExtra(ARG_ANIMATION, animId)
            intent.putExtra(ARG_PARSED_TEXT, parsedHTMLText)
            ActivityCompat.startActivity(
                requireContext(),
                intent,
                null
            )
        }
    }
}