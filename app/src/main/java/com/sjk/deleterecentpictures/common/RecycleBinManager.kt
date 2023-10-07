package com.sjk.deleterecentpictures.common

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import com.sjk.deleterecentpictures.bean.DeletedImageInfoBean
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.R
import kotlinx.coroutines.InternalCoroutinesApi
import java.io.File
import kotlinx.coroutines.internal.synchronized

/**
 * 回收站管理器，用于删除图片后将图片移动到回收站，并且可以从回收站恢复图片
 */
object RecycleBinManager {
    
    val recyclePath: String
        get() {
            return App.context.getExternalFilesDir("recycle")!!.absolutePath
        }
    
    var deletedImageInfo: DeletedImageInfoBean? = null
    
    /**
     * 在内部存储中创建回收站文件夹
     */
    fun createRecycleBinFolder(): Boolean {
        return App.fileUtil.createFolder(this.recyclePath)
    }
    
    fun createNoMediaFile(): Boolean {
        return App.fileUtil.createFile("${this.recyclePath}/.nomedia")
    }
    
    /**
     * 删除回收站内的旧图片
     */
    fun deleteOldImageInRecycleBin(): Boolean {
        if (this.deletedImageInfo == null) {
            return false
        }
        val deleted = App.fileUtil.deleteFile(this.deletedImageInfo!!.newFile)
        this.deletedImageInfo = null
        return deleted
    }
    
    /**
     * 清空回收站
     */
    fun clearRecycleBin(): Boolean {
        val folderPath = this.recyclePath
        val excludes = setOf(".nomedia")
        if (this.deletedImageInfo?.newFile?.name != null) {
            excludes.plus(this.deletedImageInfo!!.newFile.name)
        }
        
        val folder = File(folderPath)
        if (!folder.exists()) {
            return true
        }
        
        if (!folder.isDirectory) {
            return false
        }
        
        val files = folder.listFiles() ?: return true
        
        Thread {
            Thread.sleep(1000)
            for (file in files) {
                if (excludes.contains(file.name)) { // 不删除 .nomedia 文件和最新删除的图片，最新删除的图片单独处理是为了防止异步操作出问题
                    continue
                }
                file.delete()
            }
        }.start()
        return true
    }
    
    /**
     * 将图片移动到回收站
     */
    fun moveToRecycleBin(imageInfo: ImageInfoBean?): Boolean {
        if (imageInfo?.path == null) {
            return false
        }
        val recycleBinFolderCreated = this.createRecycleBinFolder()
        if (!recycleBinFolderCreated) {
            App.output.showToast(App.context.getString(R.string.recycle_bin_created_failed))
            return false
        }
        val noMediaFileCreated = this.createNoMediaFile()
        if (!noMediaFileCreated) {
            App.output.showToast(App.context.getString(R.string.recycle_bin_created_failed))
            return false
        }
        // 移动图片，新名称为：原名称_时间戳
        val oldFile = File(imageInfo.path)
        val newFile = File("${this.recyclePath}/${System.currentTimeMillis()}_${oldFile.name}")
        if (!App.fileUtil.existsFile(oldFile)) {
            return false
        }
        val moved = oldFile.renameTo(newFile)
        if (!moved) {
            return false
        }
        this.deletedImageInfo = DeletedImageInfoBean(oldFile, newFile, imageInfo)
        return true
    }
    
    /**
     * 从回收站恢复图片
     */
    fun recover(
        onSuccess: ((path: String, uri: Uri) -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (this.deletedImageInfo == null) {
            onFailed?.invoke()
            return
        }
        val oldFile = this.deletedImageInfo!!.oldFile
        val newFile = this.deletedImageInfo!!.newFile
        if (!App.fileUtil.existsFile(newFile)) {
            onFailed?.invoke()
            return
        }
        val moved = newFile.renameTo(oldFile)
        if (!moved) {
            onFailed?.invoke()
            return
        }
        
        // 更新媒体库
        MediaScannerConnection.scanFile(
            App.context,
            arrayOf(oldFile.absolutePath),
            null
        ) { path, uri ->
            onSuccess?.invoke(path, uri)
        }
        // this.updateMediaScan(App.context, this.deletedImageInfo!!.info, onSuccess, onFailed)
        this.deletedImageInfo = null
    }
    
    fun updateMediaScan(
        context: Context,
        imageInfo: ImageInfoBean?,
        onSuccess: ((path: String, uri: Uri) -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (imageInfo?.uri == null || imageInfo.path == null) {
            onFailed?.invoke()
            return
        }
        
        val contentResolver: ContentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DATE_MODIFIED, imageInfo.dateModified)
            put(MediaStore.Images.Media.DATE_ADDED, imageInfo.dateAdded)
        }
        
        val uri: Uri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.Files.FileColumns.DATA}=?"
        val selectionArgs = arrayOf(imageInfo.path)
        
        val updatedRows = contentResolver.update(uri, contentValues, selection, selectionArgs)
        
        if (updatedRows == 0) {
            contentValues.put(MediaStore.Files.FileColumns.DATA, imageInfo.path)
            contentResolver.insert(uri, contentValues)
        }
        onSuccess?.invoke(imageInfo.path, imageInfo.uri)
    }
    
}