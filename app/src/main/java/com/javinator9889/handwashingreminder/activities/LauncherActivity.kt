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
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.gms.ads.AdLoader
import com.javinator9889.handwashingreminder.gms.ads.AdsEnabler
import com.javinator9889.handwashingreminder.utils.*
import com.javinator9889.handwashingreminder.utils.Preferences.Companion.ADS_ENABLED
import com.javinator9889.handwashingreminder.utils.Preferences.Companion.APP_INIT_KEY
import com.javinator9889.handwashingreminder.utils.RemoteConfig.Keys.SPECIAL_EVENT
import com.mikepenz.iconics.Iconics
import kotlinx.android.synthetic.main.splash_screen.*
import kotlin.concurrent.thread

internal const val FAST_START_KEY = "intent:fast_start"
internal const val PENDING_INTENT_CODE = 201

class LauncherActivity : AppCompatActivity() {
    private var launchOnInstall = false
    private var launchFromNotification = false
    private lateinit var app: HandwashingApplication
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = HandwashingApplication.getInstance()
        sharedPreferences = app.sharedPreferences
        with(intent) {
            notNull {
                launchFromNotification =
                    it.getBooleanExtra(FAST_START_KEY, false)
            }
        }
        val displayThread = displayWelcomeScreen()
        initVariables()
        installRequiredModules(displayThread)
    }

    private fun displayWelcomeScreen(): Thread {
        setContentView(R.layout.splash_screen)
        return thread(start = true) {
            val isThereAnySpecialEvent =
                app.remoteConfig.getBoolean(SPECIAL_EVENT) &&
                        !launchFromNotification
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
                runOnUiThread {
                    logo.setAnimation(AnimatedResources.STAY_SAFE_STAY_HOME.res)
                }
                logo.enableMergePathsForKitKatAndAbove(true)
                logo.addLottieOnCompositionLoadedListener {
                    runOnUiThread {
                        logo.startAnimation(fadeInAnimation)
                    }
                    sleepDuration = logo.duration
                }
                while (!animationLoaded)
                    Thread.sleep(10)
                Thread.sleep(sleepDuration)
            } else {
                runOnUiThread {
                    logo.setImageResource(R.drawable.handwashing_app_logo)
                    logo.startAnimation(fadeInAnimation)
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DYNAMIC_FEATURE_INSTALL_RESULT_CODE) {
            if (sharedPreferences.getBoolean(ADS_ENABLED, true)) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val className = "${Ads.PACKAGE_NAME}.${Ads
                            .CLASS_NAME}\$${Ads.PROVIDER_NAME}"
                        val adProvider = Class.forName(className).kotlin
                            .objectInstance as AdLoader.Provider
                        app.adLoader = adProvider.instance(app)
                        val adsEnabler = AdsEnabler(app)
                        adsEnabler.enableAds()
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
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, android.R.anim.fade_out)
    }

    private fun installRequiredModules(waitingThread: Thread) {
        thread(start = true) {
            if (waitingThread.isAlive)
                waitingThread.join()
            val modules = ArrayList<String>(MODULE_COUNT)
            if (sharedPreferences.getBoolean(ADS_ENABLED, true))
                modules.add(Ads.MODULE_NAME)
            if (!sharedPreferences.getBoolean(APP_INIT_KEY, false)) {
                modules.add(AppIntro.MODULE_NAME)
                launchOnInstall = true
            }
            modules.removeAll { module -> module == "" }
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

    private fun initVariables() {
        val config = with(FirebaseRemoteConfigSettings.Builder()) {
            minimumFetchIntervalInSeconds = 3600
            fetchTimeoutInSeconds = 3
            build()
        }
        with(FirebaseRemoteConfig.getInstance()) {
            setConfigSettingsAsync(config)
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate()
        }
        Iconics.init(this)
        EmojiLoader.get(this)
    }
}