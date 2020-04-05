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
import androidx.appcompat.app.AppCompatActivity
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.gms.ads.AdLoader
import com.javinator9889.handwashingreminder.utils.ADS_DYNAMIC_FEATURE_CODE
import com.javinator9889.handwashingreminder.utils.Ads
import com.javinator9889.handwashingreminder.utils.AppIntro
import com.javinator9889.handwashingreminder.utils.Preferences.Companion.ADS_ENABLED
import com.javinator9889.handwashingreminder.utils.Preferences.Companion.APP_INIT_KEY
import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread

class LauncherActivity : AppCompatActivity() {
    private val barrier = CyclicBarrier(2)
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var app: HandwashingApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = HandwashingApplication.getInstance()
        sharedPreferences = app.sharedPreferences
        installRequiredModules()
        thread(start = true) {
            barrier.await()
            launchActivity()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ADS_DYNAMIC_FEATURE_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val className = "${Ads.PACKAGE_NAME}.${Ads
                            .CLASS_NAME}\$${Ads.PROVIDER_NAME}"
                        val adProvider = Class.forName(className).kotlin
                            .objectInstance as AdLoader.Provider
                        app.adLoader = adProvider.instance(app)
                    }
                    Activity.RESULT_CANCELED -> app.adLoader = null
                }
            }
        }
        thread(start = true) {
            barrier.await()
        }
    }

    private fun launchActivity() {
        runOnUiThread {
            val launchIntent =
                if (sharedPreferences.getBoolean(APP_INIT_KEY, true)) {
                    createDynamicFeatureActivityIntent(
                        AppIntro.MODULE_NAME, true,
                        AppIntro.MAIN_ACTIVITY_NAME, AppIntro.PACKAGE_NAME
                    )
                } else {
                    Intent(this, MainActivity::class.java)
                }
            startActivity(launchIntent)
            /*overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )*/
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }

    private fun installRequiredModules() {
        if (sharedPreferences.getBoolean(ADS_ENABLED, true)) {
            createDynamicFeatureActivityIntent(
                Ads.MODULE_NAME,
                false
            ).also {
                startActivityForResult(it, ADS_DYNAMIC_FEATURE_CODE)
            }
        }
    }

    private fun createDynamicFeatureActivityIntent(
        moduleName: String,
        launchOnInstall: Boolean = false,
        className: String = "",
        packageName: String = ""
    ): Intent = Intent(this, DynamicFeatureProgress::class.java).also {
        it.putExtra(DynamicFeatureProgress.MODULES, moduleName)
        it.putExtra(DynamicFeatureProgress.LAUNCH_ON_INSTALL, launchOnInstall)
        it.putExtra(DynamicFeatureProgress.CLASS_NAME, className)
        it.putExtra(DynamicFeatureProgress.PACKAGE_NAME, packageName)
    }
}