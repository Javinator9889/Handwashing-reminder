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
import kotlinx.android.synthetic.main.wash_your_hands_demo.*
import kotlinx.android.synthetic.main.wash_your_hands_demo.view.*
import kotlinx.coroutines.launch

class SliderView(private val position: Int) : BaseFragmentView() {
    override val layoutId: Int = R.layout.wash_your_hands_demo
    private val viewModel: VideoModel by viewModels()
//    private lateinit var video: File

    init {
        lifecycleScope.launch {
            whenStarted {
                loading.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.videos.observe(viewLifecycleOwner, Observer {
//            video = it[position]
            view.video.setVideoURI(Uri.fromFile(it[position]))
            showContent(view)
        })
    }

    private fun showContent(v: View) {
        v.loading.visibility = View.GONE
        v.title.visibility = View.VISIBLE
        v.video.visibility = View.VISIBLE
        v.video.start()
        v.video.setOnPreparedListener { mp: MediaPlayer? -> mp?.isLooping = true }
        v.description.visibility = View.VISIBLE
        v.image.visibility = View.VISIBLE
    }
}