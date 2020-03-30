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
package com.javinator9889.handwashingreminder.appintro

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroViewPager
import com.google.android.gms.common.ConnectionResult.SUCCESS
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.javinator9889.handwashingreminder.appintro.config.TimeConfigActivity
import com.javinator9889.handwashingreminder.appintro.custom.SliderPageBuilder
import com.javinator9889.handwashingreminder.appintro.fragments.SlidePolicyFragment
import com.javinator9889.handwashingreminder.appintro.fragments.TimeConfigIntroFragment
import com.javinator9889.handwashingreminder.appintro.timeconfig.TimeConfigViewHolder
import com.javinator9889.handwashingreminder.listeners.ViewHolder
import com.javinator9889.handwashingreminder.utils.*
import com.javinator9889.handwashingreminder.views.activities.MainActivity
import com.javinator9889.handwashingreminder.R as RBase
import com.javinator9889.handwashingreminder.appintro.R as RIntro


class IntroActivity : AppIntro2(),
    ViewHolder.OnItemClickListener,
    AppIntroViewPager.OnNextPageRequestedListener,
    View.OnClickListener {
    private lateinit var timeConfigSlide: TimeConfigIntroFragment
    private lateinit var fourthSlide: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firstSlide = SliderPageBuilder.Builder()
            .title(getString(RIntro.string.first_slide_title))
            .description(getString(RIntro.string.first_slide_desc))
            .imageDrawable(RBase.drawable.handwashing_app_logo)
            .build()
        addSlide(firstSlide)

        val secondSlide = SliderPageBuilder.Builder()
            .title(getString(RIntro.string.second_slide_title))
            .description(getString(RIntro.string.second_slide_desc))
            .imageDrawable(RIntro.drawable.ic_clock)
            .build()
        addSlide(secondSlide)

        timeConfigSlide = TimeConfigIntroFragment()
        timeConfigSlide.bgColor = Color.WHITE
        timeConfigSlide.listener = this
        timeConfigSlide.fromActivity = this
        addSlide(timeConfigSlide)

        val gms = GoogleApiAvailability.getInstance()
        if (gms.isGooglePlayServicesAvailable(this) == SUCCESS) {
            fourthSlide = SliderPageBuilder.Builder()
                .title(getString(RIntro.string.fourth_slide_title))
                .description(getString(RIntro.string.fourth_slide_desc))
                .imageDrawable(RIntro.drawable.ic_activty)
                .build()
            addSlide(fourthSlide)
        }

        val policySlide = SlidePolicyFragment().apply {
            title = this@IntroActivity
                .getString(RIntro.string.privacy_policy_title)
            imageDrawable = RIntro.drawable.ic_privacy
            titleColor = Color.DKGRAY
            bgColor = Color.WHITE
        }
        addSlide(policySlide)

        showSkipButton(false)
        showStatusBar(true)
        backButtonVisibilityWithDone = true;
        setIndicatorColor(Color.DKGRAY, Color.GRAY);
        nextButton.setOnClickListener(this)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    override fun onItemClick(
        viewHolder: RecyclerView.ViewHolder?,
        view: View?,
        position: Int,
        id: Long
    ) {
        if (viewHolder == null || viewHolder !is TimeConfigViewHolder)
            return
        val intent = Intent(this, TimeConfigActivity::class.java)
        val options = if (isAtLeast(AndroidVersion.LOLLIPOP)) {
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair.create(
                    viewHolder.title, TimeConfigActivity.VIEW_TITLE_NAME
                ),
                Pair.create(
                    viewHolder.image, TimeConfigActivity.INFO_IMAGE_NAME
                ),
                Pair.create(
                    viewHolder.clockIcon, TimeConfigActivity.USER_TIME_ICON
                ),
                Pair.create(
                    viewHolder.hours, TimeConfigActivity.USER_TIME_HOURS
                ),
                Pair.create(viewHolder.ddot, TimeConfigActivity.USER_DDOT),
                Pair.create(
                    viewHolder.minutes, TimeConfigActivity.USER_TIME_MINUTES
                )
            )
        } else {
            null
        }
        Log.d("Intro", options?.toString())
        intent.putExtra(
            "title", viewHolder.title.text
        )
        intent.putExtra(
            "hours", viewHolder.hours.text
        )
        intent.putExtra(
            "minutes", viewHolder.minutes.text
        )
        intent.putExtra("id", id)
        ActivityCompat.startActivityForResult(
            this, intent, id.toInt(), options?.toBundle()
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null)
            return
        val view: TimeConfigViewHolder = when (requestCode.toLong()) {
            TimeConfig.BREAKFAST_ID -> {
                timeConfigSlide.viewItems[TimeConfig.BREAKFAST_ID.toInt()]
            }
            TimeConfig.LUNCH_ID -> {
                timeConfigSlide.viewItems[TimeConfig.LUNCH_ID.toInt()]
            }
            TimeConfig.DINNER_ID -> {
                timeConfigSlide.viewItems[TimeConfig.DINNER_ID.toInt()]
            }
            else -> null
        } as TimeConfigViewHolder
        view.hours.text = data.getStringExtra("hours")
        view.minutes.text = data.getStringExtra("minutes")
    }

    override fun onSlideChanged(
        oldFragment: Fragment?,
        newFragment: Fragment?
    ) {
        when (newFragment) {
            timeConfigSlide -> {
                setSwipeLock(true)
                return
            }
            else -> setSwipeLock(false)
        }
        when (oldFragment) {
            fourthSlide -> askForPermissions(
                this,
                Permission(Manifest.permission.ACTIVITY_RECOGNITION, 0)
            )
        }
        super.onSlideChanged(oldFragment, newFragment)
    }

    protected fun isTimeConfigValidState(
        timeConfigIntroFragment: Fragment?
    ): Boolean {
        return when (timeConfigIntroFragment) {
            timeConfigSlide -> {
                var isTimeSet = true
                for (view in timeConfigSlide.viewItems) {
                    val viewHolder = view.value as TimeConfigViewHolder
                    val hours = viewHolder.hours
                    val minutes = viewHolder.minutes
                    if (hours.text == "" || minutes.text == "") {
                        isTimeSet = false
                        break
                    }
                }
                setSwipeLock(!isTimeSet)
                if (!isTimeSet) {
                    val background =
                        findViewById<FrameLayout>(RIntro.id.background)
                    Snackbar.make(
                        background,
                        RIntro.string.fill_hours,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                isTimeSet
            }
            else -> true
        }
    }

    override fun onCanRequestNextPage(): Boolean {
        val ret = super.onCanRequestNextPage()
        val currentFragment = mPagerAdapter.getItem(pager.currentItem)
        return ret && isTimeConfigValidState(currentFragment)
    }

    override fun onClick(v: View?) {
        when (v) {
            nextButton -> {
                if (onCanRequestNextPage())
                    changeSlide()
            }
        }
    }

    private fun changeSlide() {
        val currentSlide =
            mPagerAdapter.getItem(pager.currentItem)
        val nextSlide =
            mPagerAdapter.getItem(pager.currentItem + 1)
        onSlideChanged(currentSlide, nextSlide)
        pager.goToNextSlide()
    }
}