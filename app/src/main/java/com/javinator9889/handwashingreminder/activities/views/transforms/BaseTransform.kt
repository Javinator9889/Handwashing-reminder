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
 * Created by Javinator9889 on 16/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.transforms

import android.view.View
import androidx.viewpager.widget.ViewPager


abstract class BaseTransform : ViewPager.PageTransformer {
    /**
     * Called each [.transformPage].
     *
     * @param view
     * @param position
     */
    protected abstract fun onTransform(
        view: View?,
        position: Float
    )

    override fun transformPage(
        view: View,
        position: Float
    ) {
        onPreTransform(view, position)
        onTransform(view, position)
    }

    /**
     * If the position offset of a fragment is less than negative one or greater
     * than one, returning true will set the
     * visibility of the fragment to [android.view.View.GONE]. Returning false
     * will force the fragment to [android.view.View.VISIBLE].
     *
     * @return
     */
    protected fun hideOffscreenPages(): Boolean {
        return true
    }

    /**
     * Indicates if the default animations of the view pager should be used.
     *
     * @return
     */
    protected val isPagingEnabled: Boolean
        get() = false

    /**
     * Called each [.transformPage] before {[.onTransform] is called.
     *
     * @param view
     * @param position
     */
    protected fun onPreTransform(
        view: View,
        position: Float
    ) {
        val width = view.width.toFloat()
        view.rotationX = 0f
        view.rotationY = 0f
        view.rotation = 0f
        view.scaleX = 1f
        view.scaleY = 1f
        view.pivotX = 0f
        view.pivotY = 0f
        view.translationY = 0f
        view.translationX = if (isPagingEnabled) 0f else -width * position
        if (hideOffscreenPages()) {
            view.alpha = if (position <= -1f || position >= 1f) 0f else 1f
        } else {
            view.alpha = 1f
        }
    }
}