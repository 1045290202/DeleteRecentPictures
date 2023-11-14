package com.sjk.deleterecentpictures.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils

object ApkUtil {
    fun checkApkExist(context: Context?, apkPackageName: String?): Boolean {
        return if (TextUtils.isEmpty(apkPackageName)) {
            false
        } else try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context?.packageManager?.getApplicationInfo(apkPackageName!!, PackageManager.MATCH_UNINSTALLED_PACKAGES)
            } else {
                context?.packageManager?.getApplicationInfo(apkPackageName!!, PackageManager.GET_UNINSTALLED_PACKAGES)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}