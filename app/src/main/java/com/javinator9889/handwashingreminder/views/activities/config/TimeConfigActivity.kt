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
 * Created by Javinator9889 on 19/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.views.activities.config

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.isAtLeast
import com.javinator9889.handwashingreminder.views.activities.support.ActionBarBase
import java.util.*

class TimeConfigActivity :
    ActionBarBase(),
    View.OnClickListener {
    companion object {
        const val VIEW_TITLE_NAME = "detail:header:title"
    }

    private lateinit var button: Button
    private lateinit var title: TextView
    private lateinit var timePicker: TimePicker

    data class Time(val hour: Int, val minute: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.time_card_view_expanded)

        title = findViewById(R.id.title)
        button = findViewById(R.id.doneButton)
        timePicker = findViewById(R.id.timePicker)

        timePicker.setIs24HourView(true)
        button.setOnClickListener(this)
        if (isAtLeast(AndroidVersion.LOLLIPOP))
            title.transitionName = VIEW_TITLE_NAME
//        ViewCompat.setTransitionName(title, VIEW_TITLE_NAME)

        if (savedInstanceState != null || intent.extras != null) {
            val data = savedInstanceState ?: intent.extras
            val sHours = data!!.getCharSequence("hours")
            val sMinutes = data.getCharSequence("minutes")
            title.text = data.getCharSequence("title")
            setTimePickerHour(sHours.toString(), sMinutes.toString())
            val id = data.getLong("id").toInt()
            if (isAtLeast(AndroidVersion.LOLLIPOP)) {
                val transitions = resources.getStringArray(R.array.transitions)
                val transitionName = transitions[id]
                title.transitionName = transitionName
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.time_card_view_expanded

    private fun setTimePickerHour(hours: String, minutes: String) {
        val tpHour: Int
        val tpMinute: Int
        if (hours == "" || minutes == "") {
            val date = Calendar.getInstance()
            tpHour = date.get(Calendar.HOUR_OF_DAY)
            tpMinute = date.get(Calendar.MINUTE)
        } else {
            tpHour = Integer.parseInt(hours)
            tpMinute = Integer.parseInt(minutes)
        }
        if (isAtLeast(AndroidVersion.M)) {
            timePicker.hour = tpHour
            timePicker.minute = tpMinute
        } else {
            timePicker.currentHour = tpHour
            timePicker.currentMinute = tpMinute
        }
    }

    private fun getTimePickerHour(): Time {
        val tpHour: Int
        val tpMinute: Int
        if (isAtLeast(AndroidVersion.M)) {
            tpHour = timePicker.hour
            tpMinute = timePicker.minute
        } else {
            tpHour = timePicker.currentHour
            tpMinute = timePicker.currentMinute
        }
        return Time(tpHour, tpMinute)
    }

    private fun formatTime(time: Int): String {
        return if (time < 10) "0$time" else time.toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val tpTime = getTimePickerHour()
        val hour = formatTime(tpTime.hour)
        val minute = formatTime(tpTime.minute)
        outState.putCharSequence("hours", hour)
        outState.putCharSequence("minutes", minute)
        outState.putCharSequence("title", title.text)
    }

    override fun onBackPressed() {
        val intent = Intent()
        val tpTime = getTimePickerHour()
        intent.putExtra("hours", formatTime(tpTime.hour))
        intent.putExtra("minutes", formatTime(tpTime.minute))
        setResult(Activity.RESULT_OK, intent)
        if (isAtLeast(AndroidVersion.LOLLIPOP))
            finishAfterTransition()
        else
            finish()
        super.onBackPressed()
    }

    override fun onClick(v: View?) {
        when (v) {
            button -> this.onBackPressed()
        }
    }
}