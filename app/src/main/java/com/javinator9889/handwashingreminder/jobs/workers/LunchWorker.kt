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
 * Created by Javinator9889 on 22/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.jobs.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.Workers

class LunchWorker(context: Context, params: WorkerParameters) :
    AbstractNotificationsWorker(context, params) {
    override val clazz: Class<out ListenableWorker> = LunchWorker::class.java
    override val workUniqueName: String = Workers.LUNCH_UUID
    override val preferencesKey: String = Preferences.LUNCH_TIME
    override val titleRes: Int = R.string.lunch_title
    override val commentsRes: Int = R.array.lunch_comments
}