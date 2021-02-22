package com.sjk.deleterecentpictures.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object TransformUtil {
    fun filePath2Bitmap(imagePath: String?): Bitmap? {
        return BitmapFactory.decodeFile(imagePath)
    }
}