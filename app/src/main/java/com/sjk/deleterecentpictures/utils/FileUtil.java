package com.sjk.deleterecentpictures.utils;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

public class FileUtil {
    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    /**
     * 通知媒体扫描从数据库里面删除文件信息
     *
     * @param context  context
     * @param filepath 被删除文件的文路径
     */
    public static void updateFileFromDatabase(Context context, String filepath) {
        String where = MediaStore.Audio.Media.DATA + " like \"" + filepath + "%" + "\"";
        int i = context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
        if (i > 0) {
            Log.e("", "媒体库更新成功！");
        }
    }
}
