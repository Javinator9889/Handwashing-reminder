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
import android.content.Context
import android.content.Intent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
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
import com.javinator9889.handwashingreminder.BuildConfig
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentActivity
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.data.UserProperties
import com.javinator9889.handwashingreminder.databinding.SplashScreenBinding
import com.javinator9889.handwashingreminder.gms.activity.ActivityHandler
import com.javinator9889.handwashingreminder.gms.ads.AdLoader
import com.javinator9889.handwashingreminder.gms.ads.AdsEnabler
import com.javinator9889.handwashingreminder.jobs.alarms.AlarmHandler
import com.javinator9889.handwashingreminder.jobs.alarms.Alarms
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import com.javinator9889.handwashingreminder.utils.*
import com.javinator9889.handwashingreminder.utils.Preferences.ADS_ENABLED
import com.javinator9889.handwashingreminder.utils.Preferences.APP_INIT_KEY
import com.javinator9889.handwashingreminder.utils.RemoteConfig.SPECIAL_EVENT
import com.javinator9889.handwashingreminder.utils.threading.await
import com.mikepenz.iconics.Iconics
import javinator9889.localemanager.utils.languagesupport.LanguagesSupport.Language
import kotlinx.coroutines.*
import timber.log.Timber
import com.javinator9889.handwashingreminder.utils.Firebase as FirebaseConf

internal const val FAST_START_KEY = "intent:fast_start"
internal const val PENDING_INTENT_CODE = 201

class LauncherActivity : BaseFragmentActivity<SplashScreenBinding>() {
    private val deferreds = mutableSetOf<Deferred<Any?>>()
    private var launchOnInstall = false
    private var launchFromNotification = false
    private val app = HandwashingApplication.instance
    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app)
    private val dynamicFeatureDeferred = CompletableDeferred<Boolean>()
    private val activityIntentDeferred = CompletableDeferred<Intent>()
    private val splitInstallManager = SplitInstallManagerFactory.create(app)
    private lateinit var binding: SplashScreenBinding
    @get:LayoutRes
    override val layoutId: Int = R.layout.splash_screen

    init {
        lifecycleScope.launch {
            whenCreated {
                launchFromNotification =
                    intent.getBooleanExtra(FAST_START_KEY, false)
                deferreds.add(async { initVariables() })
            }
            whenStarted {
                binding.progressBar.show()
                val welcomeScreenJob = showWelcomeScreenAsync()
                deferreds.add(installRequiredModulesAsync())
                activityIntentDeferred.await().run {
                    Timber.d("Activity Init is now completed! - $this")
                    if (launchFromNotification)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                    Timber.d("Joining welcome screen...")
                    welcomeScreenJob.join()
                    Timber.d("Start")
                    startActivity(this)
                    overridePendingTransition(0, android.R.anim.fade_out)
                    finish()
                }
            }
        }
    }

    override fun inflateLayout(): SplashScreenBinding =
        SplashScreenBinding.inflate(layoutInflater).also { binding = it }

    private fun showWelcomeScreenAsync() =
        lifecycleScope.launch(Dispatchers.Main) {
            Timber.d("Awaiting Firebase initialization...")
            app.firebaseInitDeferred.await()
            val isThereAnySpecialEvent = with(Firebase.remoteConfig) {
                getBoolean(SPECIAL_EVENT) && !launchFromNotification
            }
            Timber.d("Is any special event? $isThereAnySpecialEvent")
            var sleepDuration = 0L
            val animationLoaded = CompletableDeferred<Boolean>()
            val fadeInAnimation = AnimationUtils.loadAnimation(
                this@LauncherActivity,
                android.R.anim.fade_in
            )
            fadeInAnimation.duration = 300L
            fadeInAnimation.setAnimationListener(object :
                Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    Timber.d("Animation started!")
                }
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    Timber.d("Animation is completed")
                    animationLoaded.complete(true)
                    binding.logo.playAnimation()
                }
            })
            if (isThereAnySpecialEvent && !isDebuggable()) {
                Timber.d("Starting custom animation...")
                binding.logo.setAnimation(AnimatedResources.STAY_SAFE_STAY_HOME.res)
                binding.logo.addLottieOnCompositionLoadedListener {
                    Timber.d("Animation loaded!")
                    binding.logo.startAnimation(fadeInAnimation)
                    sleepDuration = binding.logo.duration
                }
                animationLoaded.await()
                delay(sleepDuration)
            } else {
                binding.logo.setImageResource(R.drawable.handwashing_app_logo)
                binding.logo.startAnimation(fadeInAnimation)
            }
        }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != DYNAMIC_FEATURE_INSTALL_RESULT_CODE) {
            Timber.i("Unknown request code $requestCode")
            return
        }
        if (Ads.MODULE_NAME in splitInstallManager.installedModules &&
            sharedPreferences.getBoolean(ADS_ENABLED, true)
        ) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    createPackageContext(packageName, 0).also {
                        SplitCompat.install(it)
                    }
                    initAds()
                }
                Activity.RESULT_CANCELED -> app.adLoader = null
            }
        }
        if (sharedPreferences.getBoolean(APP_INIT_KEY, false) &&
            AppIntro.MODULE_NAME in splitInstallManager.installedModules
        ) {
            data?.let {
                val launchIntent = Intent(data)
                createPackageContext(packageName, 0).also {
                    SplitCompat.install(it)
                }
                Timber.d("Created launch intent $launchIntent")
                activityIntentDeferred.complete(launchIntent)
            } ?: Timber.e("Data was empty!")
        } else {
            Timber.d("Created launch intent at MainActivity")
            activityIntentDeferred.complete(
                Intent(this, MainActivity::class.java)
            )
        }
        dynamicFeatureDeferred.complete(true)
    }

    override fun finish() {
        Timber.d("Calling finish")
        binding.progressBar.hide()
        runBlocking(Dispatchers.Default) { deferreds.awaitAll() }
        super.finish()
    }

    private fun installRequiredModulesAsync() = lifecycleScope.async {
        val (modules, installedModules) = loadRequiredModules()
        Timber.d("Required to install modules: $modules")
        if (modules.isEmpty()) {
            val intent = if (AppIntro.MODULE_NAME in installedModules &&
                !sharedPreferences.getBoolean(APP_INIT_KEY, false)
            ) {
                with(Intent()) {
                    setClassName(
                        BuildConfig.APPLICATION_ID,
                        "${AppIntro.PACKAGE_NAME}.${AppIntro.MAIN_ACTIVITY_NAME}"
                    )
                    this
                }
            } else Intent(this@LauncherActivity, MainActivity::class.java)
            Timber.d("Created launch intent $intent")
            activityIntentDeferred.complete(intent)
            return@async
        }
        val intent = if (AppIntro.MODULE_NAME in modules) {
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
        dynamicFeatureDeferred.await()
    }

    private fun loadRequiredModules(): Pair<Set<String>, Set<String>> {
        val modules = mutableSetOf<String>()
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
        return (modules - splitInstallManager.installedModules) to
                splitInstallManager.installedModules
    }

    private fun initAds(context: Context = app) {
        if (Ads.MODULE_NAME in splitInstallManager.installedModules &&
            sharedPreferences.getBoolean(ADS_ENABLED, true)
        ) {
            val className = "${Ads.PACKAGE_NAME}.${Ads
                .CLASS_NAME}\$${Ads.PROVIDER_NAME}"
            val adProvider = Class.forName(className).kotlin
                .objectInstance as AdLoader.Provider
            app.adLoader = adProvider.instance(context)
            val adsEnabler = AdsEnabler(app)
            adsEnabler.enableAds()
        }
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
        Timber.d("Setting-up Firebase custom properties")
        val propertiesJob = setupFirebasePropertiesAsync()
        Timber.d("Initializing Iconics")
        Iconics.init(this)
        Timber.d("Setting-up activity recognition")
        val activityHandler = ActivityHandler.getInstance(this)
        if (sharedPreferences.getBoolean(
                Preferences.ACTIVITY_TRACKING_ENABLED, false
            )
        ) {
            Timber.d("Tracking is enabled and Play Services are available so starting tracking")
            activityHandler.startTrackingActivity()
        } else {
            Timber.d("Tracking is not enabled or Play Services are not available so starting tracking")
            activityHandler.disableActivityTracker()
        }
        with(AlarmHandler(this)) {
            scheduleAllAlarms()
        }
        Timber.d("Initializing Ads Provider")
        initAds()
        Timber.d("Adding periodic notifications if not enqueued yet")
        Timber.d("Creating alarms notification channels...")
        for (alarm in Alarms.values()) {
            Timber.d("Creating notification channel for ${alarm.groupId}")
            NotificationsHandler(
                context = this,
                channelId = alarm.channelId,
                channelName = getString(alarm.channelName),
                channelDesc = getString(alarm.channelDesc),
                groupId = alarm.groupId,
                groupName = getString(alarm.groupName)
            )
        }
        propertiesJob.join()
    }

    private fun setupFirebasePropertiesAsync() = lifecycleScope.launch {
        val firebaseAnalytics =
            FirebaseAnalytics.getInstance(this@LauncherActivity)
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
                when (UserProperties.language) {
                    Language.SPANISH -> {
                        firebaseAnalytics.setUserProperty(
                            FirebaseConf.Properties.LANGUAGE,
                            Language.SPANISH
                        )
                        R.xml.remote_config_defaults_es
                    }
                    else -> {
                        firebaseAnalytics.setUserProperty(
                            FirebaseConf.Properties.LANGUAGE,
                            Language.ENGLISH
                        )
                        R.xml.remote_config_defaults
                    }
                }
            )
            fetchAndActivate().await()
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