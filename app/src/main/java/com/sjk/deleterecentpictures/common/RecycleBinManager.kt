package com.sjk.deleterecentpictures.common

import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import com.sjk.deleterecentpictures.bean.DeletedImageInfoBean
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.R
import java.io.File

/**
 * 回收站管理器，用于删除图片后将图片移动到回收站，并且可以从回收站恢复图片
 */
object RecycleBinManager {
    
    val recyclePath: String
        get() {
            return "${App.context.filesDir.absolutePath}/recycle"
        }
    
    var deletedImageInfo: DeletedImageInfoBean? = null
    
    /**
     * 在内部存储中创建回收站文件夹
     */
    fun createRecycleBinFolder(): Boolean {
        return App.fileUtil.createFolder(this.recyclePath)
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
        // 移动图片，新名称为：原名称_时间戳
        val oldFile = File(imageInfo.path)
        val newFile = File("${this.recyclePath}/${System.currentTimeMillis()}_${oldFile.name}")
        if (!App.fileUtil.existsFile(oldFile)) {
            return false
        }
        if (!App.fileUtil.existsFile(oldFile.copyTo(newFile, true))) {
            return false
        }
        this.deleteOldImageInRecycleBin()
        val deleted = App.fileUtil.deleteImage(imageInfo)
        this.deletedImageInfo = DeletedImageInfoBean(oldFile, newFile)
        return deleted
    }
    
    /**
     * 从回收站恢复图片
     */
    fun recover(onSuccess: MediaScannerConnection.OnScanCompletedListener? = null, onFailed: (() -> Unit)? = null) {
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
        if (!App.fileUtil.existsFile(newFile.copyTo(oldFile, true))) {
            onFailed?.invoke()
            return
        }
        App.fileUtil.deleteFile(newFile)
        this.deletedImageInfo = null
        // 更新媒体库
        MediaScannerConnection.scanFile(App.context, arrayOf(oldFile.absolutePath), null, onSuccess)
    }
    
}