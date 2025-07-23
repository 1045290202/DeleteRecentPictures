package com.sjk.deleterecentpictures.activity.common

import android.app.Activity
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.logD

class ImageLongClickDialog(context: Activity, filePath: String?) {

    private val context: Activity
    private val filePath: String?

    companion object {
        const val TAG: String = "ImageLongClickDialog"

        fun build(
            context: Activity? = App.activityManager.currentActivity,
            filePath: String?
        ): ImageLongClickDialog? {
            if (context == null) {
                return null
            }
            return ImageLongClickDialog(context, filePath)
        }
    }

    init {
        this.context = context
        this.filePath = filePath
    }

    fun show() {
        val itemStrings: Array<String> =
            App.const.IMAGE_LONG_CLICK_DIALOG_ITEMS.map { App.resources.getString(it) }
                .toTypedArray()
        val alertDialog = MaterialAlertDialogBuilder(this.context)
            .setTitle(App.resources.getString(R.string.choose_your_action))
            .setItems(itemStrings) { dialogInterface: DialogInterface, i: Int ->
                this.onImageLongClickDialogItemClick(dialogInterface, i, this.filePath)
            }
            .setNegativeButton(App.resources.getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        alertDialog.show()
    }

    private fun onImageLongClickDialogItemClick(
        dialogInterface: DialogInterface,
        i: Int,
        filePath: String?
    ) {
        logD(TAG, "点击item $i")
        when (i) {
            0 -> {
                if (!App.output.openByOtherApp(filePath)) {
                    App.output.showToast(App.resources.getString(R.string.failed_to_invoke_open_method))
                }
            }

            1 -> {
                if (!App.output.shareToOtherApp(filePath)) {
                    App.output.showToast(App.resources.getString(R.string.failed_to_invoke_sharing_method))
                }
            }

            2 -> {
                val discern = Thread {
                    val content: String? = App.qrCodeUtil.decodeQRCode(filePath)

                    App.activityManager.currentActivity?.runOnUiThread {
                        if (content == null) {
                            App.output.showToast(App.resources.getString(R.string.barcode_or_qrcode_not_found))
                            return@runOnUiThread
                        }
                        val alertDialog = MaterialAlertDialogBuilder(this.context)
                            .setTitle(App.resources.getString(R.string.recognized_content))
                            .setMessage("$content")
                            .setNegativeButton(App.resources.getString(R.string.cancel)) { dialogInterface: DialogInterface, i: Int ->
                                dialogInterface.cancel()
                            }
                            .setNeutralButton(App.resources.getString(R.string.copy)) { _: DialogInterface, i: Int ->
                                App.clipboardUtil.setText(content)
                                App.output.showToast(App.resources.getString(R.string.copied))
                            }
                            .setPositiveButton(App.resources.getString(R.string.open_with_browser)) { _: DialogInterface, i: Int ->
                                if (!App.output.openLinkWithBrowser(content)) {
                                    App.output.showToast(App.resources.getString(R.string.open_failed))
                                }
                            }
                            .create()

                        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                        alertDialog.show()
                        App.alertDialogUtil.enableMessageSelection(alertDialog)
                    }
                }
                discern.start()
            }

            else -> {

            }
        }
    }
}