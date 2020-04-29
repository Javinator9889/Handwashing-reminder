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
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Toast
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.errorCode
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.splitinstall.SplitInstallHelper
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.google.firebase.analytics.FirebaseAnalytics
import com.javinator9889.handwashingreminder.BuildConfig
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.activities.base.SplitCompatBaseActivity
import com.javinator9889.handwashingreminder.utils.AndroidVersion
import com.javinator9889.handwashingreminder.utils.CONFIRMATION_REQUEST_CODE
import com.javinator9889.handwashingreminder.utils.filterNotEmpty
import com.javinator9889.handwashingreminder.utils.isAtLeast
import kotlinx.android.synthetic.main.dynamic_content_pb.*
import timber.log.Timber

class DynamicFeatureProgress : SplitCompatBaseActivity(),
    SplitInstallStateUpdatedListener {
    companion object {
        const val MODULES = "modules"
        const val LAUNCH_ON_INSTALL = "modules:launch_on_install"
        const val PACKAGE_NAME = "modules:package_name"
        const val CLASS_NAME = "modules:class_name"
    }

    private var moduleCount = 0
    private var currentModule = 1
    private var launchOnInstall = false
    private lateinit var modules: Array<String>
    private lateinit var launchActivityName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splitInstallManager.registerListener(this)
        with(FirebaseAnalytics.getInstance(this)) {
            setCurrentScreen(
                this@DynamicFeatureProgress, "Dynamic module", null
            )
        }
        modules =
            intent.getStringArrayExtra(MODULES) ?: throw NullPointerException(
                "An array of modules must be provided for " +
                        "instantiating this class"
            )
        launchOnInstall = intent.getBooleanExtra(LAUNCH_ON_INSTALL, false)
        if (launchOnInstall) {
            val packageName = intent.getStringExtra(PACKAGE_NAME)
            val className = intent.getStringExtra(CLASS_NAME)
            launchActivityName = "$packageName.$className"
        }
        val installRequestBuilder = SplitInstallRequest.newBuilder()
        modules.filterNotEmpty().forEach { module ->
            if (module !in splitInstallManager.installedModules) {
                installRequestBuilder.addModule(module)
                moduleCount++
            }
        }
        if (moduleCount > 0 && modules.isNotEmpty()) {
            setContentView(R.layout.dynamic_content_pb)
            overridePendingTransition(android.R.anim.fade_in, 0)
            val installRequest = installRequestBuilder.build()
            splitInstallManager.startInstall(installRequest)
        } else {
            setResultWithIntent(Activity.RESULT_OK)
            finish()
        }
    }

    private fun setResultWithIntent(resultCode: Int) {
        val intent = if (launchOnInstall)
            Intent().setClassName(
                BuildConfig.APPLICATION_ID,
                launchActivityName
            )
        else
            null
        setResult(resultCode, intent)
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
                        R.string.dynamic_module_loading_error, state.errorCode),
                    Toast.LENGTH_LONG
                ).show()
                Timber.e(
                    "Installation failed - error code: ${state.errorCode}"
                )
            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                splitInstallManager
                    .startConfirmationDialogForResult(
                        state,
                        this,
                        CONFIRMATION_REQUEST_CODE
                    )
            }
            SplitInstallSessionStatus.CANCELED -> {
                setResultWithIntent(Activity.RESULT_CANCELED)
                finish()
            }
            SplitInstallSessionStatus.PENDING -> {
                install_progress.isIndeterminate = true
                percentage.text = getString(R.string.preparing)
            }
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
                val currentPercentage =
                    (state.bytesDownloaded * 100 / state.totalBytesToDownload)
                        .toInt()
                percentage.text = "$currentPercentage %"
            }
            SplitInstallSessionStatus.INSTALLING -> {
                install_progress.isIndeterminate = true
                bytesInfo.text = ""
                percentage.text = getString(R.string.installing)
            }
            SplitInstallSessionStatus.INSTALLED -> {
                SplitInstallHelper.updateAppInfo(this)
                if (++currentModule >= moduleCount) {
                    dynamic_content_title.text = getString(R.string.done)
                    setResultWithIntent(Activity.RESULT_OK)
                    finish()
                }
            }
            else -> return
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONFIRMATION_REQUEST_CODE) {
            setResultWithIntent(Activity.RESULT_CANCELED)
            finish()
        }
    }
}