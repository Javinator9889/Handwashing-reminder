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
 * Created by Javinator9889 on 2/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Toast
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.javinator9889.handwashingreminder.BuildConfig
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.SplitCompatBaseActivity
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.isAtLeast
import kotlinx.android.synthetic.main.dynamic_content_pb.*

class DynamicFeatureProgress : SplitCompatBaseActivity(),
    SplitInstallStateUpdatedListener {
    companion object {
        const val MODULES = "modules"
        const val LAUNCH_ON_INSTALL = "modules:launch_on_install"
        const val PACKAGE_NAME = "modules:package_name"
        const val CLASS_NAME = "modules:class_name"
    }

    private lateinit var module: String
    private lateinit var launchActivityName: String
    private var launchOnInstall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splitInstallManager.registerListener(this)
        module =
            intent.getStringExtra(MODULES) ?: throw NullPointerException(
                "An array of modules must be provided for " +
                        "instantiating this class"
            )
        launchOnInstall = intent.getBooleanExtra(LAUNCH_ON_INSTALL, false)
        if (launchOnInstall) {
            val packageName = intent.getStringExtra(PACKAGE_NAME)
            val className = intent.getStringExtra(CLASS_NAME)
            launchActivityName = "$packageName.$className"
        }
        if (!splitInstallManager.installedModules.contains(module)) {
            setContentView(R.layout.dynamic_content_pb)
            val installRequest = SplitInstallRequest.newBuilder()
                .addModule(module)
                .build()
            splitInstallManager.startInstall(installRequest)
        } else {
            if (launchOnInstall)
                launchActivity()
        }
    }

    override fun finish() {
        splitInstallManager.unregisterListener(this)
        super.finish()
    }

    @SuppressLint("SetTextI18n")
    override fun onStateUpdate(state: SplitInstallSessionState?) {
        when (state?.status()) {
            SplitInstallSessionStatus.FAILED -> {
                Toast.makeText(
                    this, getString(
                        R.string
                            .dynamic_module_loading_error
                    ), Toast.LENGTH_LONG
                ).show()
            }
            SplitInstallSessionStatus.PENDING ->
                install_progress.isIndeterminate = true
            SplitInstallSessionStatus.DOWNLOADING -> {
                val downloadedBytes =
                    Formatter.formatFileSize(this, state.bytesDownloaded)
                val bytesToDownload =
                    Formatter.formatFileSize(this, state.totalBytesToDownload)
                bytesInfo.text = "$downloadedBytes / $bytesToDownload"
                install_progress.isIndeterminate = false
                install_progress.max = state.totalBytesToDownload.toInt()
                val progress = state.bytesDownloaded.toInt()
                if (isAtLeast(AndroidVersion.N))
                    install_progress.setProgress(progress, true)
                else
                    install_progress.progress = progress
                percentage.text = (
                        (state.bytesDownloaded / state.totalBytesToDownload)
                                * 100
                        ).toString()
            }
            SplitInstallSessionStatus.INSTALLING -> {
                install_progress.isIndeterminate = true
                bytesInfo.text = ""
                percentage.text = getString(R.string.installing)
            }
            SplitInstallSessionStatus.INSTALLED -> {
                dynamic_content_title.text = getString(R.string.done)
                if (launchOnInstall)
                    launchActivity()
            }
            else -> return
        }
    }

    private fun launchActivity() {
        Intent().setClassName(BuildConfig.APPLICATION_ID, launchActivityName)
            .also {
                startActivity(it)
                overridePendingTransition(
                    android.R.anim.fade_in, android.R.anim.fade_out
                )
                finish()
            }
    }
}