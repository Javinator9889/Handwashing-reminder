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
 * Created by Javinator9889 on 26/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.appintro.custom

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.model.SliderPage
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.utils.notNull

class SliderPageBuilder private constructor() {
    data class Builder(
        var title: String? = null,
        @FontRes var titleTypeface: Int? = R.font.raleway_bold,
        @FontRes var descTypeface: Int? = R.font.roboto,
        var description: String? = null,
        @DrawableRes var imageDrawable: Int? = null,
        @ColorInt var bgColor: Int? = Color.WHITE,
        @ColorInt var titleColor: Int? = Color.DKGRAY,
        @ColorInt var descColor: Int? = Color.DKGRAY,
        var customSlider: SliderPage? = null
    ) {
        fun title(title: String) = apply { this.title = title }
        fun titleTypeface(@FontRes typeface: Int) =
            apply { titleTypeface = typeface }

        fun descTypeface(@FontRes typeface: Int) =
            apply { descTypeface = typeface }

        fun description(description: String) =
            apply { this.description = description }

        fun imageDrawable(@DrawableRes imageDrawable: Int) = apply {
            this.imageDrawable = imageDrawable
        }

        fun bgColor(@ColorInt bgColor: Int) = apply { this.bgColor = bgColor }
        fun titleColor(@ColorRes titleColor: Int) =
            apply { this.titleColor = titleColor }

        fun descColor(@ColorRes descColor: Int) =
            apply { this.descColor = descColor }

        fun withCustomFragment(slider: SliderPage) =
            apply { this.customSlider = slider }

        fun build(): Fragment {
            val sliderPage = (customSlider ?: SliderPage())
            title.notNull { sliderPage.title = it }
            titleTypeface.notNull { sliderPage.titleTypefaceFontRes = it }
            descTypeface.notNull { sliderPage.descTypefaceFontRes = it }
            description.notNull { sliderPage.description = it }
            imageDrawable.notNull { sliderPage.imageDrawable = it }
            bgColor.notNull { sliderPage.bgColor = it }
            titleColor.notNull { sliderPage.titleColor = it }
            descColor.notNull { sliderPage.descColor = it }
            return AppIntro2Fragment.newInstance(sliderPage)
        }
    }
}