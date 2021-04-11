package com.sjk.deleterecentpictures.common

object Input {
    fun setCurrentImagePathIndex(index: Int) {
        App.recentImages.currentImagePathIndex = index
    }
    
    fun setAllImageChecksFalse() {
        App.recentImages.imageChecks.run {
            for (index in this.indices) {
                this[index] = false
            }
        }
    }
}