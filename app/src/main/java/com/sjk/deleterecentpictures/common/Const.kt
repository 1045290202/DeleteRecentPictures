package com.sjk.deleterecentpictures.common

import com.sjk.deleterecentpictures.R

object Const {
    const val DEFAULT_NUMBER_OF_PICTURES = 10
    val IMAGE_LONG_CLICK_DIALOG_ITEMS: Array<Int> = arrayOf(
        R.string.open_method,
        R.string.share,
        R.string.barcode_or_qrcode_recognition
    )
    // 自动滚动的时间间隔
    const val AUTO_SCROLL_INTERVAL = 300L
    // 自动滚动的延迟时间
    const val AUTO_SCROLL_DELAY = 1000L
}
