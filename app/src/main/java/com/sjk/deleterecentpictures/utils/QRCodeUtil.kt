package com.sjk.deleterecentpictures.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.*
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import java.util.*


object QRCodeUtil {
    private val hints: Map<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
    
    fun decodeQRCode(picturePath: String?): String? {
        return this.decodeQRCode(this.getDecodeAbleBitmap(picturePath))
    }
    
    fun decodeQRCode(bitmap: Bitmap?): String? {
        if (bitmap == null) {
            return null
        }
    
        var result: Result?
        var source: RGBLuminanceSource? = null
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            source = RGBLuminanceSource(width, height, pixels)
            result = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)), this.hints)
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            if (source != null) {
                try {
                    result = MultiFormatReader().decode(BinaryBitmap(GlobalHistogramBinarizer(source)), this.hints)
                    return result.text
                } catch (e2: Throwable) {
                    e2.printStackTrace()
                }
            }
            null
        }
    }
    
    private fun getDecodeAbleBitmap(picturePath: String?): Bitmap? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(picturePath, options)
            var sampleSize = options.outHeight / 400
            if (sampleSize <= 0) {
                sampleSize = 1
            }
            options.inSampleSize = sampleSize
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(picturePath, options)
        } catch (e: Exception) {
            null
        }
    }
}