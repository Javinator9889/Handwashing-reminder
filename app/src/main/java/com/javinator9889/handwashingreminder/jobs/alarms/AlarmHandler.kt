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
 * Created by Javinator9889 on 23/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.jobs.alarms

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import androidx.core.app.AlarmManagerCompat
import androidx.preference.PreferenceManager
import com.javinator9889.handwashingreminder.utils.timeAt
import timber.log.Timber

internal const val IDENTIFIER = "intent:id"

class AlarmHandler(private val context: Context) {
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarms) {
        try {
            cancelAlarm(alarm)
            val pendingIntent = createPendingIntentForAlarm(alarm)
            val alarmTime = getTimeForAlarm(alarm)
            val scheduleTime = timeAt(alarmTime.hour, alarmTime.minute)
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager, RTC_WAKEUP, scheduleTime, pendingIntent
            )
        } catch (_: IllegalStateException) {
            Timber.i("Time values are not initialized yet")
        }
    }

    fun scheduleAllAlarms() {
        cancelAllAlarms()
        for (alarm in Alarms.values())
            scheduleAlarm(alarm)
    }

    fun cancelAlarm(alarm: Alarms) {
        val pendingIntent = createPendingIntentForAlarm(alarm)
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAllAlarms() {
        for (alarm in Alarms.values())
            cancelAlarm(alarm)
    }

    private fun createPendingIntentForAlarm(alarm: Alarms): PendingIntent {
        return with(Intent(context, AlarmReceiver::class.java)) {
            putExtra(IDENTIFIER, alarm.identifier)
            PendingIntent.getBroadcast(context, alarm.code, this, 0)
        }
    }

    private fun getTimeForAlarm(alarm: Alarms): ScheduleTimeData {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val savedTime = preferences.getString(alarm.preferenceKey, "")
        if (savedTime.isNullOrBlank())
            throw IllegalStateException("Time value cannot be null")
        val splitTime = savedTime.split(":")
        val hour = Integer.parseInt(splitTime[0])
        val minute = Integer.parseInt(splitTime[1])
        return ScheduleTimeData(hour, minute)
    }
}

private data class ScheduleTimeData(
    @IntRange(from = 0, to = 23) val hour: Int,
    @IntRange(from = 0, to = 23) val minute: Int
)