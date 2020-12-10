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
package com.javinator9889.handwashingreminder.jobs.alarms

import androidx.annotation.StringRes
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.utils.ACTIVITY_CHANNEL_ID
import com.javinator9889.handwashingreminder.utils.Preferences

enum class Alarms(
    val identifier: String,
    val code: Int,
    val preferenceKey: String,
    val groupId: String,
    @StringRes val groupName: Int,
    val channelId: String,
    @StringRes val channelName: Int,
    @StringRes val channelDesc: Int
) {
    BREAKFAST_ALARM(
        "alarms:breakfast",
        0,
        Preferences.BREAKFAST_TIME,
        "alarms:scheduled",
        R.string.time_notification_channel_name,
        "notifications:breakfast",
        R.string.breakfast_notifications,
        R.string.breakfast_description
    ),
    LUNCH_ALARM(
        "alarms:lunch",
        1,
        Preferences.LUNCH_TIME,
        "alarms:scheduled",
        R.string.time_notification_channel_name,
        "notifications:lunch",
        R.string.lunch_notifications,
        R.string.lunch_description
    ),
    DINNER_ALARM(
        "alarms:dinner",
        2,
        Preferences.DINNER_TIME,
        "alarms:scheduled",
        R.string.time_notification_channel_name,
        "notifications:dinner",
        R.string.dinner_notifications,
        R.string.dinner_description
    ),
    PENDING_ACTIVITY_ALARM(
        "alarms:activities",
        3,
        Preferences.ACTIVITY_MINIMUM_TIME,
        "alarms:pending_activities",
        R.string.activity_notification_channel_name,
        ACTIVITY_CHANNEL_ID,
        R.string.activity_notification_channel_name,
        R.string.activity_notification_channel_desc
    )
}