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
 * Created by Javinator9889 on 9/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.appintro.custom

import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.RawRes

internal const val ARG_TITLE = "title"
internal const val ARG_TITLE_TYPEFACE = "title_typeface"
internal const val ARG_TITLE_TYPEFACE_RES = "title_typeface_res"
internal const val ARG_DESC = "desc"
internal const val ARG_DESC_TYPEFACE = "desc_typeface"
internal const val ARG_DESC_TYPEFACE_RES = "desc_typeface_res"
internal const val ARG_DRAWABLE = "drawable"
internal const val ARG_BG_COLOR = "bg_color"
internal const val ARG_TITLE_COLOR = "title_color"
internal const val ARG_DESC_COLOR = "desc_color"
internal const val ARG_ANIM_DRAWABLE = "animated_drawable"
internal const val ARG_ANIM_LOOP = "animation_loop"


data class AnimatedSliderPage @JvmOverloads constructor(
    var title: CharSequence? = null,
    var description: CharSequence? = null,
    @DrawableRes var imageDrawable: Int = 0,
    @RawRes var animatedDrawable: Int? = null,
    var animationLoop: Boolean = false,
    @ColorInt var bgColor: Int = 0,
    @ColorInt var titleColor: Int = 0,
    @ColorInt var descColor: Int = 0,
    @FontRes var titleTypefaceFontRes: Int = 0,
    @FontRes var descTypefaceFontRes: Int = 0,
    var titleTypeface: String? = null,
    var descTypeface: String? = null,
    @DrawableRes var bgDrawable: Int = 0
) {
    val titleString: String? get() = title?.toString()
    val descriptionString: String? get() = description?.toString()

    /**
     * Util method to convert a [SliderPage] into an Android [Bundle].
     * This method will be used to pass the [SliderPage] to [AppIntroBaseFragment] implementations.
     */
    fun toBundle(): Bundle {
        val newBundle = Bundle()
        newBundle.putString(ARG_TITLE, this.titleString)
        newBundle.putString(ARG_TITLE_TYPEFACE, this.titleTypeface)
        newBundle.putInt(ARG_TITLE_TYPEFACE_RES, this.titleTypefaceFontRes)
        newBundle.putInt(ARG_TITLE_COLOR, this.titleColor)
        newBundle.putString(ARG_DESC, this.descriptionString)
        newBundle.putString(ARG_DESC_TYPEFACE, this.descTypeface)
        newBundle.putInt(ARG_DESC_TYPEFACE_RES, this.descTypefaceFontRes)
        newBundle.putInt(ARG_DESC_COLOR, this.descColor)
        newBundle.putInt(ARG_DRAWABLE, this.imageDrawable)
        newBundle.putInt(ARG_BG_COLOR, this.bgColor)
        newBundle.putInt(ARG_DRAWABLE, this.bgDrawable)
        this.animatedDrawable?.let { newBundle.putInt(ARG_ANIM_DRAWABLE, it) }
        newBundle.putBoolean(ARG_ANIM_LOOP, this.animationLoop)
        return newBundle
    }
}
