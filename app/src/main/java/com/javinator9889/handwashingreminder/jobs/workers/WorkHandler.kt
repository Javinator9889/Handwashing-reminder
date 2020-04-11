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
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.javinator9889.handwashingreminder.utils.Workers
import java.util.concurrent.TimeUnit

class WorkHandler private constructor(private val context: Context?) {
    private val workManager: WorkManager

    init {
        if (context == null)
            throw IllegalStateException(
                "Context cannot be null on class creation"
            )
        workManager = WorkManager.getInstance(context)
    }

    companion object {
        private var instance: WorkHandler? = null

        fun getInstance(context: Context? = null): WorkHandler {
            instance = instance ?: WorkHandler(context)
            return instance!!
        }
    }

    fun enqueueNotificationsWorker(delaySeconds: Long, data: Data) {
        val jobRequest =
            OneTimeWorkRequest.Builder(NotificationsWorker::class.java)
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .setInputData(data)
                .build()
        with(workManager) {
            enqueueUniqueWork(
                Workers.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                jobRequest
            )
        }
    }
}