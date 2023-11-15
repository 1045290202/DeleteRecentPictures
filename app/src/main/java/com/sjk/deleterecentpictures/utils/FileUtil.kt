package com.sjk.deleterecentpictures.utils

import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.App
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException


object FileUtil {
    
    fun existsFile(filePath: String?): Boolean {
        if (filePath == null) {
            return false
        }
        
        return this.existsFile(File(filePath))
    }
    
    fun existsFile(file: File): Boolean {
        return file.isFile && file.exists()
    }
    
    /**
     * 删除图片
     */
    fun deleteImage(imageInfo: ImageInfoBean?): Boolean {
        if (imageInfo?.id == null) {
            return false
        }
        return App.applicationContext.contentResolver.delete(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "${MediaStore.Images.Media._ID} = ?",
            arrayOf(imageInfo.id.toString())
        ) > 0
    }
    
    /**
     * 删除文件，不允许删除文件夹
     */
    fun deleteFile(file: File?): Boolean {
        if (file == null) {
            return false
        }
        
        if (!file.exists()) {
            return true
        }
        
        if (!file.isFile) {
            return false
        }
        
        return file.delete()
    }
    
    fun getSimplifiedPathInExternalStorage(completePath: String?): String? {
        if (completePath == null) {
            return null
        }
        
        val externalStorageDirectory: String =
            Environment.getExternalStorageDirectory().absolutePath
        if (completePath.indexOf(externalStorageDirectory) == 0) {
            return completePath.replaceFirst(externalStorageDirectory, "")
        }
        return completePath
    }
    
    fun getFileNameByPath(completePath: String?): String? {
        if (completePath == null) {
            return null
        }
        
        return File(completePath).name
    }
    
    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
    
    fun clearFolder(folderPath: String?, excludes: Set<String>? = null): Boolean {
        
        if (folderPath == null) {
            return false
        }
        
        val folder = File(folderPath)
        if (!folder.exists()) {
            return true
        }
        
        if (!folder.isDirectory) {
            return false
        }
        
        val files = folder.listFiles() ?: return true
        
        for (file in files) {
            if (excludes != null && excludes.contains(file.name)) {
                continue
            }
            if (file.isDirectory) {
                this.clearFolder(file.absolutePath)
            } else {
                file.delete()
            }
        }
        return true
    }
    
    /**
     * 创建文件夹
     */
    fun createFolder(folderPath: String?): Boolean {
        if (folderPath == null) {
            return false
        }
        
        val folder = File(folderPath)
        if (folder.exists() && !folder.isDirectory) {
            folder.delete()
        }
        return if (folder.exists()) {
            true
        } else {
            folder.mkdirs()
        }
    }
    
    /**
     * 创建文件
     */
    fun createFile(filePath: String?): Boolean {
        if (filePath == null) {
            return false
        }
        
        val file = File(filePath)
        if (file.exists() && file.isDirectory) {
            file.delete()
        }
        return if (file.exists()) {
            true
        } else {
            try {
                file.createNewFile()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    fun clearCacheFolder(): Boolean {
        return this.clearFolder(App.applicationContext.cacheDir.absolutePath)
    }
    
    /**
     * 移动文件
     */
    fun moveFile(srcFile: File, destFile: File): Boolean {
        return try {
            FileUtils.moveFile(srcFile, destFile)
            true
        } catch (e: IOException) {
            false
        }
    }
    
}