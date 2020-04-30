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
 * Created by Javinator9889 on 23/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenStarted
import androidx.preference.PreferenceManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.gms.ads.AdLoader
import com.javinator9889.handwashingreminder.gms.ads.AdsEnabler
import com.javinator9889.handwashingreminder.jobs.alarms.AlarmHandler
import com.javinator9889.handwashingreminder.utils.*
import com.javinator9889.handwashingreminder.utils.Preferences.Companion.ADS_ENABLED
import com.javinator9889.handwashingreminder.utils.Preferences.Companion.APP_INIT_KEY
import com.javinator9889.handwashingreminder.utils.RemoteConfig.Keys.SPECIAL_EVENT
import com.mikepenz.iconics.Iconics
import javinator9889.localemanager.utils.languagesupport.LanguagesSupport
import kotlinx.android.synthetic.main.splash_screen.*
import kotlinx.coroutines.*
import org.conscrypt.Conscrypt
import timber.log.Timber
import java.security.Security
import java.util.*
import kotlin.collections.ArrayList
import com.javinator9889.handwashingreminder.utils.Firebase as FirebaseConf

internal const val FAST_START_KEY = "intent:fast_start"
internal const val PENDING_INTENT_CODE = 201

class LauncherActivity : AppCompatActivity() {
    private var launchOnInstall = false
    private var launchFromNotification = false
    private var canFinishActivity = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var app: HandwashingApplication
    private lateinit var initDeferred: Deferred<Unit>

    init {
        lifecycleScope.launch {
            whenCreated {
                app = HandwashingApplication.instance
                sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this@LauncherActivity)
                with(intent) {
                    notNull {
                        launchFromNotification =
                            it.getBooleanExtra(FAST_START_KEY, false)
                    }
                }
                initDeferred = async { initVariables() }
            }
            whenStarted {
                try {
                    withContext(Dispatchers.Main) { displayWelcomeScreen() }
                    withContext(Dispatchers.Main) { installRequiredModules() }
                } finally {
                    initDeferred.await()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
    }

    private suspend fun displayWelcomeScreen() {
        app.firebaseInitDeferred.await()
        val isThereAnySpecialEvent = with(Firebase.remoteConfig) {
            getBoolean(SPECIAL_EVENT) && !launchFromNotification
        }
        var sleepDuration = 0L
        var animationLoaded = false
        val fadeInAnimation =
            AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeInAnimation.duration = 300L
        fadeInAnimation.setAnimationListener(object :
            Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                logo.playAnimation()
                animationLoaded = true
            }
        })
        if (isThereAnySpecialEvent) {
            logo.setAnimation(AnimatedResources.STAY_SAFE_STAY_HOME.res)
            logo.enableMergePathsForKitKatAndAbove(true)
            logo.addLottieOnCompositionLoadedListener {
                logo.startAnimation(fadeInAnimation)
                sleepDuration = logo.duration
            }
            while (!animationLoaded)
                delay(10L)
            delay(sleepDuration)
        } else {
            logo.setImageResource(R.drawable.handwashing_app_logo)
            logo.startAnimation(fadeInAnimation)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DYNAMIC_FEATURE_INSTALL_RESULT_CODE) {
            EmojiLoader.get(this)
            if (sharedPreferences.getBoolean(ADS_ENABLED, true)) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        initAds()
                        data.notNull {
                            createPackageContext(packageName, 0).also {
                                SplitCompat.install(it)
                            }
                            if (launchFromNotification)
                                data!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(data)
                            finish()
                        }
                    }
                    Activity.RESULT_CANCELED -> app.adLoader = null
                }
            }
            if (!launchOnInstall) {
                Intent(this, MainActivity::class.java).also {
                    if (launchFromNotification)
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                    overridePendingTransition(0, android.R.anim.fade_out)
                }
            }
            if (canFinishActivity)
                finish()
            else
                canFinishActivity = true
        }
    }

    override fun finish() {
        Timber.d("Calling finish")
        super.finish()
    }

    private fun installRequiredModules() {
        val modules = ArrayList<String>(MODULE_COUNT)
        val googleApi = GoogleApiAvailability.getInstance()
        if (sharedPreferences.getBoolean(ADS_ENABLED, true))
            modules += Ads.MODULE_NAME
        if (!sharedPreferences.getBoolean(APP_INIT_KEY, false)) {
            modules += AppIntro.MODULE_NAME
            launchOnInstall = true
        }
        if (googleApi.isGooglePlayServicesAvailable(
                this,
                GOOGLE_PLAY_SERVICES_MIN_VERSION
            ) != ConnectionResult.SUCCESS
        )
            modules += BundledEmoji.MODULE_NAME
        else
            with(SplitInstallManagerFactory.create(this)) {
                deferredUninstall(listOf(BundledEmoji.MODULE_NAME))
            }
        modules.trimToSize()
        val intent = if (launchOnInstall) {
            createDynamicFeatureActivityIntent(
                modules.toTypedArray(),
                launchOnInstall,
                AppIntro.MAIN_ACTIVITY_NAME,
                AppIntro.PACKAGE_NAME
            )
        } else {
            createDynamicFeatureActivityIntent(modules.toTypedArray())
        }
        startActivityForResult(intent, DYNAMIC_FEATURE_INSTALL_RESULT_CODE)
    }

    private fun initAds() {
        val className = "${Ads.PACKAGE_NAME}.${Ads
            .CLASS_NAME}\$${Ads.PROVIDER_NAME}"
        val adProvider = Class.forName(className).kotlin
            .objectInstance as AdLoader.Provider
        app.adLoader = adProvider.instance(app)
        val adsEnabler = AdsEnabler(app)
        adsEnabler.enableAds()
    }

    private fun createDynamicFeatureActivityIntent(
        modules: Array<String>,
        launchOnInstall: Boolean = false,
        className: String = "",
        packageName: String = ""
    ): Intent = Intent(this, DynamicFeatureProgress::class.java).also {
        it.putExtra(DynamicFeatureProgress.MODULES, modules)
        it.putExtra(DynamicFeatureProgress.LAUNCH_ON_INSTALL, launchOnInstall)
        it.putExtra(DynamicFeatureProgress.CLASS_NAME, className)
        it.putExtra(DynamicFeatureProgress.PACKAGE_NAME, packageName)
    }

    private suspend fun initVariables() {
        // Wait at most 3 seconds for Firebase to initialize. Then continue
        // with the app initialization
        withTimeoutOrNull(3_000L) {
            app.firebaseInitDeferred.await()
        }
        Timber.d("Firebase initialized correctly")
        Timber.d("Initializing Iconics")
        Iconics.init(this)
        Timber.d("Setting-up security providers")
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        Timber.d("Setting-up activity recognition")
        if (sharedPreferences.getBoolean(
                Preferences.ACTIVITY_TRACKING_ENABLED, false
            ) && with(GoogleApiAvailability.getInstance()) {
                isGooglePlayServicesAvailable(this@LauncherActivity) ==
                        ConnectionResult.SUCCESS
            }
        ) {
            app.activityHandler.startTrackingActivity()
        } else {
            app.activityHandler.disableActivityTracker()
        }
        with(AlarmHandler(this)) {
            scheduleAllAlarms()
        }
        Timber.d("Adding periodic notifications if not enqueued yet")
        Timber.d("Setting-up Firebase custom properties")
        setupFirebaseProperties()
    }

    private fun setupFirebaseProperties() {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val firebaseRemoteConfig = Firebase.remoteConfig
        val firebasePerformance = FirebasePerformance.getInstance()
        val config = with(FirebaseRemoteConfigSettings.Builder()) {
            minimumFetchIntervalInSeconds = 10
            fetchTimeoutInSeconds = 5
            build()
        }
        with(firebaseRemoteConfig) {
            Timber.d("Initializing Firebase Remote Config")
            setConfigSettingsAsync(config)
            setDefaultsAsync(
                when (Locale.getDefault().language) {
                    Locale(LanguagesSupport.Language.SPANISH).language -> {
                        firebaseAnalytics.setUserProperty(
                            FirebaseConf.Properties.LANGUAGE,
                            LanguagesSupport.Language.SPANISH
                        )
                        R.xml.remote_config_defaults_es
                    }
                    else -> {
                        firebaseAnalytics.setUserProperty(
                            FirebaseConf.Properties.LANGUAGE,
                            LanguagesSupport.Language.ENGLISH
                        )
                        R.xml.remote_config_defaults
                    }
                }
            )
            fetchAndActivate().addOnSuccessListener {
                if (canFinishActivity)
                    finish()
                else
                    canFinishActivity = true
            }
        }
        firebaseAnalytics.setAnalyticsCollectionEnabled(
            sharedPreferences.getBoolean(
                Preferences.ANALYTICS_ENABLED,
                true
            )
        )
        firebasePerformance.isPerformanceCollectionEnabled =
            sharedPreferences.getBoolean(
                Preferences.PERFORMANCE_ENABLED,
                true
            )
        Timber.d("Performance enabled: ${firebasePerformance.isPerformanceCollectionEnabled}")
    }
}