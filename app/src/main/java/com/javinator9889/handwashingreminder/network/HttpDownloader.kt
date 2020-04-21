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
 * Created by Javinator9889 on 21/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.network

import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.OkHttp
import com.javinator9889.handwashingreminder.utils.OkHttpLegacy
import com.javinator9889.handwashingreminder.utils.isAtLeast

object HttpDownloader {
    fun newInstance(): OkHttpDownloader {
        val className = if (isAtLeast(AndroidVersion.LOLLIPOP))
            "${OkHttp.PACKAGE_NAME}.${OkHttp.CLASS_NAME}\$${OkHttp.PROVIDER_NAME}"
        else
            "${OkHttpLegacy.PACKAGE_NAME}.${OkHttpLegacy
                .CLASS_NAME}\$${OkHttpLegacy.PROVIDER_NAME}"
        val okHttpProvider = Class.forName(className).kotlin.objectInstance
                as OkHttpDownloader.Provider
        return okHttpProvider.newInstance()
    }
}