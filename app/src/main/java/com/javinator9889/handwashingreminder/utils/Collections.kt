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

import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.abs

fun <T> Array<T>?.notEmpty(f: (it: Array<T>) -> Unit) {
    if (!this.isNullOrEmpty()) f(this)
}

inline fun <reified T : CharSequence?> Array<T>?.filterNotEmpty(): Array<T> {
    if (this.isNullOrEmpty()) return emptyArray()
    val notEmptyItems = ArrayList<T>()
    for (element in this)
        if (element != null && element != "") notEmptyItems.add(element)
    notEmptyItems.trimToSize()
    return notEmptyItems.toTypedArray()
}

fun List<Date>.closest(): Date {
    val now = System.currentTimeMillis()
    return Collections.min(
        this,
        Comparator { date1, date2 ->
            val diff1 = abs(date1.time - now)
            val diff2 = abs(date2.time - now)
            return@Comparator diff1.compareTo(diff2)
        })
}