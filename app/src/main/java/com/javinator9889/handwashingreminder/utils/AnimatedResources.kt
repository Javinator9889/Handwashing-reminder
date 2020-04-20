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
package com.javinator9889.handwashingreminder.utils

import androidx.annotation.RawRes
import com.javinator9889.handwashingreminder.R

enum class AnimatedResources(@RawRes val res: Int) {
    KEEP_THEM_SAFE(R.raw.keep_them_safe),
    STAY_SAFE_STAY_HOME(R.raw.stay_safe_stay_home)
}