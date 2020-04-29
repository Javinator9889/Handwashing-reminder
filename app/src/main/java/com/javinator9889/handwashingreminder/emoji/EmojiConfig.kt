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
 * Created by Javinator9889 on 18/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.emoji

import android.content.Context
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.utils.BundledEmoji
import com.javinator9889.handwashingreminder.utils.GOOGLE_PLAY_SERVICES_MIN_VERSION
import com.javinator9889.handwashingreminder.utils.isModuleInstalled

object EmojiConfig {
    fun get(context: Context): EmojiCompat.Config {
        return with(GoogleApiAvailability.getInstance()) {
            if (isGooglePlayServicesAvailable(
                    context,
                    GOOGLE_PLAY_SERVICES_MIN_VERSION
                ) == ConnectionResult.SUCCESS ||
                !isModuleInstalled(
                    context.applicationContext,
                    BundledEmoji.MODULE_NAME
                )
            ) {
                with(
                    FontRequest(
                        "com.google.android.gms.fonts",
                        "com.google.android.gms",
                        "Noto Color Emoji Compat",
                        R.array.com_google_android_gms_fonts_certs
                    )
                ) {
                    FontRequestEmojiCompatConfig(context, this).run {
                        setReplaceAll(true)
                    }
                }
            } else {
                val className = "${BundledEmoji.PACKAGE_NAME}.${BundledEmoji
                    .CLASS_NAME}"
                val bundledProvider = Class.forName(className).kotlin
                    .objectInstance as IBundledEmojiConfig
                bundledProvider.loadBundledEmojiConfig(context)
            }
        }
    }
}