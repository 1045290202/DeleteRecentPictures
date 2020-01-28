package com.sjk.deleterecentpictures.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;

import com.sjk.deleterecentpictures.SettingsActivity2;

public class ShortcutUtil {
    public static void createLauncherShortcut(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            if (shortcutManager == null) {
                return;
            }
            if (shortcutManager.isRequestPinShortcutSupported()) {
                Intent intent = new Intent(context, SettingsActivity2.class);
                intent.setAction(Intent.ACTION_VIEW);
                
                ShortcutInfo pinShortcutInfo =
                        new ShortcutInfo.Builder(context, "delete-directly-shortcut")
                                .setShortLabel("直接删除最新图片")
                                .setIntent(intent)
                                .build();
                
                Intent pinnedShortcutCallbackIntent =
                        shortcutManager.createShortcutResultIntent(pinShortcutInfo);
                
                PendingIntent successCallback = PendingIntent.getBroadcast(context, 0,
                        pinnedShortcutCallbackIntent, 0);
                
                shortcutManager.requestPinShortcut(pinShortcutInfo,
                        successCallback.getIntentSender());
            }
        }
    }
}
