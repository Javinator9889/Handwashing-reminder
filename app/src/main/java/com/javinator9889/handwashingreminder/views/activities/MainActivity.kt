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
 * Created by Javinator9889 on 15/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.views.activities

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.base.SplitCompatBaseActivity
import com.javinator9889.handwashingreminder.utils.isAtLeast
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : SplitCompatBaseActivity() {
    private lateinit var app: HandwashingApplication

    //    private lateinit var button: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var pgThread: Thread

    /*private lateinit var fusedLocationProviderClient:
            FusedLocationProviderClient
    private lateinit var hours: EditText
    private lateinit var minutes: EditText
    private lateinit var container: ConstraintLayout*/
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val isInstalled =
            splitInstallManager.installedModules.contains("appintro")
        if (isInstalled)
            splitInstallManager.deferredUninstall(listOf("appintro"))
                .addOnCompleteListener {
                    Toast.makeText(
                        this, "AppIntro module uninstalled", Toast
                            .LENGTH_LONG
                    ).show()
                    Log.i("MainActivity", "AppIntro module uninstalled")
                }
        val app = HandwashingApplication.getInstance()
        val frameView = findViewById<FrameLayout>(R.id.ad_container)
        frameView.removeAllViews()
        if (app.adLoader != null) {
            frameView.addView(app.adLoader!!.adView)
            app.adLoader!!.loadAd()
        }

        button.setOnClickListener {
            frameView.removeAllViews()
            if (app.adLoader != null) {
                frameView.addView(app.adLoader!!.adView)
                app.adLoader!!.loadAd()
            }
        }
//        app.adLoader?.

//        app = HandwashingApplication.getInstance()

//        val dataset = arrayOf("Desayuno", "Comida", "Cena")
        /*val dataset = arrayOf(
            TimeConfigContent(
                "Desayuno",
                0L
            ),
            TimeConfigContent(
                "Comida",
                1L
            ),
            TimeConfigContent(
                "Cena",
                2L
            )
        )
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        viewManager = LinearLayoutManager(this)
        viewAdapter =
            TimeConfigAdapter(
                dataset,
                null,
                this,
                null
            )

        recyclerView = findViewById<RecyclerView>(R.id.cardsView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }*/
//        hours = findViewById(R.id.hours)
//        minutes = findViewById(R.id.minutes)
//        container = findViewById(R.id.timeCtr)
//
//        hours.setOnClickListener(this)
//        minutes.setOnClickListener(this)
//        container.setOnClickListener(this)
        /*button = findViewById(R.id.button)
        progressBar = findViewById(R.id.progressBar)
        button.setOnTouchListener(this)
        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(this)
        val permissionCheck = PermissionChecker
            .checkCallingOrSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) or PermissionChecker.checkCallingOrSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissionCheck != PERMISSION_GRANTED &&
            isAtLeast(AndroidVersion.M)
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1000
            )
        }
        val loc = LocationService()
        loc.lat
        loc.lon*/
//        button.setOnClickListener(this)
    }

    private fun runProgressBar() {
        pgThread = thread(name = "pgthread") {
            try {
                for (i in progressBar.progress until 100) {
                    this@MainActivity.runOnUiThread {
                        progressBar.incrementProgressBy(1)
                    }
                    Thread.sleep(1000L)
                }
            } catch (_: InterruptedException) {
                Log.i("Main", "Interrupted")
            }
        }
        Log.i("Main", "${pgThread.isInterrupted} - ${pgThread.isAlive}")
        if (pgThread.isInterrupted || !pgThread.isAlive) {
            pgThread.start()
        } else {
            pgThread.interrupt()
        }
    }

    /*override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (view) {
            button -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Log.i("Main", "Button clicked")
                        runProgressBar()
                    }
                    MotionEvent.ACTION_UP -> {
                        Log.i("Main", "Creating notification")
                        view.performClick()
                        val notificationHandler = NotificationsHandler(
                            this,
                            ACTIVITY_CHANNEL_ID,
                            getString(
                                R.string
                                    .activity_notification_channel_name
                            ),
                            getString(
                                R.string
                                    .activity_notification_channel_desc
                            )
                        )
                        notificationHandler.createNotification(
                            R.drawable.ic_handwashing_icon,
                            R.drawable.handwashing_app_logo,
                            R.string.test_notification,
                            R.string.test_content
                        )
                    }
                }
            }
            else -> Log.e("Main", "Unexpected click: $view")
        }
        return view?.onTouchEvent(event) ?: true
    }*/

    private fun createNotificationChannel() {
        if (isAtLeast(AndroidVersion.O)) {
            val name = "Test notifications"
            val descriptionText = "Channel for test notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("tch", name, importance)
                .apply { description = descriptionText }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as
                        NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /*override fun onClick(v: View?) {
        when (v) {
            hours, minutes, container -> {
                Log.i("Main", "Changing hours / minutes")
                val cTime = Calendar.getInstance()
                val cHour = cTime.get(Calendar.HOUR_OF_DAY)
                val cMinute = cTime.get(Calendar.MINUTE)
                val timePicker = TimePickerDialog(this, this, cHour, cMinute,
                    true)
                timePicker.setTitle("Select an hour")
                timePicker.show()
            }
            else -> {
                Log.e("Main", "Unexpected view $v")
            }
        }
    }*/

    /*override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        hours.setText(hourOfDay.toString())
        minutes.setText(minute.toString())
    }*/
}
