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
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.Task
import timber.log.Timber

internal const val ACTIVITY_REQUEST_CODE = 64
//internal const val TRANSITIONS_RECEIVER_ACTION =
//    "${BuildConfig.APPLICATION_ID}.TRANSITIONS_RECEIVER_ACTION"
internal val TRANSITIONS = listOf<ActivityTransition>(
    ActivityTransition.Builder()
        .setActivityType(DetectedActivity.IN_VEHICLE)
        .setActivityTransition(ACTIVITY_TRANSITION_EXIT)
        .build(),
    ActivityTransition.Builder()
        .setActivityType(DetectedActivity.ON_BICYCLE)
        .setActivityTransition(ACTIVITY_TRANSITION_EXIT)
        .build(),
    ActivityTransition.Builder()
        .setActivityType(DetectedActivity.RUNNING)
        .setActivityTransition(ACTIVITY_TRANSITION_EXIT)
        .build(),
    ActivityTransition.Builder()
        .setActivityType(DetectedActivity.WALKING)
        .setActivityTransition(ACTIVITY_TRANSITION_EXIT)
        .build()
)

class ActivityHandler private constructor(private val context: Context) {
    private var pendingIntent: PendingIntent = createPendingIntent()
    private var activityRegistered = false

    companion object {
        private var instance: ActivityHandler? = null

        fun getInstance(context: Context): ActivityHandler {
            instance?.let { return it }
            synchronized(this) {
                val instance = ActivityHandler(context.applicationContext)
                this.instance = instance
                return instance
            }
        }
    }

    fun startTrackingActivity() {
        Timber.d("Starting activity recognition")
        with(ActivityTransitionRequest(TRANSITIONS)) {
            ActivityRecognition.getClient(context)
                .requestActivityTransitionUpdates(this, pendingIntent).apply {
                    addOnSuccessListener { activityRegistered = true }
                    addOnFailureListener { activityRegistered = false }
                }
        }
    }

    fun disableActivityTracker(): Task<Void>? {
        Timber.d("Stopping activity recognition")
        if (!activityRegistered)
            return null
        return ActivityRecognition.getClient(context)
            .removeActivityTransitionUpdates(pendingIntent).also {
                it.addOnSuccessListener {
                    pendingIntent.cancel(); activityRegistered = false
                }
                it.addOnFailureListener { e: Exception -> Timber.e(e) }
            }
    }

    private fun createPendingIntent(): PendingIntent =
        with(Intent(context, ActivityReceiver::class.java)) {
//            action = TRANSITIONS_RECEIVER_ACTION
            PendingIntent.getBroadcast(
                context,
                ACTIVITY_REQUEST_CODE,
                this,
                FLAG_UPDATE_CURRENT
            )
        }
}