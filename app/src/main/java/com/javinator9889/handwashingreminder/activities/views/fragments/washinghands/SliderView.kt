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
 * Created by Javinator9889 on 13/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.fragments.washinghands

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.views.viewmodels.*
import com.javinator9889.handwashingreminder.graphics.GlideApp
import kotlinx.android.synthetic.main.wash_your_hands_demo.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

private const val WAITING_ITEMS_COUNT = 4
internal const val ARG_POSITION = "bundle:position"

class SliderView : BaseFragmentView() {
    override val layoutId: Int = R.layout.wash_your_hands_demo
    private lateinit var videoURI: Uri
    private var drawableId by Delegates.notNull<Int>()
    private val counter = AtomicInteger(0)
    private var position by Delegates.notNull<Int>()
    private lateinit var videoModelFactory: VideoModelFactory
    private lateinit var handsFactory: WashingHandsModelFactory
    private val viewModel: VideoModel by viewModels {
        SavedViewModelFactory(videoModelFactory, this)
    }
    private val washingHandsModel: WashingHandsModel by viewModels {
        SavedViewModelFactory(handsFactory, this)
    }

    init {
        lifecycleScope.launch {
            whenStarted {
                loading.visibility = View.VISIBLE
                viewModel.videos.observe(viewLifecycleOwner, Observer {
                    with(File(requireContext().cacheDir, it)) {
                        videoURI = Uri.fromFile(this)
                    }
                    incrementCounter()
                    Timber.d("Video finished loading")
                })
                washingHandsModel.image.observe(viewLifecycleOwner, Observer {
                    try {
                        GlideApp.with(this@SliderView)
                            .load(it)
                            .into(image)
                    } catch (e: Exception) {
                        Timber.e(e, "Error while loading Glide view")
                        image.setImageResource(it)
                    }
                    drawableId = it
                    Timber.d("Image finished loading")
                    incrementCounter()
                })
                washingHandsModel.title.observe(viewLifecycleOwner, Observer {
                    title.text = it
                    Timber.d("Title finished loading")
                    incrementCounter()
                })
                washingHandsModel.description.observe(viewLifecycleOwner,
                    Observer {
                        description.text = it
                        Timber.d("Description finished loading")
                        incrementCounter()
                    })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments ?: savedInstanceState ?: throw
        IllegalStateException("Arguments cannot be null")
        videoModelFactory = VideoModelFactory(args.getInt(ARG_POSITION))
        handsFactory = WashingHandsModelFactory(args.getInt(ARG_POSITION))
        position = args.getInt(ARG_POSITION)
    }

    override fun onPause() {
        Timber.d("Slide paused")
        video.requestFocus()
        video.pause()
        super.onPause()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden)
            onPause()
        else
            onResume()
    }

    override fun onResume() {
        Timber.d("Slide resumed")
        video.requestFocus()
        video.start()
        try {
            GlideApp.with(this)
                .load(drawableId)
                .centerInside()
                .into(image)
        } catch (e: Exception) {
            Timber.e(e, "Error while loading Glide view")
            image.setImageResource(drawableId)
        }
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image.doOnLayout {
            washingHandsModel.setImageSize(it.measuredWidth, it.measuredHeight)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_POSITION, position)
    }

    private fun incrementCounter() {
        Timber.d("Counter incremented")
        if (counter.incrementAndGet() < WAITING_ITEMS_COUNT)
            return
        showContent()
    }

    private fun showContent() {
        loading.visibility = View.GONE
        video.setVideoURI(videoURI)
        title.visibility = View.VISIBLE
        video.visibility = View.VISIBLE
        video.requestFocus()
        video.start()
        video.setOnPreparedListener { mp: MediaPlayer? ->
            mp?.isLooping = true
        }
        description.visibility = View.VISIBLE
        image.visibility = View.VISIBLE
    }
}