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
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.emoji.text.EmojiCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.textview.MaterialTextView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.base.LayoutVisibilityChange
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.adapter.Ads
import com.javinator9889.handwashingreminder.activities.views.fragments.diseases.adapter.Disease
import com.javinator9889.handwashingreminder.activities.views.viewmodels.DiseaseInformationViewModel
import com.javinator9889.handwashingreminder.activities.views.viewmodels.SavedViewModelFactory
import com.javinator9889.handwashingreminder.data.ParsedHTMLText
import com.javinator9889.handwashingreminder.data.room.entities.Handwashing
import com.javinator9889.handwashingreminder.data.viewmodels.HandwashingViewModel
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.utils.calendar.CalendarUtils
import com.javinator9889.handwashingreminder.utils.toBarEntry
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.handwash_count.*
import kotlinx.android.synthetic.main.handwash_count.view.*
import kotlinx.android.synthetic.main.loading_recycler_view.*
import kotlinx.android.synthetic.main.loading_recycler_view.view.*
import kotlinx.android.synthetic.main.main_disease_view.view.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import timber.log.Timber


class DiseasesFragment : BaseFragmentView(), LayoutVisibilityChange,
    View.OnClickListener {
    override val layoutId: Int = R.layout.main_disease_view

    private lateinit var parsedHTMLTexts: List<ParsedHTMLText>
    private lateinit var fastAdapter: FastAdapter<GenericItem>
    private lateinit var emojiLoader: Deferred<EmojiCompat>
    private lateinit var behavior: BottomSheetBehavior<LinearLayout>
    private val upperAdsAdapter: ItemAdapter<Ads> = ItemAdapter()
    private val lowerAdsAdapter: ItemAdapter<Ads> = ItemAdapter()
    private val diseasesAdapter: ItemAdapter<Disease> = ItemAdapter()
    private val informationViewModel: DiseaseInformationViewModel by viewModels {
        SavedViewModelFactory(DiseaseInformationViewModel.Factory, this)
    }
    private val handwashingViewModel: HandwashingViewModel by activityViewModels()
    private var dataSet: BarDataSet? = null

    init {
        lifecycleScope.launchWhenStarted {
            loading.visibility = View.VISIBLE
            countLoader.visibility = View.VISIBLE
            informationViewModel.parsedHTMLText.observe(viewLifecycleOwner) {
                if (it.isEmpty())
                    return@observe
                lifecycleScope.launch {
                    parsedHTMLTexts = it
                    it.forEachIndexed { i, parsedText ->
                        val animation =
                            if (i % 2 == 0) R.raw.virus_red
                            else R.raw.virus_loader
                        val layoutId =
                            if (i % 2 == 0) R.layout.disease_card_layout
                            else R.layout.disease_card_alt_layout
                        val disease =
                            Disease(animation, parsedText, layoutId, i)
                        if (diseasesAdapter.getAdapterPosition(disease) == -1)
                            diseasesAdapter.add(disease)
                    }
                    loading.visibility = View.INVISIBLE
                    container.visibility = View.VISIBLE
                }
            }
            handwashingViewModel.allData.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    dataSet?.let { set ->
                        Timber.d("Adding new items to dataSet")
                        set.clear()
                        for (entry in it.toBarEntry())
                            set.addEntry(entry)
                    } ?: with(BarDataSet(it.toBarEntry(), "")) {
                        Timber.d("dataSet not created so instantiating it")
                        valueFormatter = IntFormatter()
                        dataSet = this
                    }
                    countChart.data = BarData(dataSet)
                    countChart.notifyDataSetChanged()
                    countChart.fitScreen()
                    countChart.moveViewToX(0F)
                    countChart.setVisibleXRangeMaximum(7F)
                    countChart.invalidate()
                    val todayAmount =
                        handwashingViewModel.getAsync(CalendarUtils.today.time)
                    val weeklyAmount =
                        handwashingViewModel.getWeeklyCountAsync()
                    val monthlyAmount =
                        handwashingViewModel.getMonthlyCountAsync()
                    setCountText(
                        countDailyTextView,
                        R.string.today_washed,
                        todayAmount.await()?.amount ?: 0
                    )
                    setCountText(
                        countWeeklyTextView,
                        R.string.week_washed,
                        weeklyAmount.await()
                    )
                    setCountText(
                        countMonthlyTextView,
                        R.string.month_washed,
                        monthlyAmount.await()
                    )
                    countLoader.visibility = View.INVISIBLE
                    scrollView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreatedAsync(view, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fastAdapter.saveInstanceState(outState)
    }

    fun onBackPressed() {
        if (::behavior.isInitialized) {
            if (behavior.state == STATE_EXPANDED) {
                behavior.state = STATE_COLLAPSED
                return
            }
        }
        try {
            container.adapter = null
            diseasesAdapter.clear()
        } catch (e: Exception) {
            Timber.w(e, "Exception when calling 'onBackPressed'")
        } finally {
            onDestroy()
        }
    }

    override fun onVisibilityChanged(visibility: Int) {}

    override fun onClick(v: View?) {
        when (v) {
            countUpButton -> lifecycleScope.launch {
                val createdItem =
                    handwashingViewModel.getAsync(CalendarUtils.today.time)
                        .await()
                if (createdItem == null)
                    handwashingViewModel.create(
                        Handwashing(
                            CalendarUtils.today.time,
                            0
                        )
                    )
                handwashingViewModel.increment(CalendarUtils.today.time)
                Timber.d("${CalendarUtils.lastWeek.time}")
                Timber.d("${CalendarUtils.lastMonth.time}")
                handwashingViewModel.create(Handwashing(CalendarUtils.lastWeek.time, 12))
                leaves.visibility = View.VISIBLE
                if (!leaves.isAnimating)
                    leaves.playAnimation()
            }
            countDownButton -> lifecycleScope.launch {
                val createdItem =
                    handwashingViewModel.getAsync(CalendarUtils.today.time)
                        .await()
                if (createdItem == null)
                    handwashingViewModel.create(
                        Handwashing(
                            CalendarUtils.today.time,
                            0
                        )
                    )
                handwashingViewModel.decrement(CalendarUtils.today.time)
                handwashingViewModel.create(Handwashing(CalendarUtils.lastMonth.time, 123))
            }
        }
    }

    private suspend fun setCountText(
        view: MaterialTextView,
        @StringRes id: Int,
        times: Int
    ) {
        val emojiCompat = emojiLoader.await()
        val text = getString(
            id,
            resources.getQuantityString(R.plurals.times, times, times)
        )
        view.text = try {
            emojiCompat.process(text)
        } catch (_: IllegalStateException) {
            text
        }
    }

    private fun onViewCreatedAsync(view: View, savedInstanceState: Bundle?) =
        lifecycleScope.launch {
            emojiLoader = EmojiLoader.loadAsync(view.context)
            val adapters =
                listOf(upperAdsAdapter, diseasesAdapter, lowerAdsAdapter)
            fastAdapter = FastAdapter.with(adapters)
            val rvManager = LinearLayoutManager(context)
            with(view.container) {
                layoutManager = rvManager
                adapter = fastAdapter
            }
            fastAdapter.addEventHook(DiseaseClickEventHook())
            fastAdapter.withSavedInstanceState(savedInstanceState)
            behavior = BottomSheetBehavior.from(view.contentLayout)
            behavior.addBottomSheetCallback(BottomSheetStateCallback())
            view.countChart.setDrawGridBackground(false)
            view.countChart.axisLeft.apply {
                setDrawGridLines(false)
                valueFormatter = IntFormatter()
                isGranularityEnabled = true
                granularity = 1F
                axisMinimum = 0F
            }
            view.countChart.axisRight.apply {
                setDrawGridLines(false)
                valueFormatter = IntFormatter()
                isGranularityEnabled = true
                granularity = 1F
                axisMinimum = 0F
            }
            view.countChart.xAxis.apply {
                setDrawGridLines(false)
                valueFormatter = IntFormatter()
                isGranularityEnabled = true
                granularity = 1F
                axisMaximum = 0F
            }
            view.countChart.setVisibleXRangeMaximum(7F)
            view.countChart.isAutoScaleMinMaxEnabled = true
            view.countChart.legend.isEnabled = false
            view.countChart.description.isEnabled = false
            view.countChart.invalidate()
            view.countUpButton.setOnClickListener(this@DiseasesFragment)
            view.countDownButton.setOnClickListener(this@DiseasesFragment)
            val countUpText = getText(R.string.add_another)
            val countDownText = getText(R.string.reduce_count)
            val emojiCompat = emojiLoader.await()
            try {
                countUpButton.text = emojiCompat.process(countUpText)
                countDownButton.text = emojiCompat.process(countDownText)
            } catch (_: IllegalStateException) {
                countUpButton.text = countUpText
                countDownButton.text = countDownText
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

    private inner class BottomSheetStateCallback :
        BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == STATE_EXPANDED) {
                if (upperAdsAdapter.adapterItemCount == 0)
                    upperAdsAdapter.add(Ads())
                if (lowerAdsAdapter.adapterItemCount == 0)
                    lowerAdsAdapter.add(Ads())
                informationViewModel.parseHtml()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private inner class IntFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float) = value.toInt().toString()
    }
}
