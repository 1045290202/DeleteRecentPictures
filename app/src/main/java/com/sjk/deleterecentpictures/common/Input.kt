package com.sjk.deleterecentpictures.common

import com.sjk.deleterecentpictures.R

object Input {
    fun setCurrentImagePathIndex(index: Int) {
        App.recentImages.currentImageInfoIndex = index
    }
    
    fun setAllImageChecksFalse() {
        App.recentImages.imageChecks.run {
            for (index in this.indices) {
                this[index] = false
            }
        }
    }
    
    fun copyCurrentImagePath(): Boolean {
        if (App.dataSource.getCurrentImageInfo()?.path == null) {
            App.output.showToast(App.resources.getString(R.string.no_path))
            return false
        }

        App.clipboardUtil.setText(App.dataSource.getCurrentImageInfo()!!.path!!)
        App.output.showToast(App.resources.getString(R.string.copied))
        return true
    }
    
    fun copyCurrentImageName(): Boolean {
        if (App.dataSource.getCurrentImageInfo()?.path == null) {
            App.output.showToast(App.resources.getString(R.string.no_path))
            return false
        }

        App.clipboardUtil.setText(App.dataSource.getFileNameByPath(App.dataSource.getCurrentImageInfo())!!)
        App.output.showToast(App.resources.getString(R.string.copied))
        return true
    }
}