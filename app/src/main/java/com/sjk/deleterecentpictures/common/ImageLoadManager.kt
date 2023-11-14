package com.sjk.deleterecentpictures.common

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.sjk.deleterecentpictures.bean.ImageInfoBean

object ImageLoadManager {
    
    /**
     * 加载图片到图片控件
     */
    fun loadImageToImageView(context: Context, imageInfo: ImageInfoBean, imageView: ImageView?, skipMemoryCache: Boolean = true) {
        if (imageView == null) {
            return
        }
        Glide.with(context)
            .load(imageInfo.uri)
            .skipMemoryCache(skipMemoryCache)
            .into(imageView)
    }
    
    /**
     * 清除图片控件的图片
     */
    fun clearImageView(context: Context, imageView: ImageView?) {
        if (imageView == null) {
            return
        }
        Glide.with(context)
            .clear(imageView)
    }
    
}