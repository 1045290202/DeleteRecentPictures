package com.sjk.deleterecentpictures.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App

object ClipboardUtil {
    fun setText(text: String) {
        val clipboard = App.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(App.context.resources.getString(R.string.app_name), text)
        clipboard.setPrimaryClip(clip)
    }
}