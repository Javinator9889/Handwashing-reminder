/*
 * Copyright © 2021 - present | Handwashing reminder by Javinator9889
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
 * Created by Javinator9889 on 2/01/21 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.jobs.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.javinator9889.handwashingreminder.jobs.alarms.AlarmHandler
import com.javinator9889.handwashingreminder.jobs.alarms.Alarms
import timber.log.Timber

class AlarmCheckerWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    private val alarmHandler = AlarmHandler(context)

    override fun doWork(): Result =
        try {
            for (alarm in Alarms.values()) {
                if (!alarmHandler.isAlarmAlive(alarm))
                    alarmHandler.scheduleAlarm(alarm)
            }
            Result.success()
        } catch (e: Throwable) {
            Timber.e(e, "Unexpected error while checking alive alarms!")
            Result.retry()
        }
}