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
package com.javinator9889.handwashingreminder.jobs.workers

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.Workers
import java.util.*
import java.util.concurrent.TimeUnit

data class Who(val uuid: String, val id: Int)

class WorkHandler private constructor(private val context: Context?) {
    private val workManager: WorkManager
        get() = WorkManager.getInstance(context!!)

    init {
        if (context == null)
            throw IllegalStateException(
                "Context cannot be null on class creation"
            )
    }

    companion object {
        private var instance: WorkHandler? = null

        fun getInstance(context: Context? = null): WorkHandler {
            instance = instance ?: WorkHandler(context)
            return instance!!
        }
    }

    fun enqueuePeriodicNotificationsWorker(forceUpdate: Boolean = false) {
        val currentDate = Calendar.getInstance()
        val app = HandwashingApplication.getInstance()
        val preferences = app.sharedPreferences

        val breakfastTime =
            preferences.getString(Preferences.BREAKFAST_TIME, "")!!
        val lunchTime = preferences.getString(Preferences.LUNCH_TIME, "")!!
        val dinnerTime = preferences.getString(Preferences.DINNER_TIME, "")!!
        if (breakfastTime == "" || lunchTime == "" || dinnerTime == "")
            throw UninitializedPropertyAccessException(
                "The scheduled time values are not initialized"
            )
        val times = listOf(breakfastTime, lunchTime, dinnerTime)
        times.forEach { time ->
            val dueDate = Calendar.getInstance()
            val splittedTime = time.split(":")
            val hour = Integer.parseInt(splittedTime[0].trim())
            val minute = Integer.parseInt(splittedTime[1].trim())

            dueDate.set(Calendar.HOUR_OF_DAY, hour)
            dueDate.set(Calendar.MINUTE, minute)
            dueDate.set(Calendar.SECOND, 0)
            if (dueDate.before(currentDate))
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

            val who = when (time) {
                breakfastTime -> Who(Workers.BREAKFAST_UUID, Workers.BREAKFAST)
                lunchTime -> Who(Workers.LUNCH_UUID, Workers.LUNCH)
                dinnerTime -> Who(Workers.DINNER_UUID, Workers.DINNER)
                else -> return  // This should never happen
            }

            val data = with(Data.Builder()) {
                putInt(Workers.WHO, who.id)
                putInt(Workers.HOUR, hour)
                putInt(Workers.MINUTE, minute)
                build()
            }
            val jobRequest =
                OneTimeWorkRequestBuilder<NotificationsWorker>()
                    .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build()

            val policy = if (forceUpdate)
                ExistingWorkPolicy.REPLACE
            else
                ExistingWorkPolicy.KEEP

            with(workManager) {
                enqueueUniqueWork(
                    who.uuid,
                    policy,
                    jobRequest
                )
            }
        }
    }

    fun enqueueNotificationsWorker(delay: Long, data: Data) {
        val jobRequest =
            OneTimeWorkRequestBuilder<NotificationsWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()
        val who = when (data.getInt(Workers.WHO, -1)) {
            Workers.BREAKFAST -> Workers.BREAKFAST_UUID
            Workers.LUNCH -> Workers.LUNCH_UUID
            Workers.DINNER -> Workers.DINNER_UUID
            else -> return
        }
        with(workManager) {
            enqueueUniqueWork(
                who,
                ExistingWorkPolicy.APPEND,
                jobRequest
            )
        }
    }
}