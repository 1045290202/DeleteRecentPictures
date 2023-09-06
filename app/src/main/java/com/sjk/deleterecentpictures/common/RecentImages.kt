package com.sjk.deleterecentpictures.common

import com.sjk.deleterecentpictures.bean.ImageInfoBean

object RecentImages {
    val imageInfos: MutableList<ImageInfoBean?> = ArrayList()
    var currentImageInfoIndex: Int = 0

    val currentImageInfo: ImageInfoBean?
        get() {
            if (this.currentImageInfoIndex >= this.imageInfos.size || this.currentImageInfoIndex < 0) {
                return null
            }

            return this.imageInfos[this.currentImageInfoIndex]
        }

    val currentImagePath: String?
        get() {
            return this.currentImageInfo?.path
        }

    val imageChecks: MutableList<Boolean> = ArrayList()

    fun clearImagePaths() {
        this.imageInfos.clear()
    }

    fun resetCurrentImagePathIndex() {
        this.currentImageInfoIndex = 0
    }

    fun clearImageChecks() {
        this.imageChecks.clear()
    }
}