package com.sjk.deleterecentpictures.common

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceManager
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.entity.ImageDetailEntity
import com.sjk.deleterecentpictures.entity.ImageInfoEntity

object DataSource {

    val context: Context
        get() {
            return App.applicationContext
        }

    fun getSP(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(this.context)
    }

    fun getNumberOfPictures(): Int {
        val str = this.getSP()
            .getString("numberOfPictures", App.const.DEFAULT_NUMBER_OF_PICTURES.toString())
        var numberOfPictures: Int = try {
            if (str == null || str == "") App.const.DEFAULT_NUMBER_OF_PICTURES else str.toInt()
        } catch (e: NumberFormatException) {
            App.const.DEFAULT_NUMBER_OF_PICTURES
        }
        if (numberOfPictures == 0) {
            numberOfPictures = Const.DEFAULT_NUMBER_OF_PICTURES
        }
        return numberOfPictures
    }

    fun getSelection(): MutableSet<String> {
        val selectionList: MutableSet<String> = mutableSetOf()
        this.context.let {
            val strings = it.resources.getStringArray(R.array.path_values)
            //        Log.d(TAG, "read: " + sp.getString("path", strings[0]));
            when (this.getSP().getString("path", strings[0])) {
                strings[0] -> {
                }

                strings[1] -> {
                    selectionList.add(App.imageScannerUtil.screenshotsPath)
                }

                strings[2] -> {
                    val externalFilesDir = Environment.getExternalStorageDirectory()
                    if (externalFilesDir == null) {
                        App.output.showToast(this.context.getString(R.string.use_default_selection_because_error))
                    } else {
                        val paths = this.getSP().getString("customizePath", "")!!.split("|")
                        for (path in paths) {
                            if (path.isEmpty()) {
                                continue
                            }
                            selectionList.add("${externalFilesDir.absolutePath}/$path")
                        }
                    }
                }

                else -> {}
            }
        }
        return selectionList
    }

    fun getSimplifiedPathInExternalStorage(imageInfo: ImageInfoEntity?): String? {
        return App.fileUtil.getSimplifiedPathInExternalStorage(imageInfo?.path)
    }

    fun getFileNameByPath(imageInfo: ImageInfoEntity?): String? {
        return App.fileUtil.getFileNameByPath(imageInfo?.path)
    }

    fun getFileNameByPath(path: String?): String? {
        return App.fileUtil.getFileNameByPath(path)
    }

    /**
     * 获取最近图片的信息
     */
    fun getRecentImageInfos(): MutableList<ImageInfoEntity?> {
        return App.recentImages.imageInfos
    }

    fun getCurrentImageInfo(): ImageInfoEntity? {
        return App.recentImages.currentImageInfo
    }

    fun getCurrentImageInfoIndex(): Int {
        return App.recentImages.currentImageInfoIndex
    }

    fun getImageChecks(): MutableList<Boolean> {
        return App.recentImages.imageChecks
    }

    fun getAllCheckedImageInfos(): MutableList<ImageInfoEntity?> {
        val checkedImagePaths: MutableList<ImageInfoEntity?> = ArrayList()
        for ((index, imageCheck) in this.getImageChecks().withIndex()) {
            if (!imageCheck) {
                continue
            }
            checkedImagePaths.add(this.getRecentImageInfos()[index])
        }
        return checkedImagePaths
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
        return App.resources.configuration.orientation
    }

    /**
     * 获取图片的详细信息
     */
    fun getImageDetails(imageInfoBean: ImageInfoEntity?): ImageDetailEntity? {
        if (imageInfoBean?.id == null) {
            return null
        }
        return App.imageScannerUtil.getImageDetails(App.applicationContext, imageInfoBean.id)
    }
}