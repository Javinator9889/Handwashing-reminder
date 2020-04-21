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
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import com.javinator9889.handwashingreminder.utils.TIME_CHANNEL_ID
import com.javinator9889.handwashingreminder.utils.Workers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*


data class NotificationStructure(
    @StringRes val title: Int,
    @ArrayRes val content: Int
)

class NotificationsWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val emojiLoader = EmojiLoader.get(context)
            val notificationsHandler = NotificationsHandler(
                context,
                TIME_CHANNEL_ID,
                context.getString(R.string.time_notification_channel_name),
                context.getString(R.string.time_notification_channel_desc)
            )
            val workHandler = WorkHandler(context)

            val notificationData =
                setNotificationData(
                    inputData.getInt(
                        Workers.WHO,
                        -1
                    )
                )
            val delay = nextExecutionDelay(inputData)
            if (delay == -1L)
                return@coroutineScope Result.failure()


            val emojiCompat = emojiLoader.await()
            var title: CharSequence
            var comment: CharSequence
            try {
                title =
                    emojiCompat.process(context.getString(notificationData.title))
                val comments =
                    context.resources.getStringArray(notificationData.content)
                comment = emojiCompat.process(comments.asList().random())
            } catch (_: IllegalStateException) {
                title = context.getText(notificationData.title)
                comment = context.resources
                    .getStringArray(notificationData.content).asList().random()
            }

            withContext(Dispatchers.Main) {
                notificationsHandler.createNotification(
                    R.drawable.ic_handwashing_icon,
                    R.drawable.handwashing_app_logo,
                    title,
                    comment,
                    longContent = comment
                )
            }

            with(Data.Builder()) {
                putAll(inputData)
                build()
            }.let {
                Timber.d(it.toString())
                workHandler.enqueueNotificationsWorker(delay, it)
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Uncaught exception on worker class")
            if (runAttemptCount < 5)
                Result.retry()
            else
                Result.failure()
        }
    }

    private fun setNotificationData(who: Int): NotificationStructure =
        when (who) {
            Workers.BREAKFAST ->
                NotificationStructure(
                    R.string.breakfast_title,
                    R.array.breakfast_comments
                )
            Workers.LUNCH ->
                NotificationStructure(
                    R.string.lunch_title,
                    R.array.lunch_comments
                )
            Workers.DINNER ->
                NotificationStructure(
                    R.string.dinner_title,
                    R.array.dinner_comments
                )
            else -> throw IllegalArgumentException("Worker $who not found")
        }

    private fun nextExecutionDelay(data: Data): Long {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()

        val hour = data.getInt(Workers.HOUR, -1)
        val minute = data.getInt(Workers.MINUTE, -1)
        if (hour == -1 || hour == -1) {
            Timber.e("Hour or minute not provided")
            return -1L
        }

        dueDate.set(Calendar.HOUR_OF_DAY, hour)
        dueDate.set(Calendar.MINUTE, minute)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate))
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        Timber.i("Next execution scheduled at: ${dueDate.time}")
        return dueDate.timeInMillis - currentDate.timeInMillis
    }
}
