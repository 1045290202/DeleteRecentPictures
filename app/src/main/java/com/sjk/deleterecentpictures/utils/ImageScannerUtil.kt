package com.sjk.deleterecentpictures.utils

import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.sjk.deleterecentpictures.common.DataSource
import java.io.File
import java.util.*


object ImageScannerUtil {
    private const val TAG = "ImageScanner"
    var imagePaths: MutableList<String?>? = null
    
    //    public var firstImageThumbnail: Bitmap? = null
    private var cursor: Cursor? = null
    
    const val DATE_MODIFIED = MediaStore.Images.Media.DATE_MODIFIED
    const val DATE_ADDED = MediaStore.Images.Media.DATE_ADDED
    
    val screenshotsPath: String
        get() {
            var path = "${Environment.getExternalStorageDirectory().absolutePath}/DCIM/Screenshots/"
            val file = File(path)
            if (!file.exists()) {
                path = "${Environment.getExternalStorageDirectory().absolutePath}/Pictures/Screenshots/"
            }
            return path
        }
    
    fun init(context: Context, selection: String?, escape: Boolean = true, sortOrder: String = this.DATE_MODIFIED) {
        var realSelection = selection
        if (realSelection != null) {
            realSelection = "${MediaStore.Images.Media.DATA} like '$selection%'${if (escape) " escape '\\'" else ""}"
        }
        
        this.cursor = this.getQuery(context, realSelection, sortOrder)
        this.cursor?.moveToFirst()
    }
    
    private fun getQuery(context: Context, realSelection: String?, sortOrder: String): Cursor? {
        return context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, realSelection, null,
                "$sortOrder DESC"
        )
    }
    
    fun getNext(): String? {
        var imagePath: String? = null
        cursor?.let {
            try {
                imagePath = it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA))
                it.moveToNext()
            } catch (e: CursorIndexOutOfBoundsException) {
                imagePath = null
            }
        }
        
        return imagePath
    }
    
    fun getPrevious(): String? {
        var imagePath: String? = null
        cursor?.let {
            imagePath = it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA))
            it.moveToPrevious()
        }
        
        return imagePath
    }
    
    fun isEnd(): Boolean {
        cursor?.let {
            return it.isLast
        }
        return true
    }
    
    fun close() {
        this.cursor?.close()
        this.cursor = null
    }
    
    /**
     * 获取媒体扫描图片路径
     */
    fun searchImages(context: Context, selection: String?, escape: Boolean) {
        var sel = selection
        if (sel != null) {
            sel = "${MediaStore.Images.Media.DATA} like '$sel%'${if (escape) " escape '\\'" else ""}"
        }
        Log.d(TAG, "getImages: $sel")
        imagePaths = ArrayList()
        try {
            val numberOfPictures: Int = DataSource.getNumberOfPictures()
            
            val cursor = context.contentResolver
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            null,
                            sel,
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
                    if (i > numberOfPictures) {
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