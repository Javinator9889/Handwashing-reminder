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
package com.javinator9889.handwashingreminder.application

import android.content.Context
import android.content.SharedPreferences
import androidx.multidex.MultiDex
import androidx.preference.PreferenceManager
import androidx.work.*
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.javinator9889.handwashingreminder.gms.ads.AdLoader
import com.javinator9889.handwashingreminder.jobs.workers.AlarmCheckerWorker
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.LogReportTree
import com.javinator9889.handwashingreminder.utils.isAtLeast
import com.javinator9889.handwashingreminder.utils.isDebuggable
import javinator9889.localemanager.application.BaseApplication
import javinator9889.localemanager.utils.languagesupport.LanguagesSupport.Language
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.TimeUnit


class HandwashingApplication : BaseApplication() {
    private val scope = CoroutineScope(Dispatchers.Default)
    var adLoader: AdLoader? = null
        set(value) = synchronized(this) {
            field = value
        }
        get() = synchronized(this) {
            field
        }
    lateinit var firebaseInitDeferred: Deferred<Unit>

    companion object {
        lateinit var instance: HandwashingApplication
        val scope: CoroutineScope
            get() = instance.scope
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
        SplitCompat.install(base)
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        super.onCreate()
        instance = this
        firebaseInitDeferred = initFirebaseAppAsync()
    }

    internal fun scheduleWorkChecker() {
        val constraints = Constraints.Builder().run {
            setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            setRequiresBatteryNotLow(false)
            setRequiresCharging(false)
            if (isAtLeast(AndroidVersion.O))
                setRequiresDeviceIdle(false)
            setRequiresStorageNotLow(false)
            build()
        }
        val work =
            PeriodicWorkRequestBuilder<AlarmCheckerWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            ).run {
                setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                setConstraints(constraints)
                addTag("Alarm Checker")
                build()
            }
        with(WorkManager.getInstance(this)) {
            enqueueUniquePeriodicWork(
                "Alarm checker job",
                ExistingPeriodicWorkPolicy.KEEP,
                work
            )
        }
    }

    private fun initFirebaseAppAsync(): Deferred<Unit> {
        return scope.async {
            withContext(Dispatchers.IO) {
                FirebaseApp.initializeApp(this@HandwashingApplication)
                if (isDebuggable()) {
                    Timber.plant(Timber.DebugTree())
                    Timber.d("Application is in DEBUG mode")
                    with(FirebaseCrashlytics.getInstance()) {
                        setCrashlyticsCollectionEnabled(false)
                    }
                } else {
                    Timber.plant(LogReportTree())
                }
            }
        }
    }

    /**
     * Updates the application language
     * @param language a valid language code.
     * @see Language
     */
    fun setNewLocale(@Language language: String) {
        localeManager.setNewLocale(this, language)
    }

    /**
     * {@inheritDoc}
     */
    override fun getCustomSharedPreferences(base: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(base)
}
