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
package com.javinator9889.handwashingreminder.jobs.workers

import android.content.Context
import android.content.Intent
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.jobs.*
import com.javinator9889.handwashingreminder.jobs.alarms.AlarmHandler
import com.javinator9889.handwashingreminder.jobs.alarms.Alarms
import com.javinator9889.handwashingreminder.notifications.Action
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

abstract class ScheduledNotificationWorker(context: Context) {
    protected val context: Context = context.applicationContext
    protected abstract val alarm: Alarms
    protected abstract val titleRes: Int
    protected abstract val contentsRes: Int

    suspend fun doWork() = coroutineScope {
        var result: Result<Unit> = Result.success(Unit)
        try {
            val emojiLoader = EmojiLoader.loadAsync(context)
            val notificationsHandler = NotificationsHandler(
                context = context,
                channelId = alarm.channelId,
                channelName = getString(alarm.channelName),
                channelDesc = getString(alarm.channelDesc),
                groupId = alarm.groupId,
                groupName = getString(alarm.groupName)
            )
            val emojiCompat = emojiLoader.await()
            if (titleRes == -1 && contentsRes == -1)
                return@coroutineScope
            var title = getText(titleRes)
            var content =
                getStringArray(contentsRes).toList().random() as CharSequence
            try {
                title = emojiCompat.process(title)
                content = emojiCompat.process(content)
            } catch (_: IllegalStateException) {
            }
            withContext(Dispatchers.Main) {
                notificationsHandler.createNotification(
                    iconDrawable = R.drawable.ic_stat_handwashing,
                    largeIcon = R.drawable.handwashing_app_logo,
                    title = title,
                    content = content,
                    priority = NotificationCompat.PRIORITY_MAX,
                    longContent = content,
                    action = Action(
                        R.drawable.ic_stat_handwashing,
                        getString(R.string.just_washed),
                        HANDS_WASHED_SCHED_CODE,
                        Intent(context, HandsWashedReceiver::class.java).apply {
                            action = HANDS_WASHED_ACTION
                        }
                    )
                )
            }
        } catch (e: Throwable) {
            with(HandwashingApplication.instance) {
                // Don't use so much resources, wait at most half a second until
                // Firebase initializes or continue with execution.
                // Firebase is only needed for Timber (Crashlytics) so until
                // here there is no need to wait
                withTimeoutOrNull(500L) {
                    firebaseInitDeferred.await()
                }
            }
            result = Result.failure(e)
            // We don't want to keep using CPU at this time if the request
            // fails so schedule next execution
        } finally {
            onFinish(result)
        }
    }

    protected open fun onFinish(result: Result<Unit>) {
        if (result.isSuccess) {
            with(AlarmHandler(context)) {
                scheduleAlarm(alarm)
            }
        } else Timber.e(
            result.exceptionOrNull(),
            "Unhandled exception on worker class"
        )
    }

    private fun getString(@StringRes resId: Int): String =
        context.getString(resId)

    private fun getText(@StringRes resId: Int): CharSequence =
        context.getText(resId)

    private fun getStringArray(@ArrayRes resId: Int): Array<out String> =
        context.resources.getStringArray(resId)
}