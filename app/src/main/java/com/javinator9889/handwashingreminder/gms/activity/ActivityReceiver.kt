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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.emoji.text.EmojiCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import com.javinator9889.handwashingreminder.utils.ACTIVITY_CHANNEL_ID
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.calendar.CalendarUtils
import com.javinator9889.handwashingreminder.utils.goAsync
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit

class ActivityReceiver : BroadcastReceiver() {
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
                        )
                    )
                    goAsync {
                        putNotification(
                            notificationHandler,
                            emojiLoader,
                            event.activityType,
                            context
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
        detectedActivity: Int,
        context: Context
    ) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val timeInBetweenNotifications =
            preferences.getInt(Preferences.ACTIVITY_MINIMUM_TIME, 15)
        val timeFile = File(context.cacheDir, "activity.time")
        var latestNotificationTime = 0L
        withContext(Dispatchers.IO) {
            if (timeFile.exists()) {
                DataInputStream(FileInputStream(timeFile)).use {
                    latestNotificationTime = it.readLong()
                }
            }
        }
        val timeDifference = CalendarUtils.timeBetweenIn(
            TimeUnit.MINUTES,
            latestNotificationTime
        )
        if (timeDifference <= timeInBetweenNotifications)
            return
        val notificationContent = when (detectedActivity) {
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
                "Activity not recognized - $detectedActivity"
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
        withContext(Dispatchers.Main) {
            notificationsHandler.createNotification(
                iconDrawable = R.drawable.ic_stat_handwashing,
                largeIcon = R.drawable.handwashing_app_logo,
                title = title,
                content = content,
                longContent = content
            )
        }
        withContext(Dispatchers.IO) {
            DataOutputStream(FileOutputStream(timeFile)).use {
                it.writeLong(Calendar.getInstance().timeInMillis)
            }
        }
    }
}

private data class NotificationContent(
    @StringRes val title: Int,
    @StringRes val content: Int
)