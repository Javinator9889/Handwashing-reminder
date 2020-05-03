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
import androidx.core.content.edit
import androidx.core.util.set
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroViewPager
import com.google.android.gms.common.ConnectionResult.SUCCESS
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.javinator9889.handwashingreminder.activities.MainActivity
import com.javinator9889.handwashingreminder.appintro.custom.SliderPageBuilder
import com.javinator9889.handwashingreminder.appintro.fragments.AnimatedAppIntro
import com.javinator9889.handwashingreminder.appintro.fragments.SlidePolicyFragment
import com.javinator9889.handwashingreminder.appintro.fragments.TimeConfigIntroFragment
import com.javinator9889.handwashingreminder.appintro.fragments.TimeContainer
import com.javinator9889.handwashingreminder.appintro.timeconfig.TimeConfigItem
import com.javinator9889.handwashingreminder.appintro.utils.AnimatedResources
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.jobs.alarms.AlarmHandler
import com.javinator9889.handwashingreminder.utils.*
import kotlinx.android.synthetic.main.animated_intro.*
import timber.log.Timber
import com.javinator9889.handwashingreminder.appintro.R as RIntro


const val TIME_CONFIG_REQUEST_CODE = 16

@Keep
class IntroActivity : AppIntro2(),
    AppIntroViewPager.OnNextPageRequestedListener,
    View.OnClickListener {
    private lateinit var activitySlide: Fragment
    private lateinit var policySlide: SlidePolicyFragment
    private lateinit var timeConfigSlide: TimeConfigIntroFragment
    private var activityRecognitionPermissionGranted: Boolean = true

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SplitCompat.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(FirebaseAnalytics.getInstance(this)) {
            logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, null)
            setCurrentScreen(this@IntroActivity, "Intro", null)
        }

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

        var timeFragment: TimeConfigIntroFragment? = null
        if (savedInstanceState != null) {
            timeFragment =
                supportFragmentManager.getFragment(
                    savedInstanceState,
                    TimeConfigIntroFragment::class.simpleName!!
                ) as TimeConfigIntroFragment?
        }
        if (timeFragment == null) {
            timeConfigSlide = TimeConfigIntroFragment()
            timeConfigSlide.bgColor = Color.WHITE
        } else timeConfigSlide = timeFragment
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

        var policyConfig: SlidePolicyFragment? = null
        if (savedInstanceState != null) {
            policyConfig = supportFragmentManager.getFragment(
                savedInstanceState,
                SlidePolicyFragment::class.simpleName!!
            ) as SlidePolicyFragment?
        }
        if (policyConfig == null) {
            policySlide = SlidePolicyFragment().apply {
                title = this@IntroActivity
                    .getString(RIntro.string.privacy_policy_title)
                animatedDrawable = AnimatedResources.PRIVACY
                titleColor = Color.DKGRAY
                bgColor = Color.WHITE
            }
        } else policySlide = policyConfig
        addSlide(policySlide)

        showSkipButton(false)
        showStatusBar(true)
        backButtonVisibilityWithDone = true
        setIndicatorColor(Color.DKGRAY, Color.GRAY)
        nextButton.setOnClickListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        runCatching {
            supportFragmentManager.putFragment(
                outState,
                TimeConfigIntroFragment::class.simpleName!!,
                timeConfigSlide
            )
        }
        runCatching {
            supportFragmentManager.putFragment(
                outState,
                SlidePolicyFragment::class.simpleName!!,
                policySlide
            )
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val app = HandwashingApplication.instance
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit(commit = true) {
            timeConfigSlide.itemAdapter.adapterItems.forEach { item ->
                val time = "${item.hours}:${item.minutes}"
                when (item.id) {
                    TimeConfig.BREAKFAST_ID ->
                        putString(Preferences.BREAKFAST_TIME, time)
                    TimeConfig.LUNCH_ID ->
                        putString(Preferences.LUNCH_TIME, time)
                    TimeConfig.DINNER_ID ->
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
            if (!isAtLeast(AndroidVersion.Q))
                activityRecognitionPermissionGranted = true
            putBoolean(
                Preferences.ACTIVITY_TRACKING_ENABLED,
                activityRecognitionPermissionGranted
            )
            if (activityRecognitionPermissionGranted) {
                putStringSet(
                    Preferences.ACTIVITIES_ENABLED,
                    Preferences.DEFAULT_ACTIVITY_SET
                )
            }
            putBoolean(Preferences.APP_INIT_KEY, true)
        }
        val splitInstallManager = SplitInstallManagerFactory.create(this)
        splitInstallManager.deferredUninstall(listOf(AppIntro.MODULE_NAME))
        if (activityRecognitionPermissionGranted)
            app.activityHandler.startTrackingActivity()
        else
            app.activityHandler.disableActivityTracker()
        with(AlarmHandler(this)) {
            scheduleAllAlarms()
        }
        cacheDir.run { deleteRecursively() }
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        with(Bundle(2)) {
            putBoolean(
                "analytics_enabled",
                policySlide.firebaseAnalytics.isChecked
            )
            putBoolean(
                "performance_enabled",
                policySlide.firebasePerformance.isChecked
            )
            firebaseAnalytics.logEvent(
                FirebaseAnalytics.Event.SELECT_ITEM,
                this
            )
        }
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.TUTORIAL_COMPLETE, null
        )
        if (!policySlide.firebaseAnalytics.isChecked) {
            firebaseAnalytics.setCurrentScreen(this, null, null)
            firebaseAnalytics.setAnalyticsCollectionEnabled(false)
        }
        with(FirebasePerformance.getInstance()) {
            isPerformanceCollectionEnabled =
                policySlide.firebasePerformance.isChecked
        }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null || requestCode != TIME_CONFIG_REQUEST_CODE)
            return
        val id = data.getLongExtra("id", 0L)
        if (timeConfigSlide.isInitialized) {
            val position = data.getIntExtra("position", 0)
            val hours = data.getStringExtra("hours")
            val minutes = data.getStringExtra("minutes")
            val titleText = when(id) {
                TimeConfig.BREAKFAST_ID -> getString(RIntro.string.breakfast)
                TimeConfig.LUNCH_ID -> getString(RIntro.string.lunch)
                TimeConfig.DINNER_ID -> getString(RIntro.string.dinner)
                else -> ""
            }
            timeConfigSlide.itemAdapter[position] = TimeConfigItem(
                getString(RIntro.string.time_config_title_tpl, titleText),
                id, hours, minutes
            )
            timeConfigSlide.fastAdapter.notifyAdapterItemChanged(position)
            setSwipeLock()
        } else {
            timeConfigSlide.propertyContainer[id.toInt()] =
                TimeContainer(
                    data.getStringExtra("hours"),
                    data.getStringExtra("minutes")
                )
        }
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
            if (isAtLeast(AndroidVersion.Q)) {
                askForPermissions(
                    this,
                    Permission(
                        Manifest.permission.ACTIVITY_RECOGNITION,
                        PERMISSIONS_REQUEST_CODE
                    )
                )
            }
        if (newFragment is AnimatedAppIntro ||
            newFragment is SlidePolicyFragment
        )
            newFragment.image.playAnimation()
        super.onSlideChanged(oldFragment, newFragment)
    }

    private fun setSwipeLock() {
        var swipeLock = false
        timeConfigSlide.itemAdapter.adapterItems.forEach { item ->
            if (item.hours.isNullOrEmpty() && item.minutes.isNullOrEmpty()) {
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
                timeConfigSlide.itemAdapter.adapterItems.forEach { item ->
                    val hours = item.hours
                    val minutes = item.minutes
                    if (hours == "" || minutes == "") {
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
                (grantResults.isNotEmpty() &&
                        grantResults[0] == PERMISSION_GRANTED) ||
                        !isAtLeast(AndroidVersion.Q)
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
        try {
            val currentSlide =
                mPagerAdapter.getItem(pager.currentItem)
            val nextSlide =
                mPagerAdapter.getItem(pager.currentItem + 1)
            onSlideChanged(currentSlide, nextSlide)
            pager.goToNextSlide()
        } catch (_: ArrayIndexOutOfBoundsException) {
            Timber.e("Requested next slide illegally (not exists)")
        }
    }
}
