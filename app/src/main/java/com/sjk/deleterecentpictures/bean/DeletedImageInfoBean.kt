package com.sjk.deleterecentpictures.bean

import java.io.File

data class DeletedImageInfoBean(
    val oldFile: File,
    val newFile: File,
    val info: ImageInfoBean?,
)