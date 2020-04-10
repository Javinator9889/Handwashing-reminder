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
 * Created by Javinator9889 on 10/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.jobs.workers

import android.annotation.SuppressLint
import android.content.Context
import androidx.emoji.text.EmojiCompat
import androidx.work.*
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.TIME_CHANNEL_ID
import com.javinator9889.handwashingreminder.utils.Workers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationsWorker(
    private val context: Context,
    private val params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val notificationsHandler = NotificationsHandler(
            context,
            TIME_CHANNEL_ID,
            context.getString(R.string.time_notification_channel_name),
            context.getString(R.string.time_notification_channel_desc)
        )
        val sharedPreferences =
            HandwashingApplication.getInstance().sharedPreferences
        val emojiCompat = EmojiCompat.get()
        val notificationData = when (params.inputData.getInt(Workers.WHO, -1)) {
            Workers.BREAKFAST -> {
                val savedTime =
                    sharedPreferences.getString(Preferences.LUNCH_TIME, "")!!
                val data = Data.Builder()
                    .putInt(Workers.WHO, Workers.LUNCH)
                    .build()
                enqueue(timeDifferenceSecs(savedTime), data)
                val comments =
                    context.resources.getStringArray(R.array.breakfast_comments)
                val title = context.getText(R.string.breakfast_title)
                val comment = emojiCompat.process(comments.asList().random())
                NotificationStructure(title, comment)
            }
            Workers.LUNCH -> {
                val savedTime =
                    sharedPreferences.getString(Preferences.DINNER_TIME, "")!!
                val data = Data.Builder()
                    .putInt(Workers.WHO, Workers.DINNER)
                    .build()
                enqueue(timeDifferenceSecs(savedTime), data)
                val comments =
                    context.resources.getStringArray(R.array.lunch_comments)
                val title = context.getText(R.string.lunch_title)
                val comment = emojiCompat.process(comments.asList().random())
                NotificationStructure(title, comment)
            }
            Workers.DINNER -> {
                val savedTime =
                    sharedPreferences.getString(
                        Preferences.BREAKFAST_TIME,
                        ""
                    )!!
                val data = Data.Builder()
                    .putInt(Workers.WHO, Workers.BREAKFAST)
                    .build()
                enqueue(timeDifferenceSecs(savedTime), data)
                val comments =
                    context.resources.getStringArray(R.array.dinner_comments)
                val title = context.getText(R.string.dinner_title)
                val comment = emojiCompat.process(comments.asList().random())
                NotificationStructure(title, comment)
            }
            else -> return Result.failure()
        }
        notificationsHandler.createNotification(
            R.drawable.ic_handwashing_icon,
            R.drawable.handwashing_app_logo,
            notificationData.title,
            notificationData.content
        )
        return Result.success()
    }

    @SuppressLint("SimpleDateFormat")
    private fun timeDifferenceSecs(to: String): Long {
        if (to == "") return 0L
        val dateFormat = SimpleDateFormat("HH:mm")
        val fromDate = dateFormat.parse(to) ?: return 0L
        val cTime = Calendar.getInstance().time
        val diff = fromDate.time - cTime.time
        dateFormat.parse(dateFormat.format(diff))?.let { return it.time / 1000 }
        return 0L
    }

    private fun enqueue(delay: Long, data: Data) {
        val jobRequest = OneTimeWorkRequest.Builder(this::class.java)
            .setInitialDelay(delay, TimeUnit.SECONDS)
            .setInputData(data)
            .build()
        with(WorkManager.getInstance(context)) {
            enqueueUniqueWork(
                Workers.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                jobRequest
            )
        }
    }

}

data class NotificationStructure(
    val title: CharSequence,
    val content: CharSequence
)
