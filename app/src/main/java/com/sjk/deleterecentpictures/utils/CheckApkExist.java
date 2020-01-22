package com.sjk.deleterecentpictures.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

public class CheckApkExist {
    public static boolean checkApkExist(Context context, String ApkPackageName) {
        if (TextUtils.isEmpty(ApkPackageName)) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(ApkPackageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
