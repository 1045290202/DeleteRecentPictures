package com.sjk.deleterecentpictures.activity.main


import android.content.pm.PackageManager
import android.os.Build
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.logW
import com.sjk.deleterecentpictures.utils.PermissionUtil.checkPermissionGranted
import com.sjk.deleterecentpictures.utils.PermissionUtil.requestPermission


/**
 * MainActivity 的权限委托，负责存储权限的申请与授权结果处理
 */
class MainPermissionDelegate(private val activity: MainActivity) {

    companion object {
        private const val TAG = "MainPermissionDelegate"
    }

    /**
     * API 30+ 情况下，是否正跳转至所有文件授权
     */
    private var jumpedForAllFilesPermission = false

    fun requestWritePermission() {
        if (!this.activity.checkPermissionGranted()) {
            MaterialAlertDialogBuilder(this.activity)
                .setTitle(this.activity.getString(R.string.permission_request_title))
                .setMessage(this.activity.getString(R.string.permission_request_hint))
                .setPositiveButton(R.string.ok) { _, _ ->
                    // API 30+ 的授权流程需要此flag
                    jumpedForAllFilesPermission =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    this.activity.requestPermission()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    App.activityManager.finishAll()
                }
                .show()
        } else {
            this.activity.refreshDelegate.refreshAll()
        }
    }

    /**
     * API 29- 授权结果处理
     */
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                App.output.showToast(this.activity.getString(R.string.storage_permission_missing))
                logW(TAG, "Storage permission not obtained")
                App.activityManager.finishAll()
                return
            }
        }

        this.activity.refreshDelegate.refreshAll()
    }

    /**
     * API 30+ 授权后（从授权页面返回时）的检查
     */
    fun checkAfterRestart() {
        if (!this.jumpedForAllFilesPermission) {
            return
        }
        if (this.activity.checkPermissionGranted()) {
            this.jumpedForAllFilesPermission = false
            this.activity.refreshDelegate.refreshAll()
        } else {
            App.output
                .showToast(this.activity.getString(R.string.not_getting_manage_external_storage_permission))
            App.activityManager.finishAll()
        }
    }
}
