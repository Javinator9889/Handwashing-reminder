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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.javinator9889.handwashingreminder.gms.activity.ActivityHandler
import com.javinator9889.handwashingreminder.gms.ads.AdLoader
import com.javinator9889.handwashingreminder.gms.vendor.BillingService
import com.javinator9889.handwashingreminder.jobs.workers.WorkHandler
import com.javinator9889.handwashingreminder.utils.Firebase
import com.javinator9889.handwashingreminder.utils.LogReportTree
import com.javinator9889.handwashingreminder.utils.Preferences
import com.javinator9889.handwashingreminder.utils.isDebuggable
import javinator9889.localemanager.application.BaseApplication
import javinator9889.localemanager.utils.languagesupport.LanguagesSupport.Language
import timber.log.Timber
import java.util.*


class HandwashingApplication : BaseApplication() {
    var adLoader: AdLoader? = null
    lateinit var workHandler: WorkHandler
    lateinit var billingService: BillingService
    lateinit var activityHandler: ActivityHandler
    lateinit var remoteConfig: FirebaseRemoteConfig
    lateinit var sharedPreferences: SharedPreferences
    lateinit var firebaseAnalytics: FirebaseAnalytics
    lateinit var firebasePerformance: FirebasePerformance

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
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setupFirebaseProperties()
        firebasePerformance = FirebasePerformance.getInstance()
        if (isDebuggable()) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Application is in DEBUG mode")
            with(FirebaseCrashlytics.getInstance()) {
                setCrashlyticsCollectionEnabled(false)
            }
        } else
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
        firebaseAnalytics.setAnalyticsCollectionEnabled(
            sharedPreferences.getBoolean(
                Preferences.ANALYTICS_ENABLED,
                true
            )
        )
        firebasePerformance.isPerformanceCollectionEnabled =
            sharedPreferences.getBoolean(Preferences.PERFORMANCE_ENABLED, true)

        remoteConfig = FirebaseRemoteConfig.getInstance()
        workHandler = WorkHandler(this)
        billingService = BillingService(this)
    }

    private fun setupFirebaseProperties() {
        when (Locale.getDefault().language) {
            Locale(Language.SPANISH).language ->
                firebaseAnalytics.setUserProperty(
                    Firebase.Properties.LANGUAGE, Language.SPANISH
                )
            else ->
                firebaseAnalytics.setUserProperty(
                    Firebase.Properties.LANGUAGE, Language.ENGLISH
                )
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
