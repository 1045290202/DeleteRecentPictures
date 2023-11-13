package com.sjk.deleterecentpictures.common

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import java.io.File

object ImageLoadManger {
    // 格式转换白名单
    private val CONVERT_WHITE_LIST: Set<String> = setOf("image/avif", "image/heic", "image/x-icon")
    
    private var lastLoadedFile: File? = null
    private var lastTarget: CustomTarget<Bitmap>? = null
    private var lastThread: Thread? = null
    
    /**
     * 加载图片到缓存
     */
    private fun loadToCache(activity: Activity, imageInfo: ImageInfoBean, callback: (Uri) -> Unit) {
        this.lastTarget?.request?.clear()
        this.lastThread?.interrupt()
        this.lastLoadedFile?.delete()
        this.lastThread = Thread {
            val file = File(App.applicationContext.cacheDir, imageInfo.id.toString())
            if (file.exists()) {
                val uri = Uri.fromFile(file)
                if (activity.isDestroyed) {
                    callback(uri)
                    return@Thread
                }
                activity.runOnUiThread {
                    callback(uri)
                }
                return@Thread
            }
            this.lastTarget = Glide.with(App.applicationContext)
                .asBitmap()
                .load(imageInfo.uri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        file.outputStream().use { out ->
                            resource.compress(
                                if (resource.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                                90,
                                out
                            )
                            
                            out.flush()
                            out.close()
                        }
                        this@ImageLoadManger.lastLoadedFile = file
                        val uri = Uri.fromFile(file)
                        if (activity.isDestroyed) {
                            callback(uri)
                            return
                        }
                        activity.runOnUiThread {
                            callback(uri)
                        }
                    }
                    
                    override fun onLoadCleared(placeholder: Drawable?) {
                    
                    }
                })
        }
        this.lastThread!!.start()
    }
    
    /**
     * 加载图片
     */
    fun loadImage(activity: Activity, imageInfo: ImageInfoBean, callback: (Uri) -> Unit) {
        if (imageInfo.mimeType !in CONVERT_WHITE_LIST) {
            callback(imageInfo.uri!!)
            return
        }
        
        this.loadToCache(activity, imageInfo, callback)
    }
    
}