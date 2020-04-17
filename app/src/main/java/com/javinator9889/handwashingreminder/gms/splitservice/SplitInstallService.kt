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
 * Created by Javinator9889 on 17/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.gms.splitservice

import android.content.Context
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory

class SplitInstallService(context: Context) {
    private val splitInstallManager = SplitInstallManagerFactory.create(context)

    companion object {
        private var instance: SplitInstallService? = null
        fun getInstance(context: Context): SplitInstallService {
            instance = instance ?: SplitInstallService(context)
            return instance!!
        }
    }

    fun deferredInstall(module: String) {
        deferredInstall(listOf(module))
    }

    fun deferredInstall(modules: List<String>) {
        splitInstallManager.deferredInstall(modules)
    }

    fun deferredUninstall(module: String) {
        deferredUninstall(listOf(module))
    }

    fun deferredUninstall(modules: List<String>) {
        splitInstallManager.deferredUninstall(modules)
    }
}