package com.sjk.deleterecentpictures.utils

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.flyjingfish.openimagelib.listener.BigImageHelper
import com.flyjingfish.openimagelib.listener.OnLoadBigImageListener
import com.sjk.deleterecentpictures.common.App


class BigImageHelperImpl : BigImageHelper {
    override fun loadImage(
        context: Context?,
        imageUrl: String?,
        onLoadBigImageListener: OnLoadBigImageListener?
    ) {
        if (context == null || imageUrl == null) {
            onLoadBigImageListener?.onLoadImageFailed()
            return
        }
        App.imageLoadManger.createLoadImage(context, imageUrl)
            .into(object : CustomTarget<Drawable?>() {
                override fun onResourceReady(
                    drawable: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    onLoadBigImageListener?.onLoadImageSuccess(drawable, imageUrl)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }
}