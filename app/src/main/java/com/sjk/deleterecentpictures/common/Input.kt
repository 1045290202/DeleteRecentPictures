package com.sjk.deleterecentpictures.common

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
            App.output.showToast("无路径")
            return false
        }

        App.clipboardUtil.setText(App.dataSource.getCurrentImageInfo()!!.path!!)
        App.output.showToast("已复制到剪切板")
        return true
    }
}