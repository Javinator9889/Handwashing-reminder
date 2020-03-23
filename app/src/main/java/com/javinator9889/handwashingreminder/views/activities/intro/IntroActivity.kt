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
package com.javinator9889.handwashingreminder.views.activities.intro

import android.content.Intent
import android.graphics.Color
import android.graphics.Point
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
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.AppIntroViewPager
import com.github.paolorotolo.appintro.model.SliderPage
import com.google.android.material.snackbar.Snackbar
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.listeners.ViewHolder
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.TimeConfig
import com.javinator9889.handwashingreminder.utils.getOnClickListener
import com.javinator9889.handwashingreminder.utils.isAtLeast
import com.javinator9889.handwashingreminder.views.activities.config.TimeConfigActivity
import com.javinator9889.handwashingreminder.views.custom.timeconfig.TimeConfigViewHolder
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.iconics.Iconics


class IntroActivity : AppIntro2(),
    ViewHolder.OnItemClickListener,
    AppIntroViewPager.OnNextPageRequestedListener,
    View.OnClickListener {
    private lateinit var transitions: Array<String>
    private lateinit var timeConfigSlide: TimeConfigIntroFragment
    private var appIntroListener: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Iconics.init()
//        supportActionBar?.hide()
        transitions = resources.getStringArray(R.array.transitions)
//        setSwipeLock(false)

        val sliderPage = SliderPage()
        sliderPage.title = "First page"
        sliderPage.description = "First page description"
        sliderPage.imageDrawable = R.drawable.handwashing_app_logo
        sliderPage.bgColor = Color.WHITE
        sliderPage.titleColor = Color.DKGRAY
        sliderPage.descColor = Color.DKGRAY
        addSlide(AppIntro2Fragment.newInstance(sliderPage))

        val sliderPage2 = SliderPage()
        sliderPage2.title = "Second page"
        sliderPage2.description = "Second page description"
        sliderPage2.imageDrawable = R.drawable.handwashing_app_logo
        sliderPage2.bgColor = Color.WHITE
        sliderPage2.titleColor = Color.DKGRAY
        sliderPage2.descColor = Color.DKGRAY
        addSlide(AppIntro2Fragment.newInstance(sliderPage2))

        val sliderPage3 = SliderPage()
        sliderPage3.title = "Third page"
        sliderPage3.description = "Third page description"
        sliderPage3.imageDrawable = R.drawable.ic_handwashing_icon
        sliderPage3.bgColor = Color.WHITE
        sliderPage3.titleColor = Color.DKGRAY
        sliderPage3.descColor = Color.DKGRAY
        addSlide(AppIntro2Fragment.newInstance(sliderPage3))

        timeConfigSlide = TimeConfigIntroFragment()
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        timeConfigSlide.height = size.y - 80
        timeConfigSlide.bgColor = Color.WHITE
        timeConfigSlide.listener = this
        timeConfigSlide.fromActivity = this
        addSlide(timeConfigSlide)

        val sliderPage5 = SliderPage()
        sliderPage5.title = "Fifth page"
        sliderPage5.description = "Fifth page description"
        sliderPage5.imageDrawable = R.drawable.ic_handwashing_icon
        sliderPage5.bgColor = Color.WHITE
        sliderPage5.titleColor = Color.DKGRAY
        sliderPage5.descColor = Color.DKGRAY
        addSlide(AppIntro2Fragment.newInstance(sliderPage5))

        showSkipButton(false)
        showStatusBar(true)
        backButtonVisibilityWithDone = true;
        setIndicatorColor(Color.DKGRAY, Color.GRAY);
        appIntroListener = getOnClickListener(nextButton)
        nextButton.setOnClickListener(this)
//        setNavBarColor("#2196F3")
//        setBarColor(getColor(R.color.colorPrimary));
//        setNavBarColor(R.color.white)
//        val stackTransformer = StackTransformer()
//        setCustomTransformer(stackTransformer)

//        askForPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 3)
//        setDepthAnimation()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
        LibsBuilder()
            .withAboutDescription("This is a test desc")
            .withAboutIconShown(true)
            .withAboutVersionShown(true)
            .withAboutVersionShownCode(true)
            .withSortEnabled(true)
            .withCheckCachedDetection(true)
            .withAutoDetect(true)
            .start(this)
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
        }
        super.onSlideChanged(oldFragment, newFragment)
    }

    protected fun isTimeConfigValidState(
        timeConfigIntroFragment: Fragment?
    ): Boolean {
        Log.d("Intro/fragment", timeConfigIntroFragment.toString())
        Log.d("Intro/fragment", timeConfigSlide.toString())
        Log.d(
            "Intro/fragment",
            (timeConfigIntroFragment == timeConfigSlide).toString()
        )
        return when (timeConfigIntroFragment) {
            timeConfigSlide -> {
                Log.d("Intro/fragment", "Inside when block for configSlide")
                var isTimeSet = true
                Log.d("Map size", timeConfigSlide.viewItems.size.toString())
                for (view in timeConfigSlide.viewItems) {
                    val viewHolder = view.value as TimeConfigViewHolder
                    val hours = viewHolder.hours
                    val minutes = viewHolder.minutes
                    Log.d(
                        "Intro/HHMM",
                        "HH: ${hours.text} | MM: ${minutes.text}"
                    )
                    if (hours.text == "" || minutes.text == "") {
                        isTimeSet = false
                        break
                    }
                }
                setSwipeLock(!isTimeSet)
                if (!isTimeSet) {
                    val background = findViewById<FrameLayout>(R.id.background)
                    Snackbar.make(
                        background, R.string.fill_hours,
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
        Log.d(
            "Intro", "Can go to next slide? " +
                    "${ret && isTimeConfigValidState(currentFragment)}"
        )
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