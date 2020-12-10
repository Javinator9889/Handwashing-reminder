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
 * Created by Javinator9889 on 10/12/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.jobs.workers

import android.content.Context
import com.google.android.gms.location.DetectedActivity
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.gms.activity.ActivityReceiver
import com.javinator9889.handwashingreminder.jobs.alarms.AlarmHandler
import com.javinator9889.handwashingreminder.jobs.alarms.Alarms

class PendingActivityAlarmWorker(context: Context) :
    ScheduledNotificationWorker(context) {
    private val notificationContent: Pair<Int, Int>
        get() = when (ActivityReceiver.pendingActivities.poll()?.activityType) {
            DetectedActivity.WALKING ->
                R.string.activity_notification_walk to R.string.activity_notification_walk_content
            DetectedActivity.RUNNING ->
                R.string.activity_notification_run to R.string.activity_notifications_run_content
            DetectedActivity.ON_BICYCLE ->
                R.string.activity_notification_cycling to R.string.activity_notification_cycling_content
            DetectedActivity.IN_VEHICLE ->
                R.string.activity_notification_vehicle to R.string.activity_notification_vehicle_content
            else -> -1 to -1
        }
    override val alarm: Alarms = Alarms.PENDING_ACTIVITY_ALARM
    override val titleRes: Int = notificationContent.first
    override val contentsRes: Int = notificationContent.second

    override fun onFinish(result: Result<Unit>) {
        with(AlarmHandler(context)) {
            if (ActivityReceiver.pendingActivities.size == 0) cancelAlarm(alarm)
            else scheduleAlarm(alarm)
            ActivityReceiver.alarmScheduled =
                ActivityReceiver.pendingActivities.size != 0
        }
        if (result.isFailure) {
            super.onFinish(result)
        }
    }
}
