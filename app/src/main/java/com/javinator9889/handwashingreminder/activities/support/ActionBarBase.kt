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
 * Created by Javinator9889 on 20/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.support

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.ViewBindingInflater
import javinator9889.localemanager.activity.BaseAppCompatActivity

abstract class ActionBarBase<T : ViewBinding> : BaseAppCompatActivity(),
    ViewBindingInflater<T> {
    @get:LayoutRes
    protected abstract val layoutId: Int
    protected lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = inflateLayout()
        setContentView(layout.root)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}