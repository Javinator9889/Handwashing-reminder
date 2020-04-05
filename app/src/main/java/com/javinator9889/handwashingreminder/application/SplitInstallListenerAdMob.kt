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
 * Created by Javinator9889 on 5/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.application

import com.javinator9889.handwashingreminder.gms.ads.AdLoader
import com.javinator9889.handwashingreminder.listeners.OnSplitInstallListener
import com.javinator9889.handwashingreminder.utils.Ads

class SplitInstallListenerAdMob : OnSplitInstallListener {
    override fun onInstall(module: String) {
        when (module) {
            Ads.MODULE_NAME -> {
                val className = "${Ads.PACKAGE_NAME}.${Ads.CLASS_NAME}"
                val adProvider = Class.forName(className).kotlin.objectInstance
                        as AdLoader.Provider
                val app = HandwashingApplication.getInstance()
                app.adLoader = adProvider.instance(app)
            }
        }
    }

    override fun onFailure(module: String) {
        when (module) {
            Ads.MODULE_NAME -> {
                val app = HandwashingApplication.getInstance()
                app.adLoader = null
            }
        }
    }
}