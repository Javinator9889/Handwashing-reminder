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

import androidx.annotation.IntRange
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs


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
        if (nowTime == alarmTime || nowTime.isAfter(alarmTime)) {
            now = now.plusDays(1)
        }
        now = now
            .withHour(alarmTime.hour)
            .withMinute(alarmTime.minute)
        abs(Duration.between(LocalDateTime.now(), now).toMillis())
    } else {
        // get current time
        val now = Calendar.getInstance()
        // get again current time but truncate it to minutes and with the
        // specified hour:minute provided
        val alarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        val nowTime = now.time
        val alarmTime = alarm.time
        // check if they are the same time or if today's time has passed so
        // then schedule for next day
        if (nowTime == alarmTime || nowTime.after(alarmTime)) {
            alarm.add(Calendar.HOUR_OF_DAY, 24)
        }
        abs(alarm.timeInMillis - now.timeInMillis)
    }

fun timeAt(
    @IntRange(from = 0, to = 23) hour: Int,
    @IntRange(from = 0, to = 59) minute: Int
): Long =
    if (isAtLeast(AndroidVersion.O)) {
        // trigger at hour:minute
        val alarmTime = LocalTime.of(hour, minute)
        // obtain local date-time with fixed timezone (system default)
        var now = LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.MINUTES)
        val nowTime = now.toLocalTime()
        // check if is the same time or if today's time has passed so
        // then schedule for next day
        if (nowTime == alarmTime || nowTime.isAfter(alarmTime)) {
            now = now.plusDays(1)
        }
        val scheduledTime = now
            .withHour(alarmTime.hour)
            .withMinute(alarmTime.minute)
            .withZoneSameInstant(ZoneOffset.UTC)
        scheduledTime.toInstant().toEpochMilli()
    } else {
        // get now time and set to system's millis
        val now = Calendar.getInstance()
            .apply { timeInMillis = System.currentTimeMillis() }
        // clone now calendar epoch time and set the specified hour and
        // minute in the new Calendar object
        val alarm = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowTime = now.time
        val alarmTime = alarm.time
        // check if they are the same time or if today's time has passed so
        // then schedule for next day
        if (nowTime == alarmTime || nowTime.after(alarmTime)) {
            alarm.add(Calendar.HOUR_OF_DAY, 24)
        }
        alarm.timeInMillis
    }
