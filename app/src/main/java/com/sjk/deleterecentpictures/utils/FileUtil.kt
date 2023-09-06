package com.sjk.deleterecentpictures.utils

import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.App
import java.io.File


object FileUtil {
    
    private val gifFileFlags = arrayOf(71, 73, 70, 56, 0x3B)
    
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
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
//    fun deleteFile(filePath: String?): Boolean {
//        if (filePath == null) {
//            return false
//        }
//
//        return this.getDeleteMethod()(filePath)
//    }

    /**
     * 删除图片
     */
    fun deleteImage(imageInfo: ImageInfoBean?): Boolean {
        if (imageInfo == null) {
            return false
        }
        return App.context.contentResolver.delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "${MediaStore.Images.Media._ID} = ?",
                arrayOf(imageInfo.id.toString())
        ) > 0
    }
    
//    private fun getDeleteMethod(): (String) -> Boolean {
//        when (App.dataSource.getSP().getBoolean("useRMCommand", false)) {
//            true -> {
//                return fun(filePath: String): Boolean {
//                    return App.shellUtil.execDeleteFile(filePath)
//                }
//            }
//            false -> {
//                return fun(filePath: String): Boolean {
//                    val file = File(filePath)
//                    return if (file.isFile && file.exists()) {
//                        file.delete()
//                    } else {
//                        false
//                    }
//                }
//            }
//        }
//    }
    
//    fun isGifFile(filePath: String?): Boolean {
//        if (filePath == null) {
//            return false
//        }
//        return this.isGifFile(File(filePath))
//    }
//
//    fun isGifFile(file: File): Boolean {
//        var isGif = true
//        val inputStream = FileInputStream(file)
//        for (i in 0..4) {
//            if (i == 4) {
//                inputStream.skip(inputStream.available() - 1L)
//            }
//            if (inputStream.read() != this.gifFileFlags[i]) {
//                isGif = false
//                break
//            }
//        }
//        inputStream.close()
//        return isGif
//    }
    
    fun getSimplifiedPathInExternalStorage(completePath: String?): String? {
        if (completePath == null) {
            return null
        }
        
        val externalStorageDirectory: String = Environment.getExternalStorageDirectory().absolutePath
        if (completePath.indexOf(externalStorageDirectory) == 0) {
            return completePath.replaceFirst(externalStorageDirectory, "")
        }
        return completePath
    }
    
    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
    
}