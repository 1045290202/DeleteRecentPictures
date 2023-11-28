package com.sjk.deleterecentpictures.bean

import android.net.Uri
import java.io.Serializable

data class ImageInfoBean(
    val path: String? = null,
    @Transient val uri: Uri? = null,
    val id: Long? = null,
    val dateAdded: Long? = null,
    val dateModified: Long? = null,
    val mimeType: String? = null,
): Serializable