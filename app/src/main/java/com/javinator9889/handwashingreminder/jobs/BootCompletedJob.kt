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
package com.javinator9889.handwashingreminder.jobs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.jobs.workers.WorkHandler
import com.javinator9889.handwashingreminder.utils.Preferences
import timber.log.Timber

class BootCompletedJob : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = HandwashingApplication.getInstance()
            val preferences = app.sharedPreferences
            if (preferences.getBoolean(
                    Preferences.ACTIVITY_TRACKING_ENABLED,
                    false
                )
            )
                app.activityHandler.startTrackingActivity()
            else
                app.activityHandler.disableActivityTracker()
            try {
                with(WorkHandler(requireNotNull(context))) {
                    enqueuePeriodicNotificationsWorker()
                }
            } catch (_: IllegalArgumentException) {
                Timber.w(
                    "Context is null so notifications cannot be scheduled"
                )
            }
        }
    }
}
