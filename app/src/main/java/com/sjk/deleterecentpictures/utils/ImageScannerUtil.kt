package com.sjk.deleterecentpictures.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.DataSource
import java.io.File


object ImageScannerUtil {
    private const val TAG = "ImageScannerUtil"
    
    private var cursor: Cursor? = null
    
    const val DATE_MODIFIED = MediaStore.Files.FileColumns.DATE_MODIFIED
    const val DATE_ADDED = MediaStore.Files.FileColumns.DATE_ADDED
    
    val screenshotsPath: String
        get() {
            var path = "${Environment.getExternalStorageDirectory().absolutePath}/DCIM/Screenshots/"
            val file = File(path)
            if (!file.exists()) {
                path =
                    "${Environment.getExternalStorageDirectory().absolutePath}/Pictures/Screenshots/"
            }
            return path
        }
    
    fun init(
        context: Context,
        selection: String?,
        escape: Boolean = true,
        sortOrder: String = this.DATE_MODIFIED
    ) {
        var realSelection = selection
        if (realSelection != null) {
            realSelection = """
                ${MediaStore.Files.FileColumns.DATA} like '$selection%'${
                if (escape) {
                    " escape '\\'"
                } else {
                    ""
                }
            }
            """.trimIndent()
        }
        
        this.cursor = this.getQuery(context, realSelection, sortOrder)
        this.cursor?.moveToFirst()
    }
    
    private fun getQuery(context: Context, realSelection: String?, sortOrder: String): Cursor? {
        return context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_MODIFIED,
                // MediaStore.MediaColumns.DISPLAY_NAME,
                // MediaStore.MediaColumns.MIME_TYPE
            ),
            realSelection,
            null, // selectionArgs,
            "$sortOrder DESC",
        )
    }
    
    fun getCurrent(): ImageInfoBean? {
        var imageInfo: ImageInfoBean? = null
        cursor?.let {
            imageInfo = try {
                val columnIndexData: Int = it.getColumnIndex(MediaStore.MediaColumns.DATA)
                val imagePath = it.getString(columnIndexData)
                
                val columnIndexId: Int = it.getColumnIndex(MediaStore.MediaColumns._ID)
                val imageId: Long = it.getLong(columnIndexId)
                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"),
                    imageId,
                )
                
                val columnIndexDateAdded: Int = it.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
                val dateAdded = it.getLong(columnIndexDateAdded)
                
                val columnIndexDateModified: Int = it.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val dateModified = it.getLong(columnIndexDateModified)
                
                ImageInfoBean(imagePath, imageUri, imageId, dateAdded, dateModified)
            } catch (e: CursorIndexOutOfBoundsException) {
                null
            }
        }
        
        return imageInfo
    }
    
    fun getNext(): ImageInfoBean? {
        cursor?.let {
            if (it.isLast) {
                return null
            }
            it.moveToNext()
        }
        return getCurrent()
    }
    
    fun getPrevious(): ImageInfoBean? {
        cursor?.let {
            if (it.isFirst) {
                return null
            }
            it.moveToPrevious()
        }
        return getCurrent()
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
}