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
import androidx.annotation.ArrayRes
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import androidx.work.*
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import com.javinator9889.handwashingreminder.utils.TIME_CHANNEL_ID
import com.javinator9889.handwashingreminder.utils.runAt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

data class WorkScheduleParams(
    @IntRange(from = 0, to = 23) val hour: Int,
    @IntRange(from = 0, to = 59) val minute: Int
)

abstract class AbstractNotificationsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    protected var maxRetries = 5
    protected var shouldScheduleNext = true
    protected var workConstraints = with(Constraints.Builder()) {
        setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        setRequiresBatteryNotLow(false)
        setRequiresCharging(false)
        setRequiresDeviceIdle(false)
        setRequiresStorageNotLow(false)
        build()
    }
    protected abstract val clazz: Class<out ListenableWorker>
    protected abstract val workUniqueName: String
    protected abstract val preferencesKey: String
    protected abstract val titleRes: Int
    protected abstract val commentsRes: Int
    protected val workParams: WorkScheduleParams
        get() {
            val preferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val time = preferences.getString(preferencesKey, "")
            if (time == "" || time == null)
                throw IllegalStateException("Time value cannot be null")
            val splitTime = time.split(":")
            val hour = Integer.parseInt(splitTime[0])
            val minute = Integer.parseInt(splitTime[1])
            return WorkScheduleParams(hour, minute)
        }

    override suspend fun doWork(): Result = coroutineScope {
        with(HandwashingApplication.getInstance()) {
            firebaseInitDeferred.await()
        }
        shouldScheduleNext = true
        var data: Data? = null
        try {
            data = work()
            Result.success()
        } catch (e: Exception) {
            catchBlock(e)
        } finally {
            if (shouldScheduleNext) {
                scheduleNext(data)
            }
        }
    }

    protected open suspend fun work(): Data? {
        val emojiLoader = EmojiLoader.get(applicationContext)
        val notificationsHandler = NotificationsHandler(
            context = applicationContext,
            channelId = TIME_CHANNEL_ID,
            channelName = getString(R.string.time_notification_channel_name),
            channelDesc = getString(R.string.time_notification_channel_desc)
        )
        val emojiCompat = emojiLoader.await()
        var title: CharSequence
        var content: CharSequence
        try {
            title = emojiCompat.process(getText(titleRes))
            content = emojiCompat.process(
                getStringArray(commentsRes).toList().random()
            )
        } catch (_: IllegalStateException) {
            title = getText(titleRes)
            content = getStringArray(commentsRes).toList().random()
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
        return null
    }

    protected fun catchBlock(e: Exception): Result {
        if (runAttemptCount >= maxRetries) {
            Timber.d("Exceeded max attempts: $maxRetries")
            return Result.failure()
        }
        return when (e.cause) {
            is IllegalStateException -> {
                Timber.w(e, "IllegalStateException on worker class")
                shouldScheduleNext = false
                Result.retry()
            }
            else -> {
                Timber.e(e, "Uncaught exception on worker class")
                Result.failure()
            }
        }
    }

    protected fun scheduleNext(data: Data?) {
        val workManager = WorkManager.getInstance(applicationContext)
        val nextExecutionDelay = runAt(workParams.hour, workParams.minute)
        Timber.d("Executing $workUniqueName in ${nextExecutionDelay / 1000} s")
        val jobRequest = with(OneTimeWorkRequest.Builder(clazz)) {
            data?.let { setInputData(it) }
            setInitialDelay(nextExecutionDelay, TimeUnit.MILLISECONDS)
            setConstraints(workConstraints)
            build()
        }
        workManager.enqueueUniqueWork(
            workUniqueName, ExistingWorkPolicy.REPLACE, jobRequest
        )
    }

    private fun getString(@StringRes resId: Int): String =
        applicationContext.getString(resId)

    private fun getText(@StringRes resId: Int): CharSequence =
        applicationContext.getText(resId)

    private fun getStringArray(@ArrayRes resId: Int): Array<out String> =
        applicationContext.resources.getStringArray(resId)
}