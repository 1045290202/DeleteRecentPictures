package com.sjk.deleterecentpictures.utils

import android.widget.TextView
import androidx.appcompat.app.AlertDialog

object AlertDialogUtil {
    /**
     * 允许弹窗文本可以选择
     */
    fun enableMessageSelection(alertDialog: AlertDialog, messageTextViewId: Int = android.R.id.message) {
        alertDialog.window?.findViewById<TextView>(messageTextViewId)?.setTextIsSelectable(true)
    }
    
    fun disableMessageSelection(alertDialog: AlertDialog, messageTextViewId: Int = android.R.id.message) {
        alertDialog.window?.findViewById<TextView>(messageTextViewId)?.setTextIsSelectable(false)
    }
}