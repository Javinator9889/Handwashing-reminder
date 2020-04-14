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
 * Created by Javinator9889 on 13/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.emoji

import android.content.Context
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.javinator9889.handwashingreminder.R
import timber.log.Timber

object EmojiCompat {
    fun get(context: Context, async: Boolean = false): EmojiCompat {
        return try {
            val emojiCompat = EmojiCompat.get()
            var instance: EmojiCompat? = null
            emojiCompat.registerInitCallback(
                object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        super.onInitialized()
                        instance = EmojiCompat.get()
                    }
                })
            while (instance == null && !async) {
                Thread.sleep(100L)
            }
            if (async)
                emojiCompat
            else
                instance!!
        } catch (_: IllegalStateException) {
            Timber.d("Class not initialized yet")
            FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs
            ).let {
                var instance: EmojiCompat? = null
                val emojiCompat =
                    with(FontRequestEmojiCompatConfig(context, it)) {
                        setReplaceAll(true)
                        EmojiCompat.init(this)
                    }
                emojiCompat.registerInitCallback(
                    object : EmojiCompat.InitCallback() {
                        override fun onInitialized() {
                            super.onInitialized()
                            instance = EmojiCompat.get()
                        }
                    })
                while (instance == null && !async) {
                    Thread.sleep(100L)
                }
                if (async)
                    emojiCompat
                else
                    instance!!
            }
        }
    }
}