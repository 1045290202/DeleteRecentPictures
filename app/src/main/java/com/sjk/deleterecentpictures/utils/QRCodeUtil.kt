/**
 * Description: 二维码工具类，使用了zxing库
 */

package com.sjk.deleterecentpictures.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.*
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import java.io.FileInputStream
import java.util.*


object QRCodeUtil {
    private val hints: Map<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
    
    /**
     * 二维码解码
     */
    fun decodeQRCode(filePath: String?): String? {
        return this.decodeQRCode(this.getDecodeAbleBitmap(filePath))
    }
    
    /**
     * 二维码解码
     */
    fun decodeQRCode(bitmap: Bitmap?): String? {
        if (bitmap == null) {
            return null
        }
        
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var result: Result?
        val source: RGBLuminanceSource?
        var invertedSource: InvertedLuminanceSource? = null
        return try {
            source = RGBLuminanceSource(width, height, pixels)
            result = this.decodeQRCodeWithHybridBinarizer(source)
            if (result == null) {
                invertedSource = InvertedLuminanceSource(source)
                result = this.decodeQRCodeWithHybridBinarizer(invertedSource)
            }
            if (result == null) {
                result = this.decodeQRCodeWithGlobalHistogramBinarizer(source)
            }
            if (result == null) {
                result = this.decodeQRCodeWithGlobalHistogramBinarizer(invertedSource!!)
            }
            result?.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun decodeQRCodeWithHybridBinarizer(source: LuminanceSource): Result? {
        return try {
            val binarizer = HybridBinarizer(source)
            val binaryBitmap = BinaryBitmap(binarizer)
            MultiFormatReader().decode(binaryBitmap, this.hints)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun decodeQRCodeWithGlobalHistogramBinarizer(source: LuminanceSource): Result? {
        return try {
            val binarizer = GlobalHistogramBinarizer(source)
            val binaryBitmap = BinaryBitmap(binarizer)
            MultiFormatReader().decode(binaryBitmap, this.hints)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun getDecodeAbleBitmap(filePath: String?): Bitmap? {
        return try {
            val fileInputStream = FileInputStream(filePath)
            BitmapFactory.decodeStream(fileInputStream)
        } catch (e: Exception) {
            null
        }
    }
}