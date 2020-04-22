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
import androidx.annotation.IntRange
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
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

fun runAt(
    @IntRange(from = 0, to = 23) hour: Int,
    @IntRange(from = 0, to = 59) minute: Int
): Long =
    if (isAtLeast(AndroidVersion.O)) {
        // trigger at hour:minute
        val alarmTime = LocalTime.of(hour, minute)
        var now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        val nowTime = now.toLocalTime()
        // check if is the same time or if today's time has passed so
        // then schedule for next day
        if (nowTime == alarmTime || nowTime.isAfter(alarmTime))
            now.plusDays(1)
        now = now
            .withHour(alarmTime.hour)
            .withMinute(alarmTime.minute)
        Duration.between(LocalDateTime.now(), now).toMillis()
    } else {
        // get now time and truncate it to minutes
        val now = with(Calendar.getInstance()) {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            this
        }
        // clone now time truncated to minutes and set the specified hour and
        // minute in the new Calendar object
        val alarm = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val nowTime = now.time
        val alarmTime = alarm.time
        // check if they are the same time or if today's time has passed so
        // then schedule for next day
        if (nowTime == alarmTime || nowTime.after(alarmTime)) {
            alarm.add(Calendar.HOUR_OF_DAY, 24)
        }
        alarm.timeInMillis - now.timeInMillis
    }

