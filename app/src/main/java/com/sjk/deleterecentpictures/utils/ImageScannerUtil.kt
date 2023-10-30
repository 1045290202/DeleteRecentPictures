package com.sjk.deleterecentpictures.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.os.Environment
import android.provider.MediaStore
import com.sjk.deleterecentpictures.bean.ImageDetailBean
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.logD
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
        
        logD(TAG, "init: realSelection = $realSelection")
        try {
            this.cursor = this.getQuery(context, realSelection, sortOrder)
            this.cursor?.moveToFirst()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 查询图片
     * @param context
     * @param realSelection
     * @param sortOrder
     */
    private fun getQuery(context: Context, realSelection: String?, sortOrder: String): Cursor? {
        return this.getQuery(
            context, arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.MIME_TYPE,
            ),
            realSelection,
            sortOrder,
        )
    }
    
    /**
     * 查询图片
     * @param context
     * @param projection
     * @param realSelection
     * @param sortOrder
     */
    private fun getQuery(
        context: Context,
        projection: Array<String>,
        realSelection: String?,
        sortOrder: String
    ): Cursor? {
        return context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
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
                
                val columnIndexDateAdded: Int =
                    it.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
                val dateAdded = it.getLong(columnIndexDateAdded)
                
                val columnIndexDateModified: Int =
                    it.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val dateModified = it.getLong(columnIndexDateModified)
                
                val columnIndexMimeType: Int = it.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val mimeType = it.getString(columnIndexMimeType)
                
                ImageInfoBean(imagePath, imageUri, imageId, dateAdded, dateModified, mimeType)
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
    
    /**
     * 通过图片的uri，获取图片的详细信息
     */
    fun getImageDetails(context: Context, imageId: Long): ImageDetailBean? {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            // 路径
            MediaStore.MediaColumns.DATA,
            // 创建时间
            MediaStore.MediaColumns.DATE_ADDED,
            // 修改时间
            MediaStore.MediaColumns.DATE_MODIFIED,
            // 文件名
            MediaStore.MediaColumns.DISPLAY_NAME,
            // 媒体类型
            MediaStore.MediaColumns.MIME_TYPE,
            // 大小
            MediaStore.MediaColumns.SIZE,
            // 宽
            MediaStore.MediaColumns.WIDTH,
            // 高
            MediaStore.MediaColumns.HEIGHT,
        )
        val cursor = getQuery(
            context,
            projection,
            "${MediaStore.MediaColumns._ID} = '${imageId}'",
            MediaStore.Files.FileColumns.DATE_MODIFIED,
        )
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndexData: Int = it.getColumnIndex(MediaStore.MediaColumns.DATA)
                val imagePath = it.getString(columnIndexData)

                val columnIndexDateAdded: Int =
                    it.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
                val dateAdded = it.getLong(columnIndexDateAdded)

                val columnIndexDateModified: Int =
                    it.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val dateModified = it.getLong(columnIndexDateModified)

                val columnIndexDisplayName: Int =
                    it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val displayName = it.getString(columnIndexDisplayName)

                val columnIndexMimeType: Int = it.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val mimeType = it.getString(columnIndexMimeType)

                val columnIndexSize: Int = it.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val size = it.getLong(columnIndexSize)

                val columnIndexWidth: Int = it.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val width = it.getInt(columnIndexWidth)

                val columnIndexHeight: Int = it.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
                val height = it.getInt(columnIndexHeight)
                
                it.close()

                return ImageDetailBean(
                    imagePath,
                    dateAdded,
                    dateModified,
                    displayName,
                    mimeType,
                    size,
                    width,
                    height,
                )
            }
        }
        return null
    }
    
}