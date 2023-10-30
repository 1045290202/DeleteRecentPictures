package com.sjk.deleterecentpictures.bean

import android.net.Uri

data class ImageInfoBean(
    val path: String? = null,
    val uri: Uri? = null,
    val id: Long? = null,
    val dateAdded: Long? = null,
    val dateModified: Long? = null,
    val mimeType: String? = null,
)