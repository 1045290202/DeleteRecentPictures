package com.sjk.deleterecentpictures.common

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import java.io.File

object ImageLoadManger {
    private var lastLoadedAvifFile: File? = null
    private var lastTarget: CustomTarget<Bitmap>? = null
    
    /**
     * 加载avif图片
     */
    fun loadAvif(imageInfo: ImageInfoBean, callback: (Uri) -> Unit): CustomTarget<Bitmap>? {
        this.lastTarget?.request?.clear()
        val file = File(App.applicationContext.cacheDir, "${imageInfo.id.toString()}.png")
        if (file.exists()) {
            val uri = Uri.fromFile(file)
            callback(uri)
            return this.lastTarget
        }
        this.lastTarget = Glide.with(App.applicationContext)
            .asBitmap()
            .load(imageInfo.uri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    file.outputStream().use { out ->
                        resource.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                        out.close()
                    }
                    this@ImageLoadManger.lastLoadedAvifFile = file
                    val uri = Uri.fromFile(file)
                    callback(uri)
                }
                
                override fun onLoadCleared(placeholder: Drawable?) {
                
                }
            })
        return this.lastTarget
    }
    
}