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
import androidx.work.Data
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.jobs.workers.WorkHandler
import java.text.SimpleDateFormat

data class WorkQueueItems(val delay: Long, val data: Data)

@SuppressLint("SimpleDateFormat")
fun workManagerEnqueuer() {
    val app = HandwashingApplication.getInstance()
    val preferences = app.sharedPreferences
    val workHandler = WorkHandler.getInstance()
    val breakfastTime = preferences.getString(Preferences.BREAKFAST_TIME, "")!!
    val lunchTime = preferences.getString(Preferences.LUNCH_TIME, "")!!
    val dinnerTime = preferences.getString(Preferences.DINNER_TIME, "")!!
    if (breakfastTime == "" || lunchTime == "" || dinnerTime == "")
        throw UninitializedPropertyAccessException(
            "The scheduled time values are not initialized"
        )
    val dateFormat = SimpleDateFormat("HH:mm")

    val formattedBreakfastTime = dateFormat.parse(breakfastTime)!!
    val formattedLunchTime = dateFormat.parse(lunchTime)!!
    val formattedDinnerTime = dateFormat.parse(dinnerTime)!!

    val formattedTimes =
        listOf(formattedBreakfastTime, formattedLunchTime, formattedDinnerTime)
    val closestScheduledTime = formattedTimes.closest()

    val work = when (closestScheduledTime.time) {
        formattedBreakfastTime.time ->
            with(Data.Builder()) {
                putInt(Workers.WHO, Workers.BREAKFAST)
                build()
            }.let { WorkQueueItems(timeDifferenceSecs(breakfastTime), it) }
        formattedLunchTime.time ->
            with(Data.Builder()) {
                putInt(Workers.WHO, Workers.LUNCH)
                build()
            }.let { WorkQueueItems(timeDifferenceSecs(lunchTime), it) }
        formattedDinnerTime.time ->
            with(Data.Builder()) {
                putInt(Workers.WHO, Workers.DINNER)
                build()
            }.let { WorkQueueItems(timeDifferenceSecs(dinnerTime), it) }
        else -> return  // This should never happen
    }
    workHandler.enqueueNotificationsWorker(work.delay, work.data)
}