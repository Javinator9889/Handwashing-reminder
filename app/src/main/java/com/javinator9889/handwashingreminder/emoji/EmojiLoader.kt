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
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import timber.log.Timber

object EmojiLoader {
    fun loadAsync(context: Context) = HandwashingApplication.scope.async {
        try {
            EmojiCompat.get()
        } catch (_: IllegalStateException) {
            Timber.d("EmojiCompat not initialized yet")
            val emojiCompat = with(EmojiConfig.get(context)) {
                EmojiCompat.init(this)
            }
            val loadDeferred = CompletableDeferred<EmojiCompat>()
            emojiCompat.registerInitCallback(object :
                EmojiCompat.InitCallback() {
                override fun onInitialized() {
                    super.onInitialized()
                    emojiCompat.unregisterInitCallback(this)
                    loadDeferred.complete(EmojiCompat.get())
                }

                override fun onFailed(throwable: Throwable?) {
                    super.onFailed(throwable)
                    emojiCompat.unregisterInitCallback(this)
                    val exception = throwable
                        ?: RuntimeException("EmojiCompat failed to load")
                    loadDeferred.completeExceptionally(exception)
                }
            })
            loadDeferred.await()
        }
    }
}