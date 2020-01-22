package com.sjk.deleterecentpictures.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ImageScanner {
    /**
     * 获取媒体扫描图片路径
     */
    public static List<String> getImages(Context context, String selection) {
        if (selection != null) {
            selection = MediaStore.Images.Media.DATA + " like '" + selection + "%'";
        }
//        Log.d(TAG, "getImages: " + screenshotsPath);
        
        List<String> imagePaths = new ArrayList<>();
        Cursor cursor = context.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        selection,
                        null,
                        MediaStore.Images.Media._ID + " DESC");//MediaStore.Images.Media._ID + " DESC"
        if (cursor != null && cursor.moveToFirst()) {
            do {
                imagePaths.add(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
            } while (cursor.moveToNext());
            cursor.close();
        }
//        Log.d(TAG, "getImages: " + imagePaths);
        return imagePaths;
    }
    
    public static String getScreenshotsPath() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Screenshots/";
        File file = new File(path);
        if (!file.exists()) {
            path = Environment.getExternalStorageDirectory().getPath() + "/Pictures/Screenshots/";
        }
        return path;
    }
}
