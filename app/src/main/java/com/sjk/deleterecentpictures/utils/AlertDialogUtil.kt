package com.sjk.deleterecentpictures.utils

import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import android.widget.AdapterView
import android.view.View

object AlertDialogUtil {
    
    /**
     * 允许弹窗文本选择
     */
    fun enableMessageSelection(
        alertDialog: AlertDialog,
        messageTextViewId: Int = android.R.id.message
    ) {
        alertDialog.window?.findViewById<TextView>(messageTextViewId)?.setTextIsSelectable(true)
    }
    
    /**
     * 禁止弹窗文本选择
     */
    fun disableMessageSelection(
        alertDialog: AlertDialog,
        messageTextViewId: Int = android.R.id.message
    ) {
        alertDialog.window?.findViewById<TextView>(messageTextViewId)?.setTextIsSelectable(false)
    }
    
    /**
     * 禁止点击弹窗列表项时自动关闭弹窗，同时覆盖默认的点击事件
     */
    fun disableAutoDismissWhenItemClick(
        alertDialog: AlertDialog,
        onItemClickListener: ((adapterView: AdapterView<*>, view: View, i: Int, l: Long) -> Unit)?
    ) {
        alertDialog.listView.setOnItemClickListener { adapterView, view, i, l ->
            onItemClickListener?.invoke(adapterView, view, i, l)
        }
    }
    
    /**
     * 禁止点击弹窗列表项时自动关闭弹窗，同时覆盖默认的点击事件
     */
    fun disableAutoDismissWhenItemClick(
        alertDialog: AlertDialog,
    ) {
        alertDialog.listView.setOnItemClickListener { adapterView, view, i, l ->
        
        }
    }
    
}