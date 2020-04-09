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
package com.javinator9889.handwashingreminder.appintro.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder
import com.github.paolorotolo.appintro.ISlideSelectionListener
import com.github.paolorotolo.appintro.util.LogHelper
import com.github.paolorotolo.appintro.util.TypefaceContainer
import com.google.android.play.core.splitcompat.SplitCompat
import com.javinator9889.handwashingreminder.appintro.R
import com.javinator9889.handwashingreminder.appintro.custom.*

class AnimatedAppIntro : Fragment(),
    ISlideSelectionListener, ISlideBackgroundColorHolder {
    private var drawable = 0
    private var bgColor = 0
    private var titleColor = 0
    private var descColor = 0

    @get:LayoutRes
    protected val layoutId: Int = R.layout.animated_intro
    private var title: String? = null
    private var animatedDrawable: String? = null
    private var animationLoop: Boolean = false
    private var description: String? = null
    private var titleTypeface: TypefaceContainer? = null
    private var descTypeface: TypefaceContainer? = null
    private var mainLayout: ConstraintLayout? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        SplitCompat.installActivity(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        if (arguments != null && arguments!!.size() != 0) {
            val argsTitleTypeface = arguments!!.getString(ARG_TITLE_TYPEFACE)
            val argsDescTypeface = arguments!!.getString(ARG_DESC_TYPEFACE)
            val argsTitleTypefaceRes =
                arguments!!.getInt(ARG_TITLE_TYPEFACE_RES)
            val argsDescTypefaceRes = arguments!!.getInt(ARG_DESC_TYPEFACE_RES)
            drawable = arguments!!.getInt(ARG_DRAWABLE)
            animatedDrawable = arguments!!.getString(ARG_ANIM_DRAWABLE)
            animationLoop = arguments!!.getBoolean(ARG_ANIM_LOOP)
            title = arguments!!.getString(ARG_TITLE)
            description = arguments!!.getString(ARG_DESC)
            titleTypeface =
                TypefaceContainer(argsTitleTypeface, argsTitleTypefaceRes)
            descTypeface =
                TypefaceContainer(argsDescTypeface, argsDescTypefaceRes)
            bgColor = arguments!!.getInt(ARG_BG_COLOR)
            titleColor =
                if (arguments!!.containsKey(ARG_TITLE_COLOR))
                    arguments!!.getInt(ARG_TITLE_COLOR)
                else 0
            descColor =
                if (arguments!!.containsKey(ARG_DESC_COLOR))
                    arguments!!.getInt(ARG_DESC_COLOR)
                else 0
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            drawable = savedInstanceState.getInt(ARG_DRAWABLE)
            animatedDrawable = savedInstanceState.getString(ARG_ANIM_DRAWABLE)
            animationLoop = savedInstanceState.getBoolean(ARG_ANIM_LOOP)
            title = savedInstanceState.getString(ARG_TITLE)
            description = savedInstanceState.getString(ARG_DESC)
            titleTypeface = TypefaceContainer(
                savedInstanceState.getString(ARG_TITLE_TYPEFACE),
                savedInstanceState.getInt(ARG_TITLE_TYPEFACE_RES, 0)
            )
            descTypeface = TypefaceContainer(
                savedInstanceState.getString(ARG_DESC_TYPEFACE),
                savedInstanceState.getInt(ARG_DESC_TYPEFACE_RES, 0)
            )
            bgColor = savedInstanceState.getInt(ARG_BG_COLOR)
            titleColor = savedInstanceState.getInt(ARG_TITLE_COLOR)
            descColor = savedInstanceState.getInt(ARG_DESC_COLOR)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layoutId, container, false)
        val titleText = view.findViewById<TextView>(R.id.title)
        val descriptionText = view.findViewById<TextView>(R.id.description)
        val slideImage = view.findViewById<LottieAnimationView>(R.id.image)
        mainLayout = view.findViewById(R.id.main)
        titleText.text = title
        if (titleColor != 0) {
            titleText.setTextColor(titleColor)
        }
        titleTypeface!!.applyTo(titleText)
        titleTypeface!!.applyTo(descriptionText)
        descriptionText.text = description
        if (descColor != 0) {
            descriptionText.setTextColor(descColor)
        }
        if (animatedDrawable != null)
            slideImage.setAnimation(animatedDrawable)
        else
            slideImage.setImageResource(drawable)
        slideImage.repeatCount =
            if (animationLoop) LottieDrawable.INFINITE else 0
        mainLayout!!.setBackgroundColor(bgColor)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_DRAWABLE, drawable)
        outState.putString(ARG_TITLE, title)
        outState.putString(ARG_DESC, description)
        outState.putInt(ARG_BG_COLOR, bgColor)
        outState.putInt(ARG_TITLE_COLOR, titleColor)
        outState.putInt(ARG_DESC_COLOR, descColor)
        outState.putString(ARG_ANIM_DRAWABLE, animatedDrawable)
        outState.putBoolean(ARG_ANIM_LOOP, animationLoop)
        saveTypefacesInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun saveTypefacesInstanceState(outState: Bundle) {
        if (titleTypeface != null) {
            outState.putString(ARG_TITLE_TYPEFACE, titleTypeface!!.typeFaceUrl)
            outState.putInt(
                ARG_TITLE_TYPEFACE_RES,
                titleTypeface!!.typeFaceResource
            )
        }
        if (descTypeface != null) {
            outState.putString(ARG_DESC_TYPEFACE, descTypeface!!.typeFaceUrl)
            outState.putInt(
                ARG_DESC_TYPEFACE_RES,
                descTypeface!!.typeFaceResource
            )
        }
    }

    override fun onSlideDeselected() {
        LogHelper.d(
            TAG,
            String.format("Slide %s has been deselected.", title)
        )
    }

    override fun onSlideSelected() {
        LogHelper.d(
            TAG,
            String.format("Slide %s has been selected.", title)
        )
    }

    override fun getDefaultBackgroundColor(): Int {
        return bgColor
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        mainLayout?.setBackgroundColor(backgroundColor)
    }

    companion object Creator {
        fun newInstance(sliderPage: AnimatedSliderPage): AnimatedAppIntro =
            AnimatedAppIntro().apply {
                arguments = sliderPage.toBundle()
            }

        private val TAG = LogHelper.makeLogTag(
            AnimatedAppIntro::class.java
        )
    }
}