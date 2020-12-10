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
 * Created by Javinator9889 on 24/06/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.utils.calendar

import java.util.*
import java.util.concurrent.TimeUnit

object CalendarUtils {
    val today: Calendar
        get() = with(Calendar.getInstance()) {
            this[Calendar.HOUR_OF_DAY] = 0
            this[Calendar.MINUTE] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MILLISECOND] = 0
            this
        }

    val lastWeek: Calendar
        get() {
            val aWeekAgo = today
            aWeekAgo.add(Calendar.DAY_OF_MONTH, -7)
            return aWeekAgo
        }

    val lastMonth: Calendar
        get() {
            val aMonthAgo = today
            aMonthAgo.add(Calendar.MONTH, -1)
            return aMonthAgo
        }

    val now: Long
        get() = Calendar.getInstance().timeInMillis

    fun timeBetweenIn(
        unit: TimeUnit,
        to: Long,
        from: Long = today.timeInMillis
    ): Long = unit.convert(timeBetween(to, from), TimeUnit.MILLISECONDS)

    fun timeBetween(
        to: Long,
        from: Long = today.timeInMillis
    ): Long = from - to

    fun timeIn(amount: Int, unit: TimeUnit) =
        with(Calendar.getInstance()) {
            when (unit) {
                TimeUnit.MILLISECONDS -> this[Calendar.MILLISECOND] += amount
                TimeUnit.SECONDS -> this[Calendar.SECOND] += amount
                TimeUnit.MINUTES -> this[Calendar.MINUTE] += amount
                TimeUnit.HOURS -> this[Calendar.HOUR_OF_DAY] += amount
                TimeUnit.DAYS -> this[Calendar.DAY_OF_MONTH] += amount
                else -> Unit
            }
            this
        }
}