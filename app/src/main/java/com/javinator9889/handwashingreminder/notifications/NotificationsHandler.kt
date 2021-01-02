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
 * Created by Javinator9889 on 15/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.notifications

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.FAST_START_KEY
import com.javinator9889.handwashingreminder.activities.LauncherActivity
import com.javinator9889.handwashingreminder.activities.PENDING_INTENT_CODE
import com.javinator9889.handwashingreminder.jobs.NOTIFICATION_ID_KEY
import com.javinator9889.handwashingreminder.jobs.ShareReceiver
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.isAtLeast
import com.javinator9889.handwashingreminder.utils.notNull
import java.util.concurrent.atomic.AtomicInteger

class NotificationsHandler(
    private val context: Context,
    private val channelId: String,
    private val channelName: String = "",
    private val channelDesc: String = "",
    groupId: String = "",
    groupName: String = ""
) {
    companion object NotificationId {
        val id = AtomicInteger(0)
    }

    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val vibrationPattern = longArrayOf(300L, 300L, 300L, 300L)
    private val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    init {
        if (groupId.isNotEmpty() && groupName.isNotEmpty()
            && isAtLeast(AndroidVersion.O)
        ) {
            manager.createNotificationChannelGroup(
                NotificationChannelGroup(
                    groupId,
                    groupName
                )
            )
        }
        createNotificationChannel(groupId)
    }

    fun createNotification(
        @DrawableRes iconDrawable: Int,
        @DrawableRes largeIcon: Int,
        @StringRes title: Int,
        @StringRes content: Int,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        @StringRes longContent: Int = -1,
        action: Action? = null
    ) {
        val longContentProcessed =
            if (longContent != -1) context.getText(longContent) else null
        createNotification(
            iconDrawable,
            largeIcon,
            context.getText(title),
            context.getText(content),
            priority,
            longContentProcessed,
            action
        )
    }

    fun createNotification(
        @DrawableRes iconDrawable: Int,
        @DrawableRes largeIcon: Int,
        title: CharSequence,
        content: CharSequence,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        longContent: CharSequence? = null,
        action: Action? = null
    ) {
        val bitmapIcon = if (isAtLeast(AndroidVersion.JELLY_BEAN_MR2)) {
            if (isAtLeast(AndroidVersion.P)) {
                val source =
                    ImageDecoder.createSource(context.resources, largeIcon)
                ImageDecoder.decodeBitmap(source)
            } else {
                BitmapFactory.decodeResource(
                    context.resources,
                    largeIcon
                )
            }
        } else {
            null
        }
        createNotification(
            iconDrawable,
            bitmapIcon,
            title,
            content,
            priority,
            longContent,
            action
        )
    }

    fun createNotification(
        @DrawableRes iconDrawable: Int,
        largeIcon: Bitmap?,
        title: CharSequence,
        content: CharSequence,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        longContent: CharSequence? = null,
        action: Action? = null
    ) {
        val notifyIntent = Intent(context, LauncherActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(FAST_START_KEY, true)
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            context,
            PENDING_INTENT_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val sharePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, ShareReceiver::class.java),
            0
        )
        val notificationId = id.getAndIncrement()
        with(NotificationCompat.Builder(context, channelId)) {
            setSmallIcon(iconDrawable)
            setLargeIcon(largeIcon)
            setContentTitle(title)
            setContentText(content)
            setPriority(priority)
            setVibrate(vibrationPattern)
            setContentIntent(notifyPendingIntent)
            action?.let {
                action.intent.putExtra(NOTIFICATION_ID_KEY, notificationId)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    action.requestCode,
                    action.intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                addAction(action.drawable, action.text, pendingIntent)
            }
            addAction(
                R.drawable.ic_share_black,
                context.getString(R.string.share),
                sharePendingIntent
            )
            setAutoCancel(true)
            longContent.notNull {
                setStyle(NotificationCompat.BigTextStyle().bigText(longContent))
            }
            build()
        }.let {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, it)
            }
        }
    }

    private fun createNotificationChannel(groupId: String = "") {
        if (isAtLeast(AndroidVersion.O)) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val that = this
            val channel =
                NotificationChannel(channelId, channelName, importance)
                    .apply {
                        description = channelDesc
                        vibrationPattern = that.vibrationPattern
                        enableVibration(true)
                        if (groupId.isNotEmpty())
                            group = groupId
                    }
            manager.createNotificationChannel(channel)
        }
    }

    private fun isNotificationChannelCreated(): Boolean {
        if (isAtLeast(AndroidVersion.O)) {
            val channel = manager.getNotificationChannel(channelId)
            channel?.let {
                return it.importance != NotificationManager.IMPORTANCE_NONE
            } ?: return false
        } else {
            return NotificationManagerCompat.from(context)
                .areNotificationsEnabled()
        }
    }

    private fun createChannelRequired() =
        preferences.getBoolean(Preferences.CREATE_CHANNEL_KEY, true)
}

data class Action(
    @DrawableRes val drawable: Int,
    val text: CharSequence,
    val requestCode: Int,
    val intent: Intent
)