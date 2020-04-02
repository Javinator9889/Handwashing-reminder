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

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.utils.Modules.AppIntro.MAIN_ACTIVITY_NAME
import com.javinator9889.handwashingreminder.utils.Modules.AppIntro.MODULE_NAME
import com.javinator9889.handwashingreminder.utils.Modules.AppIntro.PACKAGE_NAME
import com.javinator9889.handwashingreminder.utils.Preferences.Companion.APP_INIT_KEY

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences =
            HandwashingApplication.getInstance().sharedPreferences
        val launchIntent =
            if (sharedPreferences.getBoolean(APP_INIT_KEY, true)) {
                Intent(this, DynamicFeatureProgress::class.java).also {
                    it.putExtra(DynamicFeatureProgress.MODULES, MODULE_NAME)
                    it.putExtra(DynamicFeatureProgress.LAUNCH_ON_INSTALL, true)
                    it.putExtra(
                        DynamicFeatureProgress.CLASS_NAME,
                        MAIN_ACTIVITY_NAME
                    )
                    it.putExtra(
                        DynamicFeatureProgress.PACKAGE_NAME,
                        PACKAGE_NAME
                    )
                }
            } else {
                Intent(this, MainActivity::class.java)
            }
        startActivity(launchIntent)
        finish()
    }
}