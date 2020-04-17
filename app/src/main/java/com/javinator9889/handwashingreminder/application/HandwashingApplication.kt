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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.javinator9889.handwashingreminder.gms.activity.ActivityHandler
import com.javinator9889.handwashingreminder.gms.ads.AdLoader
import com.javinator9889.handwashingreminder.gms.vendor.BillingService
import com.javinator9889.handwashingreminder.jobs.workers.WorkHandler
import com.javinator9889.handwashingreminder.utils.LogReportTree
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.isDebuggable
import javinator9889.localemanager.application.BaseApplication
import javinator9889.localemanager.utils.languagesupport.LanguagesSupport.Language
import timber.log.Timber


class HandwashingApplication : BaseApplication() {
    var adLoader: AdLoader? = null
    lateinit var workHandler: WorkHandler
    lateinit var billingService: BillingService
    lateinit var activityHandler: ActivityHandler
    lateinit var remoteConfig: FirebaseRemoteConfig
    lateinit var sharedPreferences: SharedPreferences
    //TODO lateinit var firebaseAnalytics
    //TODO lateinit var firebasePerformance

    companion object {
        private lateinit var instance: HandwashingApplication

        fun getInstance(): HandwashingApplication {
            return this.instance
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
        SplitCompat.install(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedPreferences = getCustomSharedPreferences(this)
        if (isDebuggable()) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Application is in DEBUG mode")
        }
        else
            Timber.plant(LogReportTree())
        activityHandler = ActivityHandler(this)
        if (sharedPreferences.getBoolean(
                Preferences.ACTIVITY_TRACKING_ENABLED, false
            ) && with(GoogleApiAvailability.getInstance()) {
                isGooglePlayServicesAvailable(this@HandwashingApplication) ==
                        ConnectionResult.SUCCESS
            }
        )
            activityHandler.startTrackingActivity()
        else
            activityHandler.disableActivityTracker()

        remoteConfig = FirebaseRemoteConfig.getInstance()
        workHandler = WorkHandler(this)
        try {
            workHandler.enqueuePeriodicNotificationsWorker()
        } catch (_: UninitializedPropertyAccessException) {
            Timber.i("Scheduler times have not been initialized")
        }
        billingService = BillingService(this)
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
