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

import androidx.annotation.ArrayRes
import androidx.annotation.RawRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.emoji.EmojiLoader

internal data class Measurements(var width: Int, var height: Int)

private const val IMAGE_DRAWABLE = "state:images"
private const val TITLE_STRING = "state:title"
private const val DESCRIPTION_STRING = "state:description"

class WashingHandsModel(
    private val state: SavedStateHandle,
    private val position: Int
) : ViewModel() {
    private val measurements = Measurements(0, 0)
    private var isViewMeasured = false
    val image: LiveData<Int> = liveData { emit(loadBitmap()) }
    val title: LiveData<CharSequence> = liveData { emit(loadTitle()) }
    val description: LiveData<CharSequence> = liveData {
        emit(loadDescription())
    }

    fun setImageSize(width: Int, height: Int) {
        measurements.width = width
        measurements.height = height
        isViewMeasured = true
    }

    private fun loadBitmap(): Int {
        val drawableId = getBitmapId()
        state.set(IMAGE_DRAWABLE, drawableId)
        return drawableId
    }

    private suspend fun loadTitle(): CharSequence {
        var titleText = state.get<CharSequence>(TITLE_STRING)
        if (titleText == null)
            titleText = processStringArray(R.array.washing_hands_titles)
        state.set(TITLE_STRING, titleText)
        return titleText
    }

    private suspend fun loadDescription(): CharSequence {
        var descriptionText = state.get<CharSequence>(DESCRIPTION_STRING)
        if (descriptionText == null)
            descriptionText =
                processStringArray(R.array.washing_hands_descriptions)
        state.set(DESCRIPTION_STRING, descriptionText)
        return descriptionText
    }

    private suspend fun processStringArray(@ArrayRes array: Int): CharSequence =
        with(HandwashingApplication.instance) {
            with(EmojiLoader.get(this)) {
                try {
                    this.await()
                        .process(resources.getStringArray(array)[position])
                } catch (_: IllegalStateException) {
                    resources.getStringArray(array)[position]
                }
            }
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

class WashingHandsModelFactory constructor(
    private val position: Int
) : ViewModelAssistedFactory<WashingHandsModel> {
    override fun create(handle: SavedStateHandle) =
        WashingHandsModel(handle, position)
}