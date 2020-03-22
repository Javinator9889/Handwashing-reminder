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
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.model.SliderPage
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.TimeConfig
import com.javinator9889.handwashingreminder.utils.isAtLeast
import com.javinator9889.handwashingreminder.views.activities.MainActivity
import com.javinator9889.handwashingreminder.views.activities.config.TimeConfigActivity


class IntroActivity : AppIntro2(), AdapterView.OnItemClickListener {
    private val views = HashMap<Int, View>(3)
    private lateinit var transitions: Array<String>
    private lateinit var sliderPage4: TimeConfigIntroActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        sliderPage4 = TimeConfigIntroActivity()
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        sliderPage4.height = size.y - 80
        sliderPage4.bgColor = Color.WHITE
        sliderPage4.listener = this
        sliderPage4.fromActivity = this
        addSlide(sliderPage4)

        showSkipButton(false)
        showStatusBar(true)
        backButtonVisibilityWithDone = true;
        setIndicatorColor(Color.DKGRAY, Color.GRAY);
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
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    override fun onItemClick(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
        if (view == null)
            return
        Log.d("Intro", view.toString())
        views[id.toInt()] = view
        val intent = Intent(this, TimeConfigActivity::class.java)
        val title = view.findViewById<TextView>(R.id.title)
        val image = view.findViewById<ImageView>(R.id.infoImage)
        val timeCtr = view.findViewById<ConstraintLayout>(R.id.timeCtr)
        val clockIcon = timeCtr.findViewById<ImageView>(R.id.clockIcon)
        val hours = timeCtr.findViewById<TextView>(R.id.hours)
        val ddot = timeCtr.findViewById<TextView>(R.id.ddot)
        val minutes = timeCtr.findViewById<TextView>(R.id.minutes)
        Log.d("Intro", title.toString())
        Log.d("Intro", title.text.toString())
        val options = if (isAtLeast(AndroidVersion.LOLLIPOP)) {
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair.create(title, TimeConfigActivity.VIEW_TITLE_NAME),
                Pair.create(image, TimeConfigActivity.INFO_IMAGE_NAME),
                Pair.create(clockIcon, TimeConfigActivity.USER_TIME_ICON),
                Pair.create(hours, TimeConfigActivity.USER_TIME_HOURS),
                Pair.create(ddot, TimeConfigActivity.USER_DDOT),
                Pair.create(minutes, TimeConfigActivity.USER_TIME_MINUTES)
            )
        } else {
            null
        }
        Log.d("Intro", options?.toString())
        intent.putExtra(
            "title", view.findViewById<TextView>(R.id.title).text
        )
        intent.putExtra(
            "hours", view.findViewById<TextView>(R.id.hours).text
        )
        intent.putExtra(
            "minutes", view.findViewById<TextView>(R.id.minutes).text
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
        val view = when (requestCode.toLong()) {
            TimeConfig.BREAKFAST_ID -> {
                views[TimeConfig.BREAKFAST_ID.toInt()]
            }
            TimeConfig.LUNCH_ID -> {
                views[TimeConfig.LUNCH_ID.toInt()]
            }
            TimeConfig.DINNER_ID -> {
                views[TimeConfig.DINNER_ID.toInt()]
            }
            else -> null
        }
        view?.findViewById<TextView>(R.id.hours)?.text =
            data.getStringExtra("hours")
        view?.findViewById<TextView>(R.id.minutes)?.text =
            data.getStringExtra("minutes")
    }
}