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

import com.google.android.gms.location.DetectedActivity

const val TIME_CHANNEL_ID = "timeNotificationsChannel"
const val ACTIVITY_CHANNEL_ID = "activityNotificationsChannel"

class Preferences {
    companion object {
        const val CREATE_CHANNEL_KEY = "notifications:channel:create"
        const val APP_INIT_KEY = "app:initialized"
        const val ADS_ENABLED = "app:ads:enabled"
        const val BREAKFAST_TIME = "app:breakfast"
        const val LUNCH_TIME = "app:lunch"
        const val DINNER_TIME = "app:dinner"
        const val ANALYTICS_ENABLED = "firebase:analytics"
        const val PERFORMANCE_ENABLED = "firebase:performance"
        const val ACTIVITY_TRACKING_ENABLED = "activity:gms:tracking"
        const val ACTIVITIES_ENABLED = "activity:gms:activities:enabled"
        val DEFAULT_ACTIVITY_SET = setOf(
            DetectedActivity.IN_VEHICLE.toString(),
            DetectedActivity.ON_BICYCLE.toString(),
            DetectedActivity.RUNNING.toString(),
            DetectedActivity.WALKING.toString()
        )
        const val DONATIONS = "donations"
        const val INITIAL_TUTORIAL_DONE = "app:tutorial:is_done"
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

class BundledEmoji {
    companion object Modules {
        const val MODULE_NAME = "bundledemoji"
        const val PACKAGE_NAME = "com.javinator9889.handwashingreminder.bundledemoji"
        const val CLASS_NAME = "BundledEmojiConfig"
    }
}

class OkHttp {
    companion object Modules {
        const val MODULE_NAME = "okhttp"
        const val PACKAGE_NAME = "com.javinator9889.handwashingreminder.okhttp"
        const val CLASS_NAME = "OkHttpDownloader"
        const val PROVIDER_NAME = "Provider"
    }
}

class OkHttpLegacy {
    companion object Modules {
        const val MODULE_NAME = "okhttplegacy"
        const val PACKAGE_NAME = "com.javinator9889.handwashingreminder.okhttplegacy"
        const val CLASS_NAME = "OkHttpDownloader"
        const val PROVIDER_NAME = "Provider"
    }
}

class RemoteConfig {
    companion object Keys {
        const val SPECIAL_EVENT = "special_event"
        const val ANIMATION_NAME = "animation_name"
        const val WORK_IN_PROGRESS = "work_in_progress_message"
        const val DISEASES_JSON = "diseases"
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
        const val HOUR = "worker:hour"
        const val MINUTE = "worker:minute"
    }
}

class Videos {
    companion object URI {
        const val URL = "video:url"
        const val HASH = "video:hash"
        const val FILENAME = "video:name"
        val VideoList = arrayOf(
            hashMapOf(
                URL to "https://cloud.javinator9889.com/s/j4tAXFpNmcZ7WRG/download",
                HASH to "4bab6874bf8b091accaaf01b07cbc16cbba661411b4dbdb4b2a9f4c033ce8889",
                FILENAME to "first-step.mp4"
            ),
            hashMapOf(
                URL to "https://cloud.javinator9889.com/s/XRDj6LBwb92mSNc/download",
                HASH to "dc42473a8150d1f258d26e22d455500dcef9ee3c13122e183d712f8b5b8ecf0d",
                FILENAME to "second-step.mp4"
            ),
            hashMapOf(
                URL to "https://cloud.javinator9889.com/s/bQNtM9LPsfLYqjy/download",
                HASH to "d8c83722a72466780ba00a6df3cf5fec30f662d538041c49e5cd1f6228d9a009",
                FILENAME to "third-step.mp4"
            ),
            hashMapOf(
                URL to "https://cloud.javinator9889.com/s/3G4npX58tkqXxx9/download",
                HASH to "2e1ce76a465be9a0c4de53d607d4a6285c393094e88d63673571ab9355e09825",
                FILENAME to "fourth-step.mp4"
            ),
            hashMapOf(
                URL to "https://cloud.javinator9889.com/s/D9CPyBWQ5MbEsbE/download",
                HASH to "e2b8893019af37a59b7dc3f9dfdf1788d781498ed8c67fa50997d26cc3beb27b",
                FILENAME to "fifth-step.mp4"
            ),
            hashMapOf(
                URL to "https://cloud.javinator9889.com/s/NZ23LBigk5pAXqR/download",
                HASH to "b8831875e52841dd9d1bb3bb575fc1759cf69f93af868c3bb9098d7483be30f9",
                FILENAME to "sixth-step.mp4"
            ),
            hashMapOf(
                URL to "https://cloud.javinator9889.com/s/zMRz6nWqi6rsNen/download",
                HASH to "d414da0b1cdc818bea8028b7ac1f4e9702fcad6546cae83575b3dd908caa0f3d",
                FILENAME to "seventh-step.mp4"
            )
        )
    }
}

object Email {
    const val TO = "contact@javinator9889.com"
    const val SUBJECT = "Handwashing reminder | Suggestions"
}

object Firebase {
    object Properties {
        const val LANGUAGE = "language"
    }
}

const val TRANSLATE_URL = "https://s.javinator9889.com/hwtranslate"
const val PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=com.javinator9889.handwashingreminder"
const val TELEGRAM_URL = "https://t.me/handwash"
const val TWITTER_URL = "https://twitter.com/javinator9889"
const val LINKEDIN_URL = "https://www.linkedin.com/in/javinator9889"
const val GITHUB_URL = "https://github.com/Javinator9889/Handwashing-reminder"

const val MODULE_COUNT = 3
const val DYNAMIC_FEATURE_INSTALL_RESULT_CODE = 32
const val CONFIRMATION_REQUEST_CODE = 128
const val PERMISSIONS_REQUEST_CODE = 10

const val GOOGLE_PLAY_SERVICES_MIN_VERSION = 1100000
