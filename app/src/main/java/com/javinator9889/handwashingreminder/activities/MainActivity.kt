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
package com.javinator9889.handwashingreminder.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.forEach
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.SplitCompatBaseActivity
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.gms.ads.AdsEnabler
import com.javinator9889.handwashingreminder.notifications.NotificationsHandler
import com.javinator9889.handwashingreminder.utils.Ads
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : SplitCompatBaseActivity() {
    private lateinit var app: HandwashingApplication

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = HandwashingApplication.getInstance()
        delegateMenuIcons(menu)
        app.adLoader?.loadAdForViewGroup(ad_container)

        button.setOnClickListener {
            app.adLoader?.loadAdForViewGroup(ad_container)
            val notificationsHandler =
                NotificationsHandler(
                    this,
                    "test_notification_channel",
                    "Test notification channel",
                    "A channel for test notifications"
                )
            notificationsHandler.createNotification(
                R.drawable.ic_stat_handwashing,
                R.drawable.handwashing_app_logo,
                "Test notification",
                "Test description with new icon"
            )
        }
        ads_remove.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                ads_remove.isEnabled = false
                ads_remove.text = "Ads will be uninstalled"
                splitInstallManager.deferredUninstall(mutableListOf(Ads.MODULE_NAME))
                val adsRemover =
                    AdsEnabler(HandwashingApplication.getInstance())
                adsRemover.disableAds()
            }
        }
    }

    protected fun delegateMenuIcons(menu: BottomNavigationView) {
        thread(start = true) {
            menu.menu.forEach { item ->
                when (item.itemId) {
                    R.id.diseases ->
                        IconicsDrawable(
                            this, GoogleMaterial.Icon
                                .gmd_feedback
                        ).apply { item.icon = this }
                    R.id.news ->
                        IconicsDrawable(
                            this, GoogleMaterial.Icon
                                .gmd_chrome_reader_mode
                        ).apply { item.icon = this }
                    R.id.settings ->
                        IconicsDrawable(
                            this, GoogleMaterial.Icon
                                .gmd_settings
                        ).apply { item.icon = this }
                }
            }
        }
    }

    override fun onDestroy() {
        app.adLoader?.destroy()
        super.onDestroy()
    }
}
