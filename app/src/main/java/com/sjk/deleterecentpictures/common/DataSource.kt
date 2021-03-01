package com.sjk.deleterecentpictures.common

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceManager
import com.sjk.deleterecentpictures.R

object DataSource {
    
    val context: Context
        get() {
            return App.context
        }
    
    fun getSP(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(this.context)
    }
    
    fun getNumberOfPictures(): Int {
        val str = this.getSP().getString("numberOfPictures", App.const.DEFAULT_NUMBER_OF_PICTURES.toString())
        var numberOfPictures: Int = if (str == null || str == "") App.const.DEFAULT_NUMBER_OF_PICTURES else str.toInt()
        if (numberOfPictures == 0) {
            numberOfPictures = Const.DEFAULT_NUMBER_OF_PICTURES
        }
        return numberOfPictures
    }
    
    fun getSelection(): String? {
        var selection: String? = null
        context.let {
            val strings = it.resources.getStringArray(R.array.path_values)
            val sp = PreferenceManager.getDefaultSharedPreferences(this.context)
            //        Log.d(TAG, "read: " + sp.getString("path", strings[0]));
            when (sp.getString("path", strings[0])) {
                strings[0] -> {
                    selection = null
                }
                strings[1] -> {
                    selection = App.imageScannerUtil.screenshotsPath
                }
                strings[2] -> {
                    val externalFilesDir = Environment.getExternalStorageDirectory()
        
                    selection = if (externalFilesDir != null) {
                        "${externalFilesDir.absolutePath}/${sp.getString("customizePath", "")}"
                    } else {
                        App.output.showToast("无法获取外置存储位置, 替换为默认查询")
                        null
                    }
                }
            }
        }
        return selection
    }
    
    fun getSimplifiedPathInExternalStorage(completePath: String?): String? {
        return App.fileUtil.getSimplifiedPathInExternalStorage(completePath)
    }
    
    fun getRecentImagePaths(): MutableList<String?> {
        return App.recentImages.imagePaths
    }
    
    fun getCurrentImagePath(): String? {
        return App.recentImages.currentImagePath
    }
    
    fun getCurrentImagePathIndex(): Int {
        return App.recentImages.currentImagePathIndex
    }
}