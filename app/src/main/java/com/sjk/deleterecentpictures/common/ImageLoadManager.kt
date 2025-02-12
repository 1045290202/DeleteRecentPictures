package com.sjk.deleterecentpictures.common

import android.content.Context
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromContent
import com.sjk.deleterecentpictures.bean.ImageInfoBean

object ImageLoadManager {

    /**
     * 加载图片到图片控件
     */
    fun loadImageToImageView(context: Context, imageInfo: ImageInfoBean, imageView: ZoomImageView?, useSubsampling: Boolean = true) {
        if (imageView == null || imageInfo.uri == null) {
            return
        }

        if (useSubsampling) {
            // 设置子采样图片源
            imageView.setSubsamplingImage(ImageSource.fromContent(context, imageInfo.uri))
        }
        // 利用 Glide 加载较模糊的缩略图
        Glide.with(context)
            .load(imageInfo.uri)
            .skipMemoryCache(true)
            .into(imageView)
    }

    /**
     * 清除图片控件的图片
     */
    fun clearImageView(context: Context, imageView: ZoomImageView?) {
        if (imageView == null) {
            return
        }

        Glide.with(context)
            .clear(imageView)

//        imageView.setSubsamplingImage()
    }

}