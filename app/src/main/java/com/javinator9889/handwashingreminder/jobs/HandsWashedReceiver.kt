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
 * Created by Javinator9889 on 2/07/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.jobs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.data.repositories.HandwashingRepository
import com.javinator9889.handwashingreminder.data.room.db.HandwashingDatabase
import com.javinator9889.handwashingreminder.data.room.entities.Handwashing
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.utils.calendar.CalendarUtils
import com.javinator9889.handwashingreminder.utils.goAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal const val HANDS_WASHED_CODE = 128
internal const val HANDS_WASHED_ACTION =
    "com.javinator9889.handwashingreminder.HANDSWASHED_EVENT"

class HandsWashedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val emojiLoader = EmojiLoader.loadAsync(context)
        val repository =
            with(HandwashingDatabase.getDatabase(context).handwashingDao()) {
                HandwashingRepository(this)
            }
        with(NotificationManagerCompat.from(context)) {
            cancel(1)
        }
        goAsync {
            val createdItem = withContext(Dispatchers.IO) {
                repository.get(CalendarUtils.today.time)
            }
            if (createdItem == null) {
                withContext(Dispatchers.IO) {
                    repository.create(Handwashing(CalendarUtils.today.time, 0))
                }
            }
            withContext(Dispatchers.IO) {
                repository.increment(CalendarUtils.today.time)
            }
            val emojiCompat = emojiLoader.await()
            val toastText = context.getText(R.string.hurray)
            withContext(Dispatchers.Main) {
                try {
                    Toast.makeText(
                        context,
                        emojiCompat.process(toastText),
                        Toast.LENGTH_LONG
                    ).show()
                } catch (_: IllegalStateException) {
                    Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}