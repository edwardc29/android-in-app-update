package com.carrion.edward.androidinappupdate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_main.*

private const val UPDATE_REQUEST_CODE = 100

class MainActivity : AppCompatActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private val listener = InstallStateUpdatedListener {
        if (it.installStatus() == InstallStatus.DOWNLOADED) {
            log("An update has been downloaded")
            showSnackBarForCompleteUpdate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(listener)

        checkUpdate()
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                if (it.installStatus() == InstallStatus.DOWNLOADED) {

                }
            } else {
                if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        it, AppUpdateType.IMMEDIATE, this, UPDATE_REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        appUpdateManager.unregisterListener(listener)
        super.onDestroy()
    }

    private fun checkUpdate() {
        Log.d("-zzzzz", "Checking for updates")
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                log("Update available")
                requestUpdate(it, AppUpdateType.FLEXIBLE)
            } else {
                log("No update available")
            }
        }
    }

    private fun requestUpdate(appUpdateInfo: AppUpdateInfo, appUpdateType: Int) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo, appUpdateType, this, UPDATE_REQUEST_CODE
        )
    }

    private fun showSnackBarForCompleteUpdate() {
        Snackbar.make(constraintLayout, "Update completed", Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.restart)) {
                appUpdateManager.completeUpdate()
                appUpdateManager.unregisterListener(listener)
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UPDATE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    log("Update installed")
                }
                Activity.RESULT_CANCELED -> {
                    log("Update canceled")
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    log("Something bad happened")
                }
            }
        }
    }

    private fun log(msg: String) {
        Log.e("-zzzzz", msg)
    }
}