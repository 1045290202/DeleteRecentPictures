package com.sjk.deleterecentpictures.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.sjk.deleterecentpictures.common.App

object PermissionUtil {
    private val PERMISSIONS_STORAGE_V29 = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    /**
     * 检查是否完整授予存储权限
     */
    fun Activity.checkPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        } else {
            PERMISSIONS_STORAGE_V29.forEach {
                if (checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }
    }

    /**
     * 依据不同安卓版本申请对应存储权限
     */
    fun Activity.requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestPermissionV30()
        } else {
            requestPermissionV29()
        }
    }

    /**
     * API30+ 所有文件权限申请
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun Activity.requestPermissionV30() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = "package:${App.applicationContext.packageName}".toUri()
        startActivity(intent)
    }

    /**
     * API29- 读写存储权限申请
     */
    fun Activity.requestPermissionV29() {
        ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE_V29, 0)
    }
}