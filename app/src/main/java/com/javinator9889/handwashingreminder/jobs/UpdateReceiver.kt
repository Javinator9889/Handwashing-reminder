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
 * Created by Javinator9889 on 21/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.jobs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.javinator9889.handwashingreminder.jobs.alarms.AlarmHandler
import timber.log.Timber

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Timber.d("Package updated so rescheduling jobs")
            with(AlarmHandler(context)) {
                scheduleAllAlarms()
            }
        }
    }
}