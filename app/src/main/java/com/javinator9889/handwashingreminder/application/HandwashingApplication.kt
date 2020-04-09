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
package com.javinator9889.handwashingreminder.application

import android.content.Context
import android.content.SharedPreferences
import androidx.multidex.MultiDex
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.gms.ads.AdLoader
import com.javinator9889.handwashingreminder.graphics.ImageCache
import com.javinator9889.handwashingreminder.utils.IMAGE_CACHE_DIR
import com.javinator9889.handwashingreminder.utils.Preferences.Companion.NAME
import com.mikepenz.iconics.Iconics
import javinator9889.localemanager.application.BaseApplication
import javinator9889.localemanager.utils.languagesupport.LanguagesSupport.Language


class HandwashingApplication : BaseApplication() {
    var adLoader: AdLoader? = null
    lateinit var remoteConfig: FirebaseRemoteConfig
    lateinit var sharedPreferences: SharedPreferences
    lateinit var imageCacheParams: ImageCache.ImageCacheParams

    companion object {
        private lateinit var instance: HandwashingApplication

        fun getInstance(): HandwashingApplication {
            return this.instance
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
        SplitCompat.install(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedPreferences = getCustomSharedPreferences(this)!!
        Iconics.init(this)
        imageCacheParams = ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR)
        imageCacheParams.setMemCacheSizePercent(0.25f)
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder().apply {
            minimumFetchIntervalInSeconds = 3600
            fetchTimeoutInSeconds = 3
        }.build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
    }

    /**
     * Updates the application language
     * @param language a valid language code.
     * @see Language
     */
    fun setNewLocale(@Language language: String) {
        localeManager.setNewLocale(this, language)
    }

    /**
     * {@inheritDoc}
     */
    override fun getCustomSharedPreferences(base: Context): SharedPreferences? =
        base.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
