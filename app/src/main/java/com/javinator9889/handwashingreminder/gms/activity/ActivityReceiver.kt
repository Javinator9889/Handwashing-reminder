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
package com.javinator9889.handwashingreminder.gms.activity

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.emoji.text.EmojiCompat
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.jobs.HANDS_WASHED_ACTION
import com.javinator9889.handwashingreminder.jobs.HANDS_WASHED_CODE
import com.javinator9889.handwashingreminder.jobs.HandsWashedReceiver
import com.javinator9889.handwashingreminder.jobs.alarms.AlarmHandler
import com.javinator9889.handwashingreminder.jobs.alarms.Alarms
import com.javinator9889.handwashingreminder.notifications.Action
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import com.javinator9889.handwashingreminder.utils.ACTIVITY_CHANNEL_ID
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.calendar.CalendarUtils
import com.javinator9889.handwashingreminder.utils.goAsync
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class ActivityReceiver : BroadcastReceiver() {
    companion object ReceiverData {
        val pendingActivities: Deque<ActivityTransitionEvent> = ArrayDeque()
        var latestNotificationTime: Long = 0L
        var alarmScheduled: Boolean = false
    }

    /**
     * {@inheritDoc}
     */
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("Detected user activity - ${intent.action}")
        if (ActivityTransitionResult.hasResult(intent)) {
            val emojiLoader = EmojiLoader.loadAsync(context)
            val result = ActivityTransitionResult.extractResult(intent)
            result?.let {
                for (event in result.transitionEvents) {
                    if (event.transitionType !=
                        ActivityTransition.ACTIVITY_TRANSITION_EXIT ||
                        event.activityType == DetectedActivity.UNKNOWN
                    )
                        continue
                    val notificationHandler = NotificationsHandler(
                        context,
                        ACTIVITY_CHANNEL_ID,
                        context.getString(
                            R.string.activity_notification_channel_name
                        ),
                        context.getString(
                            R.string.activity_notification_channel_desc
                        ),
                        "alarms:pending_activities",
                        context.getString(
                            R.string.activity_notification_channel_name
                        )
                    )
                    goAsync {
                        putNotification(
                            notificationHandler,
                            emojiLoader,
                            event,
                            context,
                            intent
                        )
                    }
                    break
                }
            } ?: Timber.w("Received unmatched activity - $intent")
        }
    }

    private suspend fun putNotification(
        notificationsHandler: NotificationsHandler,
        emojiLoader: Deferred<EmojiCompat>,
        event: ActivityTransitionEvent,
        context: Context,
        intent: Intent
    ) {
        val timeInBetweenNotifications =
            intent.getIntExtra(Preferences.ACTIVITY_MINIMUM_TIME, 15)
        val timeDifference = CalendarUtils.timeBetweenIn(
            TimeUnit.MINUTES,
            latestNotificationTime
        )
        Timber.d("$timeDifference - $timeInBetweenNotifications")
        if (timeDifference < timeInBetweenNotifications) {
            pendingActivities.add(event)
            if (!alarmScheduled) {
                with(AlarmHandler(context)) {
                    scheduleAlarm(Alarms.PENDING_ACTIVITY_ALARM)
                }
                alarmScheduled = true
            }
            return
        }
        latestNotificationTime = CalendarUtils.now
        val notificationContent = when (event.activityType) {
            DetectedActivity.WALKING ->
                NotificationContent(
                    R.string.activity_notification_walk,
                    R.string.activity_notification_walk_content
                )
            DetectedActivity.RUNNING ->
                NotificationContent(
                    R.string.activity_notification_run,
                    R.string.activity_notifications_run_content
                )
            DetectedActivity.ON_BICYCLE ->
                NotificationContent(
                    R.string.activity_notification_cycling,
                    R.string.activity_notification_cycling_content
                )
            DetectedActivity.IN_VEHICLE ->
                NotificationContent(
                    R.string.activity_notification_vehicle,
                    R.string.activity_notification_vehicle_content
                )
            else -> throw IllegalArgumentException(
                "Activity not recognized - $event"
            )
        }
        var title = context.getText(notificationContent.title)
        var content = context.getText(notificationContent.content)
        try {
            val emojiCompat = emojiLoader.await()
            title = emojiCompat.process(title)
            content = emojiCompat.process(content)
        } catch (_: IllegalStateException) {
        }
        val washedPendingIntent = PendingIntent.getBroadcast(
            context,
            HANDS_WASHED_CODE,
            Intent(context, HandsWashedReceiver::class.java).apply {
                action = HANDS_WASHED_ACTION
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        withContext(Dispatchers.Main) {
            notificationsHandler.createNotification(
                iconDrawable = R.drawable.ic_stat_handwashing,
                largeIcon = R.drawable.handwashing_app_logo,
                title = title,
                content = content,
                longContent = content,
                notificationId = 2,
                priority = NotificationCompat.PRIORITY_MAX,
                action = Action(
                    R.drawable.ic_stat_handwashing,
                    context.getText(R.string.just_washed),
                    washedPendingIntent
                )
            )
        }
    }
}

private data class NotificationContent(
    @StringRes val title: Int,
    @StringRes val content: Int
)