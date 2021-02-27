/**
 * 已废弃
 */

package com.sjk.deleterecentpictures.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream


object TransformUtil {
    fun filePath2Bitmap(imagePath: String?): Bitmap? {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap = BitmapFactory.decodeFile(imagePath, options)
        
        val baos = ByteArrayOutputStream()
        val quality = 50
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        bitmap.recycle()
        val bytes = baos.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        
//        val options = BitmapFactory.Options()
//        options.inPreferredConfig = Bitmap.Config.RGB_565
//        return BitmapFactory.decodeFile(imagePath, options)
    }
}