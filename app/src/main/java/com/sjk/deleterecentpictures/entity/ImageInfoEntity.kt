package com.sjk.deleterecentpictures.entity

import android.net.Uri
import com.flyjingfish.openimagelib.beans.OpenImageUrl
import com.flyjingfish.openimagelib.enums.MediaType

data class ImageInfoEntity(
    val path: String? = null,
    @Transient val uri: Uri? = null,
    val id: Long? = null,
    val dateAdded: Long? = null,
    val dateModified: Long? = null,
    val mimeType: String? = null,
) : OpenImageUrl {
    override fun getImageUrl(): String {
        return this.path ?: ""
    }

    override fun getVideoUrl(): String {
        return ""
    }

    override fun getCoverImageUrl(): String {
        return ""
    }

    override fun getType(): MediaType {
        return MediaType.IMAGE
    }
}