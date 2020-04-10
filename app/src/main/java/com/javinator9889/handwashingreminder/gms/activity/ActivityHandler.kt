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
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.Task


class ActivityHandler(private val context: Context) {
    private val requestCode = 51824210
    private val tag = ActivityHandler::class.simpleName
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
        with(Intent(context, ActivityReceiver::class.java)) {
            pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, this, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    fun startTrackingActivity() {
        Log.i(tag, "Starting activity recognition")
        with(ActivityTransitionRequest(transitions)) {
            task = ActivityRecognition.getClient(context)
                .requestActivityTransitionUpdates(this, pendingIntent).apply {
                    addOnSuccessListener { activityRegistered = true }
                    addOnFailureListener { activityRegistered = false }
                }
        }
    }

    fun disableActivityTracker() {
        Log.i(tag, "Stopping activity recognition")
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
}