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

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.LayoutRes
import androidx.preference.DialogPreference
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.emoji.EmojiLoader
import com.javinator9889.handwashingreminder.utils.Preferences

class TimePreference : DialogPreference {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        0
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, defStyleAttr)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    var time: String = ""
        set(value) {
            field = value
            persistString(value)
            setSummary(value)
        }

//    private val summaryText: CharSequence

    init {
        val emojiLoader = EmojiLoader.get(context)
        val data = when (key) {
            Preferences.BREAKFAST_TIME -> PreferenceData(
                context.getText(R.string.breakfast_pref_title),
                context.getText(R.string.breakfast_pref_summ)
            )
            Preferences.LUNCH_TIME -> PreferenceData(
                context.getText(R.string.lunch_pref_title),
                context.getText(R.string.lunch_pref_summ)
            )
            Preferences.DINNER_TIME -> PreferenceData(
                context.getText(R.string.dinner_pref_title),
                context.getText(R.string.dinner_pref_summ)
            )
            else -> PreferenceData("", "")
        }
//        val emojiCompat = runBlocking { emojiLoader.await() }
//        title = emojiCompat.process(data.title)
//        summaryText = emojiCompat.process(data.summary)
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any? =
        a?.getString(index)

    override fun onSetInitialValue(defaultValue: Any?) {
        this.time = getPersistedString("00:00")
    }

    @LayoutRes
    override fun getDialogLayoutResource(): Int = R.layout.pref_dialog_time

    private fun setSummary(time: String): String {
//        summary = "$summaryText $time"
        return time
    }

    private data class PreferenceData(
        val title: CharSequence,
        val summary: CharSequence
    )
}