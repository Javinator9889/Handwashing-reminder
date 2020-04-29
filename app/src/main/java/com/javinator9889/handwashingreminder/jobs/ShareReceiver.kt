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
 * Created by Javinator9889 on 27/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.jobs

import android.content.*
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.utils.getUriFromRes
import timber.log.Timber

class ShareReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("Receiver broadcast")
        with(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                context.getText(R.string.share_text)
            )
            putExtra(
                Intent.EXTRA_TITLE,
                context.getText(R.string.share_title)
            )
            ClipData.Item(
                getUriFromRes(
                    context,
                    R.drawable.handwashing_app_logo
                )
            )
            clipData = ClipData(
                ClipDescription(
                    context.getString(R.string.share_label),
                    arrayOf("image/*")
                ),
                ClipData.Item(
                    getUriFromRes(
                        context,
                        R.drawable.handwashing_app_logo
                    )
                )
            )
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            type = "text/plain"
        }, null)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(this)
        }
    }
}