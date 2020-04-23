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
import androidx.preference.PreferenceManager
import androidx.work.*
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.Workers
import com.javinator9889.handwashingreminder.utils.runAt
import timber.log.Timber
import java.util.concurrent.TimeUnit

data class Who(
    val uuid: String,
    val id: Int,
    val clazz: Class<out ListenableWorker>
)

class WorkHandler(private val context: Context) {
    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    fun enqueuePeriodicNotificationsWorker(forceUpdate: Boolean = false) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val breakfastTime =
            preferences.getString(Preferences.BREAKFAST_TIME, "")!!
        val lunchTime = preferences.getString(Preferences.LUNCH_TIME, "")!!
        val dinnerTime = preferences.getString(Preferences.DINNER_TIME, "")!!
        if (breakfastTime == "" || lunchTime == "" || dinnerTime == "") {
            Timber.e("The scheduled times are not initialized")
            return
        }
        val times = arrayOf(breakfastTime, lunchTime, dinnerTime)
        times.forEach { time ->
            val splittedTime = time.split(":")
            val hour = Integer.parseInt(splittedTime[0].trim())
            val minute = Integer.parseInt(splittedTime[1].trim())

            val timeDiff = runAt(hour, minute)
            val who = when (time) {
                breakfastTime -> Who(
                    Workers.BREAKFAST_UUID,
                    Workers.BREAKFAST,
                    BreakfastWorker::class.java
                )
                lunchTime -> Who(
                    Workers.LUNCH_UUID,
                    Workers.LUNCH,
                    LunchWorker::class.java
                )
                dinnerTime -> Who(
                    Workers.DINNER_UUID,
                    Workers.DINNER,
                    DinnerWorker::class.java
                )
                else -> {
                    Timber.e("Unmatched time: $time against $times")
                    return
                }
            }
            Timber.i(
                "Scheduled activity ${who.uuid} in $timeDiff ms"
            )

            val jobRequest = createJobRequest(timeDiff, who.clazz)

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

    private fun createJobRequest(
        initialDelayMillis: Long,
        clazz: Class<out ListenableWorker>,
        inputData: Data? = null
    ): OneTimeWorkRequest {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setRequiresStorageNotLow(false)
            .build()
        return with(OneTimeWorkRequest.Builder(clazz)) {
            setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            inputData?.let { setInputData(it) }
            setConstraints(constraints)
            setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15000L,
                TimeUnit.MILLISECONDS
            )
            build()
        }
    }
}