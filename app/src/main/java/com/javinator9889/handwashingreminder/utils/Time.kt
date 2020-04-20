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

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun timeDifferenceSecs(to: String): Long {
    if (to == "") return 0L
    val dateFormat = SimpleDateFormat("HH:mm")
    val fromDate = dateFormat.parse(to) ?: return 0L
    val cTime = Calendar.getInstance().time
    val diff = fromDate.time - cTime.time
    dateFormat.parse(dateFormat.format(diff))?.let { return it.time / 1000 }
    return 0L
}

fun formatTime(time: Int) = if (time < 10) "0$time" else time.toString()