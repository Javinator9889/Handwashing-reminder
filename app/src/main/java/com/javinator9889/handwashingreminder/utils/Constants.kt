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
package com.javinator9889.handwashingreminder.utils

const val TIME_CHANNEL_ID = "timeNotificationsChannel"
const val ACTIVITY_CHANNEL_ID = "activityNotificationsChannel"

class Preferences {
    companion object {
        const val NAME = "handwasingreminder:prefs"
        const val CREATE_CHANNEL_KEY = "create_channel_req"
        const val APP_INIT_KEY = "application_initialized"
        const val ADS_ENABLED = "app_admob_enabled"
        const val BREAKFAST_TIME = "app:breakfast"
        const val LUNCH_TIME = "app:lunch"
        const val DINNER_TIME = "app:dinner"
        const val ANALYTICS_ENABLED = "firebase:analytics"
        const val PERFORMANCE_ENABLED = "firebase:performance"
        const val ACTIVITY_TRACKING_ENABLED = "activity:gms:tracking"
    }
}

class TimeConfig {
    companion object {
        const val BREAKFAST_ID = 0L
        const val LUNCH_ID = 1L
        const val DINNER_ID = 2L
    }
}

class AppIntro {
    companion object Modules {
        const val MODULE_NAME = "appintro"
        const val PACKAGE_NAME =
            "com.javinator9889.handwashingreminder.appintro"
        const val MAIN_ACTIVITY_NAME = "IntroActivity"
    }
}

class Ads {
    companion object Modules {
        const val MODULE_NAME = "ads"
        const val PACKAGE_NAME = "com.javinator9889.handwashingreminder.ads"
        const val CLASS_NAME = "AdLoaderImpl"
        const val PROVIDER_NAME = "Provider"
    }
}

class RemoteConfig {
    companion object Keys {
        const val SPECIAL_EVENT = "special_event"
        const val ANIMATION_NAME = "animation_name"
        const val WORK_IN_PROGRESS = "work_in_progress_message"
    }
}

class Workers {
    companion object Keys {
        const val WHO = "workers:key_id"
        const val BREAKFAST = 0
        const val LUNCH = 1
        const val DINNER = 2
        const val BREAKFAST_UUID = "workers:breakfast"
        const val LUNCH_UUID = "workers:lunch"
        const val DINNER_UUID = "workers:dinner"
        const val UNIQUE_WORK_NAME = "workers:time-sched_job"
        const val HOUR = "worker:hour"
        const val MINUTE = "worker:minute"
    }
}

const val MODULE_COUNT = 2
const val DYNAMIC_FEATURE_INSTALL_RESULT_CODE = 32
const val IMAGE_CACHE_DIR = "images"
const val CONFIRMATION_REQUEST_CODE = 128
const val PERMISSIONS_REQUEST_CODE = 10
