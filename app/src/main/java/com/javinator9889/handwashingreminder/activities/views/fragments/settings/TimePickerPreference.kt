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

import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.widget.TimePicker
import androidx.preference.EditTextPreference
import com.javinator9889.handwashingreminder.jobs.alarms.AlarmHandler
import com.javinator9889.handwashingreminder.jobs.alarms.Alarms
import com.javinator9889.handwashingreminder.utils.formatTime

class TimePickerPreference : EditTextPreference,
    TimePickerDialog.OnTimeSetListener {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)
    lateinit var alarm: Alarms
    lateinit var summaryText: CharSequence

    private fun setSummary(hours: Int, minutes: Int): String {
        return with("${formatTime(hours)}:${formatTime(minutes)}") {
            setSummary(this)
            this
        }
    }

    private fun setSummary(time: String): String {
        summary = if (::summaryText.isInitialized) "$summaryText $time"
        else time
        return time
    }

    fun updateSummary(time: String? = null) {
        setSummary(time ?: text)
    }

    override fun onClick() {
        val time = text.split(":")
        val tpHour = Integer.parseInt(time[0])
        val tpMinute = Integer.parseInt(time[1])
        val tpDialog = TimePickerDialog(
            context, this, tpHour, tpMinute, true
        )
        tpDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val time = setSummary(hourOfDay, minute)
        text = time
        with(AlarmHandler(context)) {
            scheduleAlarm(alarm)
        }
    }
}