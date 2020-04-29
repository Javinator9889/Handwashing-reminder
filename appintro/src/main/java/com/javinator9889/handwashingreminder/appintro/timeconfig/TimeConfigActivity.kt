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
package com.javinator9889.handwashingreminder.appintro.timeconfig

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.google.android.play.core.splitcompat.SplitCompat
import com.javinator9889.handwashingreminder.activities.support.ActionBarBase
import com.javinator9889.handwashingreminder.appintro.R
import com.javinator9889.handwashingreminder.graphics.GlideApp
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.TimeConfig
import com.javinator9889.handwashingreminder.utils.formatTime
import com.javinator9889.handwashingreminder.utils.isAtLeast
import timber.log.Timber
import java.util.*
import kotlin.properties.Delegates

class TimeConfigActivity :
    ActionBarBase(),
    View.OnClickListener,
    TimePickerDialog.OnTimeSetListener {
    companion object Transitions {
        const val VIEW_TITLE_NAME = "detail:header:title"
        const val INFO_IMAGE_NAME = "detail:header:image"
        const val USER_TIME_ICON = "detail:body:time-icon"
        const val USER_TIME_HOURS = "detail:body:time-hours"
        const val USER_TIME_MINUTES = "detail:body:time-minutes"
        const val USER_DDOT = "detail:body:time-ddot"
    }

    override val layoutId: Int = R.layout.time_card_view_expanded
    private lateinit var doneButton: Button
    private lateinit var title: TextView
    private lateinit var image: ImageView
    private lateinit var setButton: Button
    private lateinit var timeContainer: ConstraintLayout
    private lateinit var ddot: TextView
    private lateinit var hours: TextView
    private lateinit var minutes: TextView
    private lateinit var clockIcon: ImageView
    private var id by Delegates.notNull<Long>()
    private var position by Delegates.notNull<Int>()

    data class Time(val hour: Int, val minute: Int)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = findViewById(R.id.title)
        doneButton = findViewById(R.id.doneButton)
        image = findViewById(R.id.infoImage)
        setButton = findViewById(R.id.setTimeButton)
        timeContainer = findViewById(R.id.timeContainer)
        ddot = timeContainer.findViewById(R.id.ddot)
        hours = timeContainer.findViewById(R.id.hours)
        minutes = timeContainer.findViewById(R.id.minutes)
        clockIcon = timeContainer.findViewById(R.id.clockIcon)

        doneButton.setOnClickListener(this)
        setButton.setOnClickListener(this)
        hours.setOnClickListener(this)
        ddot.setOnClickListener(this)
        minutes.setOnClickListener(this)
        clockIcon.setOnClickListener(this)
        ViewCompat.setTransitionName(title,
            VIEW_TITLE_NAME
        )
        ViewCompat.setTransitionName(image,
            INFO_IMAGE_NAME
        )
        ViewCompat.setTransitionName(hours,
            USER_TIME_HOURS
        )
        ViewCompat.setTransitionName(ddot,
            USER_DDOT
        )
        ViewCompat.setTransitionName(minutes,
            USER_TIME_MINUTES
        )
        ViewCompat.setTransitionName(clockIcon,
            USER_TIME_ICON
        )

        if (savedInstanceState != null || intent.extras != null) {
            val data = savedInstanceState ?: intent.extras
            val sHours = data!!.getCharSequence("hours")
            val sMinutes = data.getCharSequence("minutes")
            title.text = data.getCharSequence("title")
            id = data.getLong("id", TimeConfig.BREAKFAST_ID)
            position = data.getInt("position", 0)
            val imageRes = when (id) {
                TimeConfig.BREAKFAST_ID -> R.drawable.ic_breakfast
                TimeConfig.LUNCH_ID -> R.drawable.ic_lunch
                TimeConfig.DINNER_ID -> R.drawable.ic_dinner
                else -> null
            }
            if (imageRes != null)
                try {
                    GlideApp.with(this)
                        .load(imageRes)
                        .centerInside()
                        .into(image)
                } catch (e: Exception) {
                    Timber.e(e, "Error while loading Glide view")
                    image.setImageResource(imageRes)
                }
            setHours(sHours.toString(), sMinutes.toString())
        }
    }

    private fun setHours(hours: String, minutes: String) {
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
        this.hours.text = formatTime(tpHour)
        this.minutes.text = formatTime(tpMinute)
    }

    private fun getHours(): Time {
        val tpHour = Integer.parseInt(hours.text.toString())
        val tpMinute = Integer.parseInt(minutes.text.toString())
        return Time(
            tpHour,
            tpMinute
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val tpTime = getHours()
        val hour = formatTime(tpTime.hour)
        val minute = formatTime(tpTime.minute)
        outState.putCharSequence("hours", hour)
        outState.putCharSequence("minutes", minute)
        outState.putCharSequence("title", title.text)
        outState.putLong("id", id)
        outState.putInt("position", position)
    }

    override fun onBackPressed() {
        val intent = Intent()
        val tpTime = getHours()
        intent.putExtra("hours", formatTime(tpTime.hour))
        intent.putExtra("minutes", formatTime(tpTime.minute))
        intent.putExtra("id", id)
        intent.putExtra("position", position)
        setResult(Activity.RESULT_OK, intent)
        if (isAtLeast(AndroidVersion.LOLLIPOP))
            finishAfterTransition()
        else
            finish()
        super.onBackPressed()
    }

    override fun onClick(v: View?) {
        when (v) {
            setButton, hours, minutes, clockIcon, ddot -> {
                val tpHour = Integer.parseInt(hours.text as String)
                val tpMinute = Integer.parseInt(minutes.text as String)
                val tpDialog = TimePickerDialog(
                    this, this, tpHour, tpMinute, true
                )
                tpDialog.show()
            }
            doneButton -> this.onBackPressed()
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        hours.text = formatTime(hourOfDay)
        minutes.text = formatTime(minute)
    }
}