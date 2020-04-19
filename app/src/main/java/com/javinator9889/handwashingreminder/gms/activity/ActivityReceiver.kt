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
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import com.javinator9889.handwashingreminder.utils.ACTIVITY_CHANNEL_ID
import kotlinx.coroutines.*

class ActivityReceiver : BroadcastReceiver() {
    /**
     * {@inheritDoc}
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val emojiLoader = EmojiLoader.get(context)
            val result = ActivityTransitionResult.extractResult(intent)!!
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
                putNotification(
                    notificationHandler,
                    emojiLoader,
                    event.activityType,
                    context
                )
                break
            }
        }
    }

    private fun putNotification(
        notificationsHandler: NotificationsHandler,
        emojiLoader: CompletableDeferred<EmojiCompat>,
        detectedActivity: Int,
        context: Context,
        coroutineScope: CoroutineScope = GlobalScope
    ) {
        val result = goAsync()
        coroutineScope.launch {
            try {
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
                        "Activity not recognized"
                    )
                }
                val emojiCompat = emojiLoader.await()
                val title = emojiCompat.process(
                    context.getText(notificationContent.title)
                )
                val content = emojiCompat.process(
                    context.getText(notificationContent.content)
                )
                withContext(Dispatchers.Main) {
                    notificationsHandler.createNotification(
                        R.drawable.ic_stat_handwashing,
                        R.drawable.handwashing_app_logo,
                        title,
                        content,
                        longContent = content
                    )
                }
            } finally {
                result.finish()
            }
        }
    }

    private data class NotificationContent(
        @StringRes val title: Int,
        @StringRes val content: Int
    )
}