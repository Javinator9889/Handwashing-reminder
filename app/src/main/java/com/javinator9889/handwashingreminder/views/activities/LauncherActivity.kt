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
package com.javinator9889.handwashingreminder.views.activities

import android.content.Intent
import android.os.Bundle
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.javinator9889.handwashingreminder.BuildConfig
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.utils.Preferences.Companion.APP_INIT_KEY
import com.javinator9889.handwashingreminder.utils.base.SplitCompatBaseActivity

class LauncherActivity : SplitCompatBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = HandwashingApplication.getInstance()
        val isIntroEnabled =
            splitInstallManager.installedModules.contains("appintro")
        if (!isIntroEnabled) {
            val installRequest = SplitInstallRequest.newBuilder()
                .addModule("appintro")
                .build()
            val installTask = app.manager.startInstall(installRequest)
            installTask.addOnCompleteListener { launchIntroOrActivity(app) }
        } else {
            launchIntroOrActivity(app)
        }
    }

    private fun launchIntroOrActivity(app: HandwashingApplication) {
        val sharedPreferences = app.getSharedPreferences()
        val isAppInitialized = sharedPreferences.getBoolean(APP_INIT_KEY, false)
        val launcherIntent = if (!isAppInitialized) {
            Intent().setClassName(
                BuildConfig.APPLICATION_ID,
                "com.javinator9889.handwashingreminder.appintro.IntroActivity"
            )
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(launcherIntent)
        finish()
    }
}