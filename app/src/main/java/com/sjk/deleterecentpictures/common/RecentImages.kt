package com.sjk.deleterecentpictures.common

object RecentImages {
    val imagePaths: MutableList<String?> = mutableListOf()
    var currentImagePathIndex: Int = 0
    val currentImagePath: String?
        get() {
            if (this.currentImagePathIndex >= this.imagePaths.size || currentImagePathIndex < 0) {
                return null
            }
            
            return this.imagePaths[this.currentImagePathIndex]
        }
    
    val imageChecks: MutableList<Boolean> = mutableListOf()
    
    fun clearImagePaths() {
        this.imagePaths.clear()
    }
    
    fun resetCurrentImagePathIndex() {
        this.currentImagePathIndex = 0
    }
    
    fun clearImageChecks() {
        this.imageChecks.clear()
    }
}