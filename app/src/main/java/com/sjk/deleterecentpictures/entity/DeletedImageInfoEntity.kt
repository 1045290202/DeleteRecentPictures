package com.sjk.deleterecentpictures.entity

import java.io.File

data class DeletedImageInfoEntity(
    val oldFile: File,
    val newFile: File,
    val info: ImageInfoEntity?,
)