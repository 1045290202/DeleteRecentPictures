/**
 * 已废弃
 */

package com.sjk.deleterecentpictures.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.activity.settings.SettingsActivity

object ShortcutUtil {
    fun createLauncherShortcut(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                    ?: return
            if (shortcutManager.isRequestPinShortcutSupported) {
                val intent = Intent(context, SettingsActivity::class.java)
                intent.action = Intent.ACTION_VIEW
                val pinShortcutInfo = ShortcutInfo.Builder(context, "delete-directly-shortcut")
                        .setShortLabel(context.getString(R.string.delete_the_latest_pictures_directly))
                        .setIntent(intent)
                        .build()
                val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)
                val successCallback = PendingIntent.getBroadcast(context, 0,
                        pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE)
                shortcutManager.requestPinShortcut(pinShortcutInfo,
                        successCallback.intentSender)
            }
        }
    }
}