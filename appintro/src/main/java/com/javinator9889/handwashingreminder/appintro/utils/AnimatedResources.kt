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
 * Created by Javinator9889 on 9/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.appintro.utils

import androidx.annotation.RawRes
import com.javinator9889.handwashingreminder.appintro.R


enum class AnimatedResources(@RawRes val res: Int) {
    WASH_HANDS(R.raw.wash_your_hands),
    TIMER(R.raw.pending_timer),
    ACTIVITY(R.raw.travelers_walking),
    PRIVACY(R.raw.padlock_animation)
}
