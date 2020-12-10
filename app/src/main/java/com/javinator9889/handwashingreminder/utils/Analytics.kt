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
 * Created by Javinator9889 on 10/12/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import kotlin.reflect.KClass


fun FirebaseAnalytics.setCurrentScreen(name: String?, cls: KClass<*>?) {
    with(Bundle(2)) {
        putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
        putString(FirebaseAnalytics.Param.SCREEN_CLASS, cls?.simpleName)
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, this)
    }
}
