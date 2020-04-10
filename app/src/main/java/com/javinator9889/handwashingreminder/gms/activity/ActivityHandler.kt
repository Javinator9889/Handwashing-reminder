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
package com.javinator9889.handwashingreminder.gms.activity

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import com.javinator9889.handwashingreminder.utils.ACTIVITY_CHANNEL_ID
import com.javinator9889.handwashingreminder.utils.Preferences.Companion.ACTIVITY_TRACKING_ENABLED

class ActivityHandler(private val context: Context) : BroadcastReceiver() {
    private val requestCode = 51824210
    private val tag = "ActivityHandler"
    private val transitions: MutableList<ActivityTransition> = mutableListOf()
    private var task: Task<Void>? = null
    private val pendingIntent: PendingIntent
    private var activityRegistered = false

    init {
        val activitiesSet = setOf(
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.RUNNING,
            DetectedActivity.WALKING
        )
        addTransitions(activitiesSet, transitions)
        with(Intent(context, this::class.java)) {
            pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, this, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    fun startTrackingActivity() {
        with(ActivityTransitionRequest(transitions)) {
            task = ActivityRecognition.getClient(context)
                .requestActivityTransitionUpdates(this, pendingIntent).apply {
                    addOnSuccessListener { activityRegistered = true }
                    addOnFailureListener { activityRegistered = false }
                }
        }
    }

    private fun addTransitions(
        activitiesSet: Set<Int>,
        transitions: MutableList<ActivityTransition>,
        activityTransition: Int = ActivityTransition.ACTIVITY_TRANSITION_EXIT
    ) {
        for (activity in activitiesSet) {
            transitions += ActivityTransition.Builder()
                .setActivityType(activity)
                .setActivityTransition(activityTransition)
                .build()
        }
    }

    fun disableActivityTracker() {
        if (!activityRegistered)
            return
        task = ActivityRecognition.getClient(context)
            .removeActivityTransitionUpdates(pendingIntent).apply {
                addOnSuccessListener { pendingIntent.cancel() }
                addOnFailureListener { e: Exception ->
                    Log.e(tag, e.message, e)
                }
            }
    }

    /**
     * {@inheritDoc}
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = HandwashingApplication.getInstance()
            val sharedPreferences = app.sharedPreferences
            if (sharedPreferences.getBoolean(ACTIVITY_TRACKING_ENABLED, false))
                startTrackingActivity()
            else
                disableActivityTracker()
            return
        }
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            for (event in result.transitionEvents) {
                if (event.transitionType !=
                    ActivityTransition.ACTIVITY_TRANSITION_EXIT ||
                    event.activityType == DetectedActivity.UNKNOWN
                )
                    continue
                val notificationHandler = NotificationsHandler(
                    this.context,
                    ACTIVITY_CHANNEL_ID,
                    this.context.getString(
                        R.string
                            .activity_notification_channel_name
                    ),
                    this.context.getString(
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
                break
            }
        }
    }

}