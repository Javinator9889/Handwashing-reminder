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
 * Created by Javinator9889 on 15/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.fragments.washinghands

import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.BaseFragmentView
import kotlinx.android.synthetic.main.wash_your_hands_first_slide.*

class FirstSlide : BaseFragmentView() {
    override val layoutId: Int = R.layout.wash_your_hands_first_slide

    override fun onPause() {
        super.onPause()
        animation.pauseAnimation()
    }

    override fun onResume() {
        super.onResume()
        animation.playAnimation()
        image.setImageResource(R.drawable.handwashing_app_logo)
    }
}