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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.edit
import androidx.core.util.Pair
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroViewPager
import com.google.android.gms.common.ConnectionResult.SUCCESS
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.javinator9889.handwashingreminder.activities.MainActivity
import com.javinator9889.handwashingreminder.appintro.config.TimeConfigActivity
import com.javinator9889.handwashingreminder.appintro.custom.SliderPageBuilder
import com.javinator9889.handwashingreminder.appintro.fragments.AnimatedAppIntro
import com.javinator9889.handwashingreminder.appintro.fragments.SlidePolicyFragment
import com.javinator9889.handwashingreminder.appintro.fragments.TimeConfigIntroFragment
import com.javinator9889.handwashingreminder.appintro.timeconfig.TimeConfigViewHolder
import com.javinator9889.handwashingreminder.appintro.utils.AnimatedResources
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.listeners.ViewHolder
import com.javinator9889.handwashingreminder.utils.*
import kotlinx.android.synthetic.main.animated_intro.*
import com.javinator9889.handwashingreminder.appintro.R as RIntro


@Keep
class IntroActivity : AppIntro2(),
    ViewHolder.OnItemClickListener,
    AppIntroViewPager.OnNextPageRequestedListener,
    View.OnClickListener {
    private lateinit var activitySlide: Fragment
    private lateinit var policySlide: SlidePolicyFragment
    private lateinit var timeConfigSlide: TimeConfigIntroFragment
    private var activityRecognitionPermissionGranted: Boolean = false

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SplitCompat.installActivity(base)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firstSlide = SliderPageBuilder.Builder()
            .title(getString(RIntro.string.first_slide_title))
            .description(getString(RIntro.string.first_slide_desc))
            .animationResource(AnimatedResources.WASH_HANDS)
            .loopAnimation(true)
            .build()
        addSlide(firstSlide)

        val secondSlide = SliderPageBuilder.Builder()
            .title(getString(RIntro.string.second_slide_title))
            .description(getString(RIntro.string.second_slide_desc))
            .animationResource(AnimatedResources.TIMER)
            .loopAnimation(false)
            .build()
        addSlide(secondSlide)

        timeConfigSlide = TimeConfigIntroFragment()
        timeConfigSlide.bgColor = Color.WHITE
        timeConfigSlide.listener = this
        timeConfigSlide.fromActivity = this
        addSlide(timeConfigSlide)

        val gms = GoogleApiAvailability.getInstance()
        if (gms.isGooglePlayServicesAvailable(this) == SUCCESS) {
            activitySlide = SliderPageBuilder.Builder()
                .title(getString(RIntro.string.fourth_slide_title))
                .description(getString(RIntro.string.fourth_slide_desc))
                .animationResource(AnimatedResources.ACTIVITY)
                .loopAnimation(true)
                .build()
            addSlide(activitySlide)
        }

        policySlide = SlidePolicyFragment().apply {
            title = this@IntroActivity
                .getString(RIntro.string.privacy_policy_title)
            animatedDrawable = AnimatedResources.PRIVACY
            titleColor = Color.DKGRAY
            bgColor = Color.WHITE
        }
        addSlide(policySlide)

        showSkipButton(false)
        showStatusBar(true)
        backButtonVisibilityWithDone = true
        setIndicatorColor(Color.DKGRAY, Color.GRAY)
        nextButton.setOnClickListener(this)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val app = HandwashingApplication.getInstance()
        val sharedPreferences = app.sharedPreferences
        sharedPreferences.edit {
            timeConfigSlide.viewItems.forEach { key, value ->
                val time = "${value.hours.text}:${value.minutes}"
                when (key) {
                    TimeConfig.BREAKFAST_ID.toInt() ->
                        putString(Preferences.BREAKFAST_TIME, time)
                    TimeConfig.LUNCH_ID.toInt() ->
                        putString(Preferences.LUNCH_TIME, time)
                    TimeConfig.DINNER_ID.toInt() ->
                        putString(Preferences.DINNER_TIME, time)
                }
            }
            putBoolean(
                Preferences.ANALYTICS_ENABLED,
                policySlide.firebaseAnalytics.isChecked
            )
            putBoolean(
                Preferences.PERFORMANCE_ENABLED,
                policySlide.firebasePerformance.isChecked
            )
            putBoolean(
                Preferences.ADS_ENABLED,
                sharedPreferences.getBoolean(Preferences.ADS_ENABLED, true)
            )
            putBoolean(
                Preferences.ACTIVITY_TRACKING_ENABLED,
                activityRecognitionPermissionGranted
            )
            putBoolean(Preferences.APP_INIT_KEY, true)
        }
        val splitInstallManager = SplitInstallManagerFactory.create(this)
        splitInstallManager.deferredUninstall(listOf(AppIntro.MODULE_NAME))
        if (activityRecognitionPermissionGranted)
            app.activityHandler.startTrackingActivity()
        else
            app.activityHandler.disableActivityTracker()
        workManagerEnqueuer()
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
            val pairs = mutableListOf<Pair<View, String>>()
            val items = HashMap<String, View>(6).apply {
                this[TimeConfigActivity.VIEW_TITLE_NAME] = viewHolder.title
                this[TimeConfigActivity.INFO_IMAGE_NAME] = viewHolder.image
                this[TimeConfigActivity.USER_TIME_ICON] = viewHolder.clockIcon
                this[TimeConfigActivity.USER_TIME_HOURS] = viewHolder.hours
                this[TimeConfigActivity.USER_DDOT] = viewHolder.ddot
                this[TimeConfigActivity.USER_TIME_MINUTES] = viewHolder.minutes
            }
            val lm =
                timeConfigSlide.recyclerView.layoutManager as LinearLayoutManager
            if (position <= lm.findLastCompletelyVisibleItemPosition()) {
                items.onEach {
                    pairs.add(Pair.create(it.value, it.key))
                }
            }
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                *pairs.toTypedArray()
            )
        } else {
            null
        }
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
        view.saveContentToTextViews()
        setSwipeLock()
    }

    override fun onSlideChanged(
        oldFragment: Fragment?,
        newFragment: Fragment?
    ) {
        if (newFragment == timeConfigSlide) {
            setSwipeLock()
            return
        } else {
            setSwipeLock(false)
        }
        if (oldFragment == activitySlide)
            askForPermissions(
                this,
                Permission(
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    PERMISSIONS_REQUEST_CODE
                )
            )
        if (newFragment is AnimatedAppIntro ||
            newFragment is SlidePolicyFragment
        )
            newFragment.image.playAnimation()
        super.onSlideChanged(oldFragment, newFragment)
    }

    private fun setSwipeLock() {
        var swipeLock = false
        timeConfigSlide.viewItems.forEach { _, value ->
            if (value.hours.text == "" && value.minutes.text == "") {
                swipeLock = true
                return@forEach
            }
        }
        setSwipeLock(swipeLock)
    }

    private fun isTimeConfigValidState(
        timeConfigIntroFragment: Fragment?
    ): Boolean {
        return when (timeConfigIntroFragment) {
            timeConfigSlide -> {
                var isTimeSet = true
                timeConfigSlide.viewItems.forEach { _, value ->
                    val hours = value.hours
                    val minutes = value.minutes
                    if (hours.text == "" || minutes.text == "") {
                        isTimeSet = false
                        return@forEach
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            activityRecognitionPermissionGranted =
                grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
