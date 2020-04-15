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
 * Created by Javinator9889 on 15/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.viewmodels

import android.graphics.drawable.BitmapDrawable
import androidx.annotation.ArrayRes
import androidx.annotation.RawRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.emoji.EmojiCompat
import com.javinator9889.handwashingreminder.graphics.ImageCache
import com.javinator9889.handwashingreminder.graphics.ImageResizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

internal data class Measurements(var width: Int, var height: Int)

private const val IMAGE_DRAWABLE = "state:images"
private const val TITLE_STRING = "state:title"
private const val DESCRIPTION_STRING = "state:description"

class WashingHandsModel(
    private val state: SavedStateHandle,
    private val position: Int,
    private val imageCache: ImageCache
) : ViewModel() {
    private val measurements = Measurements(0, 0)
    private var isViewMeasured = false
    val image: LiveData<BitmapDrawable> = liveData { emit(loadBitmap()) }
    val title: LiveData<CharSequence> = liveData { emit(loadTitle()) }
    val description: LiveData<CharSequence> = liveData {
        emit(loadDescription())
    }

    fun setImageSize(width: Int, height: Int) {
        measurements.width = width
        measurements.height = height
        isViewMeasured = true
    }

    private suspend fun loadBitmap(): BitmapDrawable {
        val resources = HandwashingApplication.getInstance().resources
        return withContext(Dispatchers.IO) {
            Timber.d("Waiting for view measured")
            while (!isViewMeasured)
                delay(10L)
            Timber.d("View measured")
            Timber.d(measurements.toString())
            val drawable = getBitmapId()
//            BitmapDrawable(resources, BitmapFactory.decodeResource(resources,
//                drawable))
            state.set(IMAGE_DRAWABLE, drawable)
            BitmapDrawable(
                resources,
                ImageResizer.decodeSampledBitmapFromResource(
                    resources, drawable,
                    measurements.width, measurements.height, imageCache
                )
            )
        }
    }

    private fun loadTitle(): CharSequence {
        var titleText = state.get<CharSequence>(TITLE_STRING)
        if (titleText == null)
            titleText = processStringArray(R.array.washing_hands_titles)
        state.set(TITLE_STRING, titleText)
        return titleText
    }

    private fun loadDescription(): CharSequence {
        var descriptionText = state.get<CharSequence>(DESCRIPTION_STRING)
        if (descriptionText == null)
            descriptionText =
                processStringArray(R.array.washing_hands_descriptions)
        state.set(DESCRIPTION_STRING, descriptionText)
        return descriptionText
    }

    private fun processStringArray(@ArrayRes array: Int): CharSequence =
        with(HandwashingApplication.getInstance()) {
            val emojiCompat = EmojiCompat.get(this, true)
            Timber.d("EmojiCompat initialized")
            emojiCompat.process(
                resources.getStringArray(array)[position]
            )
        }

    @RawRes
    private fun getBitmapId(): Int =
        state[IMAGE_DRAWABLE] ?: when (position) {
            0 -> R.raw.rub_hands
            1 -> R.raw.rub_palms
            2 -> R.raw.rub_back
            3 -> R.raw.sides_interlocked
            4 -> R.raw.thumbs
            5 -> R.raw.middle_hand
            6 -> R.raw.rinse_hands
            else -> -1
        }
}

class WashingHandsModelFactory @Inject constructor(
    private val position: Int,
    private val imageCache: ImageCache
) : ViewModelAssistedFactory<WashingHandsModel> {
    override fun create(handle: SavedStateHandle) =
        WashingHandsModel(handle, position, imageCache)
}