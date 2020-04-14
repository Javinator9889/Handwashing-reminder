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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.activities.views.viewmodels.SavedViewModelFactory
import com.javinator9889.handwashingreminder.activities.views.viewmodels.VideoModel
import com.javinator9889.handwashingreminder.activities.views.viewmodels.VideoModelFactory
import kotlinx.android.synthetic.main.wash_your_hands_demo.*
import kotlinx.android.synthetic.main.wash_your_hands_demo.view.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class SliderView(position: Int) : BaseFragmentView() {
    override val layoutId: Int = R.layout.wash_your_hands_demo
    private lateinit var videoURI: Uri
    private lateinit var contentView: View
    private val videoModelFactory = VideoModelFactory(position)
    private val viewModel: VideoModel by viewModels {
        SavedViewModelFactory(videoModelFactory, this)
    }

    init {
        lifecycleScope.launch {
            whenStarted {
                loading.visibility = View.VISIBLE
                viewModel.videos.observe(viewLifecycleOwner, Observer {
                    with(File(requireContext().cacheDir, it)) {
                        videoURI = Uri.fromFile(this)
                    }
                    if (::contentView.isInitialized)
                        showContent(contentView)
                })
                if (::videoURI.isInitialized)
                    showContent()
            }
        }
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
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView = view
        if (::videoURI.isInitialized) {
            showContent(view)
        }
    }

    private fun showContent(v: View? = null) {
        val cView = v ?: requireView()
        cView.loading.visibility = View.GONE
        cView.video.setVideoURI(videoURI)
        cView.title.visibility = View.VISIBLE
        cView.video.visibility = View.VISIBLE
        cView.video.requestFocus()
        cView.video.start()
        cView.video.setOnPreparedListener { mp: MediaPlayer? -> mp?.isLooping = true }
        cView.description.visibility = View.VISIBLE
        cView.image.visibility = View.VISIBLE
    }
}