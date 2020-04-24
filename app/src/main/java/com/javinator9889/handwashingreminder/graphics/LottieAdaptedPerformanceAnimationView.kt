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
package com.javinator9889.handwashingreminder.graphics

import android.content.Context
import android.util.AttributeSet
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieOnCompositionLoadedListener
import com.javinator9889.handwashingreminder.utils.isHighPerformingDevice

class LottieAdaptedPerformanceAnimationView : LottieAnimationView,
    LottieOnCompositionLoadedListener {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, attrStyle: Int):
            super(context, attrs, attrStyle)

    init {
        addLottieOnCompositionLoadedListener(this)
        enableMergePathsForKitKatAndAbove(true)
        setCacheComposition(true)
    }

    override fun getDuration(): Long =
        if (isHighPerformingDevice()) super.getDuration() else 100L

    override fun onCompositionLoaded(composition: LottieComposition?) {
        if (!isHighPerformingDevice()) {
            setMinFrame(maxFrame.toInt())
            repeatCount = 0
        }
    }
}