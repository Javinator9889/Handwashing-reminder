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
import android.util.AttributeSet
import androidx.preference.MultiSelectListPreference
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.ionicons.Ionicons
import com.mikepenz.iconics.utils.sizeDp
import java.util.*

class ActivityMultiSelectList : MultiSelectListPreference {
    private var isFirstCall = true
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        icon = IconicsDrawable(context, Ionicons.Icon.ion_android_alert).apply {
            sizeDp = 20
        }
    }

    override fun notifyChanged() {
        super.notifyChanged()
        if (!isFirstCall) {
            loadSummary()
            reloadActivityHandler()
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        super.onSetInitialValue(defaultValue)
        isFirstCall = false
        loadSummary()
    }

    private fun loadSummary() {
        val builder = StringBuilder()
        var isFirstItem = true
        var allDisabled = true
        selectedItems.forEachIndexed { i, enabled ->
            if (enabled) {
                allDisabled = false
                if (!isFirstItem)
                    builder.append(", ")
                else
                    isFirstItem = false
                builder.append(entries[i].toString().toLowerCase(Locale.ROOT))
            }
        }
        builder.append('.')
        summary = if (allDisabled)
            context.getText(R.string.activities_disabled)
        else
            "${context.getString(R.string.activities_info)} $builder"
    }

    private fun reloadActivityHandler() {
        with(HandwashingApplication.instance) {
            activityHandler.reload()
        }
    }
}