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

import android.content.Context
import androidx.emoji.text.EmojiCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.TIME_CHANNEL_ID
import com.javinator9889.handwashingreminder.utils.Workers
import com.javinator9889.handwashingreminder.utils.timeDifferenceSecs


data class NotificationStructure(
    val title: CharSequence,
    val content: CharSequence
)

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
        val workHandler = WorkHandler.getInstance()
        val notificationData = when (params.inputData.getInt(Workers.WHO, -1)) {
            Workers.BREAKFAST -> {
                val savedTime =
                    sharedPreferences.getString(Preferences.LUNCH_TIME, "")!!
                val data = Data.Builder()
                    .putInt(Workers.WHO, Workers.LUNCH)
                    .build()
                workHandler.enqueueNotificationsWorker(
                    timeDifferenceSecs(savedTime), data
                )
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
                workHandler.enqueueNotificationsWorker(
                    timeDifferenceSecs(savedTime), data
                )
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
                workHandler.enqueueNotificationsWorker(
                    timeDifferenceSecs(savedTime), data
                )
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
}
