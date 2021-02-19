package com.sjk.deleterecentpictures.utils

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.util.*


object ImageScanner {
    private const val TAG = "ImageScanner"

    /*fun getA(context: Context){
        var filepath: String?
        val mImageUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Thumbnails.DATA
        )
        //全部图片
        //全部图片
        val where = (MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=?")
        //指定格式
        //指定格式
        val whereArgs = arrayOf("image/jpeg", "image/png", "image/jpg")
        //查询
        //查询
        val mCursor: Cursor? = context.contentResolver.query(
                mImageUri, projection, null, null,
                null)
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                // 获取图片的路径
                val thumbPathIndex: Int = mCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA)
                val timeIndex: Int = mCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                val pathIndex: Int = mCursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val id: Int = mCursor.getColumnIndex(MediaStore.Images.Media._ID)
                val date: Long = mCursor.getLong(timeIndex) * 1000
                var thumbPath: String
                thumbPath = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        .buildUpon()
                        .appendPath(java.lang.String.valueOf(mCursor.getInt(id))).build().toString()
                filepath = thumbPath
            }
            mCursor.close()
        }
    }*/

    /**
     * 获取媒体扫描图片路径
     */
    fun getImages(context: Context, selection: String?, escape: Boolean): MutableList<String?>? {
        var selection = selection
        if (selection != null) {
            selection = "${MediaStore.Images.Media.DATA} like '$selection%'${if (escape) " escape '\\'" else ""}"
        }
        Log.d(TAG, "getImages: $selection")
        val imagePaths: MutableList<String?> = ArrayList()
        try {
            val cursor = context.contentResolver
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            null,
                            selection,
                            null,
                            "${MediaStore.Images.Media._ID} DESC") //MediaStore.Images.Media._ID + " DESC"
            if (cursor != null && cursor.moveToFirst()) {
                do {
//                    imagePaths.add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                        .buildUpon()
//                        .appendPath(java.lang.String.valueOf(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)))).build())
                    imagePaths.add(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)))
                } while (cursor.moveToNext())
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
//        Log.d(TAG, "getImages: $imagePaths");
        return imagePaths
    }

    val screenshotsPath: String
        get() {
            var path = Environment.getExternalStorageDirectory().path + "/DCIM/Screenshots/"
            val file = File(path)
            if (!file.exists()) {
                path = Environment.getExternalStorageDirectory().path + "/Pictures/Screenshots/"
            }
            return path
        }
}