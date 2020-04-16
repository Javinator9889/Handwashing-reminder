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
package com.javinator9889.handwashingreminder.activities.views.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceDialogFragmentCompat
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.formatTime
import com.javinator9889.handwashingreminder.utils.isAtLeast
import kotlinx.android.synthetic.main.pref_dialog_time.*
import kotlinx.android.synthetic.main.pref_dialog_time.view.*

class TimePreferenceFragment : PreferenceDialogFragmentCompat() {
    companion object {
        fun newInstance(key: String) =
            with(TimePreferenceFragment()) {
                val args = Bundle(1)
                args.putString(ARG_KEY, key)
                arguments = args
                this
            }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        val time = (preference as TimePreference).time
        val splittedTime = time.split(":")
        val hours = Integer.valueOf(splittedTime[0])
        val minutes = Integer.valueOf(splittedTime[1])

        view.edit.apply {
            if (isAtLeast(AndroidVersion.M)) {
                hour = hours
                minute = minutes
            } else {
                currentHour = hours
                currentMinute = minutes
            }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val time = if (isAtLeast(AndroidVersion.M))
                TimePickerContent(edit.hour, edit.minute)
            else
                TimePickerContent(edit.currentHour, edit.currentMinute)
            with(preference as TimePreference) {
                this.time =
                    "${formatTime(time.hour)}:${formatTime(time.minute)}"
            }
        }
    }

    private data class TimePickerContent(val hour: Int, val minute: Int)
}