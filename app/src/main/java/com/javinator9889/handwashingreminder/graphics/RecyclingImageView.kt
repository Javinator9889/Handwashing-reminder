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
 * Created by Javinator9889 on 1/07/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.graphics

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import coil.api.load

class RecyclingImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    @DrawableRes
    var savedDrawableRes: Int? = null

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (savedDrawableRes != null
            && visibility == View.VISIBLE
            && drawable != null
        )
            try {
                load(savedDrawableRes!!)
            } catch (ignored: Throwable) {
                setImageResource(savedDrawableRes!!)
            }
        else if (visibility == View.INVISIBLE || visibility == View.GONE)
            onDetachedFromWindow()
    }

    /**
     * @see android.widget.ImageView.onDetachedFromWindow
     */
    public override fun onDetachedFromWindow() {
        // This has been detached from Window, so clear the drawable
        setImageDrawable(null)
        super.onDetachedFromWindow()
    }

    /**
     * @see android.widget.ImageView.setImageDrawable
     */
    override fun setImageDrawable(drawable: Drawable?) {
        // Keep hold of previous Drawable
        val previousDrawable = getDrawable()

        // Call super to set new Drawable
        super.setImageDrawable(drawable)

        // Notify new Drawable that it is being displayed
        notifyDrawable(drawable, true)

        // Notify old Drawable so it is no longer being displayed
        notifyDrawable(previousDrawable, false)
    }

    /**
     * Notifies the drawable that it's displayed state has changed.
     */
    private fun notifyDrawable(
        drawable: Drawable?,
        isDisplayed: Boolean
    ) {
        if (drawable is RecyclingBitmapDrawable) {
            // The drawable is a CountingBitmapDrawable, so notify it
            drawable.setIsDisplayed(isDisplayed)
        } else if (drawable is LayerDrawable) {
            // The drawable is a LayerDrawable, so recurse on each layer
            var i = 0
            val z = drawable.numberOfLayers
            while (i < z) {
                notifyDrawable(drawable.getDrawable(i), isDisplayed)
                i++
            }
        }
    }
}