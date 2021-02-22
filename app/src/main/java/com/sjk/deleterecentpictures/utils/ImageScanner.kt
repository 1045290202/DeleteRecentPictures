package com.sjk.deleterecentpictures.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.preference.PreferenceManager
import com.sjk.deleterecentpictures.common.DataSource
import java.io.File
import java.util.*


object ImageScanner {
    private const val TAG = "ImageScanner"
    public var imagePaths: MutableList<String?>? = null
//    public var firstImageThumbnail: Bitmap? = null

    val screenshotsPath: String
        get() {
            var path = Environment.getExternalStorageDirectory().path + "/DCIM/Screenshots/"
            val file = File(path)
            if (!file.exists()) {
                path = Environment.getExternalStorageDirectory().path + "/Pictures/Screenshots/"
            }
            return path
        }

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
    fun searchImages(context: Context, selection: String?, escape: Boolean) {
        var selection = selection
        if (selection != null) {
            selection = "${MediaStore.Images.Media.DATA} like '$selection%'${if (escape) " escape '\\'" else ""}"
        }
        Log.d(TAG, "getImages: $selection")
        imagePaths = ArrayList()
        try {
            val numberOfPictures: Int = DataSource.getNumberOfPictures()

            val cursor = context.contentResolver
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            null,
                            selection,
                            null,
                            "${MediaStore.Images.Media.DATE_MODIFIED} DESC") //MediaStore.Images.Media._ID + " DESC" //按照修改日期排序
            if (cursor != null && cursor.moveToFirst()) {
                var i: Long = 0
                do {
//                    imagePaths.add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                        .buildUpon()
//                        .appendPath(java.lang.String.valueOf(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)))).build())
                    val imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                    /*if (cursor.isFirst) {
                        firstImageThumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val sp = PreferenceManager.getDefaultSharedPreferences(context)
                            val thumbnailSize = sp.getInt("thumbnailSize", 512)
                            ThumbnailUtils.createImageThumbnail(
                                    File(imagePath),
                                    Size(thumbnailSize, thumbnailSize),
                                    CancellationSignal()
                            )
                        } else {
                            val imageColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                            val id = cursor.getLong(imageColumnIndex)
                            MediaStore.Images.Thumbnails.getThumbnail(context.contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                        }
                    }*/
                    imagePaths!!.add(imagePath)
                    i++
                    if (i > numberOfPictures){
                        break
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        Log.d(TAG, "getImages: $imagePaths");
    }

    fun refreshMediaLibraryByPath(context: Context, filePath: String?, type: Int, cb: (path: String, uri: Uri?) -> Unit) {
        if (filePath == null) {
            return
        }

        when (type) {
            1 -> {
                MediaScannerConnection.scanFile(context, arrayOf(filePath), null) { path: String, uri: Uri? ->
                    cb(path, uri)
                    Log.d(TAG, "媒体库更新成功！")
                }
            }
            2 -> {
                val where = "${MediaStore.Audio.Media.DATA} like \"$filePath%\""
                val i = context.contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null)
                if (i > 0) {
                    Log.d(TAG, "媒体库更新成功！")
                }
                cb(filePath, null)
            }
            else -> {
                Log.e(TAG, "查询类型错误")
            }
        }
    }
}