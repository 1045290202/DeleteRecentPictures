package com.sjk.deleterecentpictures.common

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceManager
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.bean.ImageInfoBean

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
        this.context.let {
            val strings = it.resources.getStringArray(R.array.path_values)
            //        Log.d(TAG, "read: " + sp.getString("path", strings[0]));
            when (this.getSP().getString("path", strings[0])) {
                strings[0] -> {
                    selection = null
                }
                strings[1] -> {
                    selection = App.imageScannerUtil.screenshotsPath
                }
                strings[2] -> {
                    val externalFilesDir = Environment.getExternalStorageDirectory()

                    selection = if (externalFilesDir != null) {
                        "${externalFilesDir.absolutePath}/${this.getSP().getString("customizePath", "")}"
                    } else {
                        App.output.showToast("无法获取外置存储位置, 替换为默认查询")
                        null
                    }
                }
            }
        }
        return selection
    }

    fun getSimplifiedPathInExternalStorage(imageInfo: ImageInfoBean?): String? {
        return App.fileUtil.getSimplifiedPathInExternalStorage(imageInfo?.path)
    }
    
    fun getFileNameByPath(imageInfo: ImageInfoBean?): String? {
        return App.fileUtil.getFileNameByPath(imageInfo?.path)
    }
    
    fun getFileNameByPath(path: String?): String? {
        return App.fileUtil.getFileNameByPath(path)
    }

    /**
     * 获取最近图片的信息
     */
    fun getRecentImageInfos(): MutableList<ImageInfoBean?> {
        return App.recentImages.imageInfos
    }

    fun getCurrentImageInfo(): ImageInfoBean? {
        return App.recentImages.currentImageInfo
    }

    fun getCurrentImageInfoIndex(): Int {
        return App.recentImages.currentImageInfoIndex
    }

    fun getImageChecks(): MutableList<Boolean> {
        return App.recentImages.imageChecks
    }

    fun getAllCheckedImageInfos(): MutableList<ImageInfoBean?> {
        val checkedImagePaths: MutableList<ImageInfoBean?> = ArrayList()
        for ((index, imageCheck) in this.getImageChecks().withIndex()) {
            if (!imageCheck) {
                continue
            }
            checkedImagePaths.add(this.getRecentImageInfos()[index])
        }
        return checkedImagePaths
    }

    fun getNavigationBarHeight(): Int {
        val resourceId: Int = this.context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return this.context.resources.getDimensionPixelSize(resourceId)
    }

    fun getSortOrder(): String {
        var sorterOrderType: String = App.imageScannerUtil.DATE_MODIFIED

        this.context.let {
            val strings = it.resources.getStringArray(R.array.sort_order)
            when (this.getSP().getString("sortOrder", strings[0])) {
                strings[0] -> {
                    sorterOrderType = App.imageScannerUtil.DATE_MODIFIED
                }
                strings[1] -> {
                    sorterOrderType = App.imageScannerUtil.DATE_ADDED
                }
            }
        }

        return sorterOrderType
    }

    /**
     * 获取当前屏幕的旋转方向
     */
    fun getCurrentScreenOrientation(): Int {
        return App.appResources.configuration.orientation
    }
}