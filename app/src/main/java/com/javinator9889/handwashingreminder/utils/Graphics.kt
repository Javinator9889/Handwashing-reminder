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
 * Created by Javinator9889 on 11/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.utils

import com.javinator9889.handwashingreminder.application.HandwashingApplication

/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 *
 * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun dpToPx(dp: Float): Float {
    val context = HandwashingApplication.instance.applicationContext
    return dp * context.resources.displayMetrics.density
}

/**
 * This method converts device specific pixels to density independent pixels.
 *
 * @param px      A value in px (pixels) unit. Which we need to convert into db
 * @return A float value to represent dp equivalent to px value
 */
fun pxToDp(px: Float): Float {
    val context = HandwashingApplication.instance.applicationContext
    return px / context.resources.displayMetrics.density
}