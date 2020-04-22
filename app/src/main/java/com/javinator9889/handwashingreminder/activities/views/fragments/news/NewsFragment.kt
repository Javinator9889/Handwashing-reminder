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
 * Created by Javinator9889 on 12/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.fragments.news

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import com.javinator9889.handwashingreminder.utils.RemoteConfig
import kotlinx.android.synthetic.main.under_construction.*

private const val ARG_UNDER_CONSTRUCTION_TEXT = "news:text:content"

class NewsFragment : BaseFragmentView() {
    @LayoutRes
    override val layoutId: Int = R.layout.under_construction

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            underConstructionText.text = savedInstanceState.getString(
                ARG_UNDER_CONSTRUCTION_TEXT
            )
        } else {
            with(Firebase.remoteConfig) {
                underConstructionText.text =
                    getString(RemoteConfig.WORK_IN_PROGRESS)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            ARG_UNDER_CONSTRUCTION_TEXT, underConstructionText.text.toString()
        )
    }
}