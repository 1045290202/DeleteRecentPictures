package com.sjk.deleterecentpictures.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils

object CheckApkExist {
    fun checkApkExist(context: Context?, ApkPackageName: String?): Boolean {
        return if (TextUtils.isEmpty(ApkPackageName)) {
            false
        } else try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context?.packageManager?.getApplicationInfo(ApkPackageName!!, PackageManager.MATCH_UNINSTALLED_PACKAGES)
            } else {
                context?.packageManager?.getApplicationInfo(ApkPackageName!!, PackageManager.GET_UNINSTALLED_PACKAGES)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}