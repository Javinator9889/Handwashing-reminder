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
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.tasks.Task
import com.javinator9889.handwashingreminder.BuildConfig
import com.javinator9889.handwashingreminder.utils.Preferences
import timber.log.Timber

internal const val ACTIVITY_REQUEST_CODE = 64
internal const val TRANSITIONS_RECEIVER_ACTION =
    "${BuildConfig.APPLICATION_ID}/TRANSITIONS_RECEIVER_ACTION"

class ActivityHandler private constructor(private val context: Context) {
    private val transitions: MutableList<ActivityTransition> = mutableListOf()
    private var pendingIntent: PendingIntent
    private var activityRegistered = false
    private val transitionsReceiver = ActivityReceiver()

    init {
        val activitiesSet = createSetOfTransitions()
        addTransitions(activitiesSet, transitions)
        registerActivityReceiver()
        pendingIntent = createPendingIntent()
    }

    companion object {
        private var instance: ActivityHandler? = null

        fun getInstance(context: Context): ActivityHandler {
            instance?.let { return it }
            instance = ActivityHandler(context)
            return instance!!
        }
    }

    fun startTrackingActivity() {
        if (transitions.size == 0)
            return
        Timber.d("Starting activity recognition")
        with(ActivityTransitionRequest(transitions)) {
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

    fun reload() = with(createSetOfTransitions()) {
        transitions.clear()
        addTransitions(this, transitions)
        disableActivityTracker()?.let {
            it.addOnCompleteListener {
                pendingIntent = createPendingIntent()
                startTrackingActivity()
            }
        }
    }

    private fun createSetOfTransitions(): Set<Int> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        with(mutableSetOf<Int>()) {
            preferences.getStringSet(
                Preferences.ACTIVITIES_ENABLED,
                Preferences.DEFAULT_ACTIVITY_SET
            )!!.run {
                forEach { this@with += Integer.parseInt(it) }
            }
            return this
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

    private fun createPendingIntent(): PendingIntent =
        with(Intent(TRANSITIONS_RECEIVER_ACTION)) {
            PendingIntent.getBroadcast(context, ACTIVITY_REQUEST_CODE, this, 0)
        }

    private fun registerActivityReceiver() =
        LocalBroadcastManager.getInstance(context).registerReceiver(
            transitionsReceiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION)
        )
}