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
 * Created by Javinator9889 on 16/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.emoji

import android.content.Context
import androidx.emoji.text.EmojiCompat
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber

object EmojiLoader {
    fun get(context: Context): CompletableDeferred<EmojiCompat> {
        val deferred = CompletableDeferred<EmojiCompat>()
        try {
            with(EmojiCompat.get()) {
                deferred.complete(this)
            }
        } catch (_: IllegalStateException) {
            Timber.d("EmojiCompat not initialized yet")
            val emojiCompat = with(EmojiConfig.get(context)) {
                EmojiCompat.init(this)
            }
            emojiCompat.registerInitCallback(
                object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        emojiCompat.unregisterInitCallback(this)
                        deferred.complete(EmojiCompat.get())
                    }

                    override fun onFailed(throwable: Throwable?) {
                        emojiCompat.unregisterInitCallback(this)
                        val exception = throwable
                            ?: RuntimeException("EmojiCompat failed to load")
                        deferred.completeExceptionally(exception)
                    }
                })
        } finally {
            return deferred
        }
    }
}