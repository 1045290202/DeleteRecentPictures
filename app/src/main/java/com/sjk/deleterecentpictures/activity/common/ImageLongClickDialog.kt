package com.sjk.deleterecentpictures.activity.common

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.logD
import kotlin.concurrent.thread

class ImageLongClickDialog(activityContext: Activity, filePath: String?) {
    private var dialogBuilder: MaterialAlertDialogBuilder
    
    companion object {
        const val TAG: String = "ImageLongClickDialog"
        
        fun build(activityContext: Activity = App.currentActivity, filePath: String?): ImageLongClickDialog {
            return ImageLongClickDialog(activityContext, filePath)
        }
    }
    
    init {
        this.dialogBuilder = MaterialAlertDialogBuilder(activityContext)
                .setTitle("请选择你的操作")
                .setItems(App.const.IMAGE_LONG_CLICK_DIALOG_ITEMS) { dialogInterface: DialogInterface, i: Int ->
                    this.onImageLongClickDialogItemClick(dialogInterface, i, filePath)
                }
                .setNegativeButton("取消") { dialog: DialogInterface, _: Int -> dialog.cancel() }
    }
    
    fun show() {
        this.dialogBuilder.show()
    }
    
    private fun onImageLongClickDialogItemClick(dialogInterface: DialogInterface, i: Int, filePath: String?) {
        logD(TAG, "点击item $i")
        when (i) {
            0 -> {
                if (!App.output.openByOtherApp(filePath)) {
                    App.output.showToast("唤起打开方式失败")
                }
            }
            1 -> {
                if (!App.output.shareToOtherApp(filePath)) {
                    App.output.showToast("唤起分享方式失败")
                }
            }
            2 -> {
                val discern = Thread {
                    val content: String? = App.qrCodeUtil.decodeQRCode(filePath)
            
                    App.currentActivity.runOnUiThread {
                        if (content == null) {
                            App.output.showToast("未发现二维码")
                            return@runOnUiThread
                        }
                        MaterialAlertDialogBuilder(this.dialogBuilder.context)
                                .setTitle("二维码内容")
                                .setMessage("$content")
                                .setNegativeButton("取消") { dialogInterface: DialogInterface, i: Int ->
                                    dialogInterface.cancel()
                                }
                                .setNeutralButton("复制") { dialogInterface: DialogInterface, i: Int ->
                                    App.clipboardUtil.setText(content)
                                    App.output.showToast("已复制到剪切板")
                                }
                                .setPositiveButton("浏览器打开") { dialogInterface: DialogInterface, i: Int ->
                                    if (!App.output.openLinkWithBrowser(content)) {
                                        App.output.showToast("链接打开失败")
                                    }
                                }
                                .show()
                    }
                }
                discern.start()
            }
            else -> {
            
            }
        }
    }
}