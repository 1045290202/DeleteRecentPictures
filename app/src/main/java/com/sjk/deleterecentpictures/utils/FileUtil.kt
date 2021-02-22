package com.sjk.deleterecentpictures.utils

import java.io.File


object FileUtil {
    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    fun deleteFile(filePath: String?): Boolean {
        if (filePath == null) {
            return false
        }
        
        val file = File(filePath)
        return if (file.isFile && file.exists()) {
            file.delete()
        } else {
            false
        }
    }
    
    /**
     * 通知媒体扫描从数据库里面删除文件信息
     *
     * @param context  context
     * @param filepath 被删除文件的文路径
     */
    /*fun updateFileFromDatabase(context: Context, filePath: String?, cb: (path: String, uri: Uri?) -> Unit) {
        if (filePath == null) {
            return
        }

        *//*val where = "${MediaStore.Audio.Media.DATA} like \"$filePath%\""
        val i = context.contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null)
        if (i > 0) {
            Log.e("", "媒体库更新成功！")
        }*//*

        MediaScannerConnection.scanFile(context, arrayOf(filePath), null) { path: String, uri: Uri? ->
            cb(path, uri)
            Log.e("", "媒体库更新成功！")
        }
    }*/
}