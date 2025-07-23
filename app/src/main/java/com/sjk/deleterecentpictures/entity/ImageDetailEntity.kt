package com.sjk.deleterecentpictures.entity

data class ImageDetailEntity(
    val data: String,
    val dateAdded: Long,
    val dateModified: Long,
    val displayName: String,
    val mimeType: String,
    val size: Long,
    val width: Int,
    val height: Int,
)