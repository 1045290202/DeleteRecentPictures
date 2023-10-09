package com.sjk.deleterecentpictures.common

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.activity.common.ImageLongClickDialog
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import java.io.File
import kotlin.Pair
import android.text.format.Formatter
import android.util.TypedValue


object Output {
    const val TAG: String = "Output"
    private val dataSource: DataSource = DataSource
    
    fun showToast(content: CharSequence) {
        Toast.makeText(this.dataSource.context, content, Toast.LENGTH_SHORT).show()
    }
    
    fun showToast(content: Int) {
        Toast.makeText(this.dataSource.context, content.toString(), Toast.LENGTH_SHORT).show()
    }
    
    fun showToastLong(content: CharSequence) {
        Toast.makeText(this.dataSource.context, content, Toast.LENGTH_LONG).show()
    }
    
    fun showToastLong(content: Int) {
        Toast.makeText(this.dataSource.context, content.toString(), Toast.LENGTH_LONG).show()
    }
    
    fun showSnackBarIndefinite(
        view: View,
        content: CharSequence,
        onSnackBarCreate: ((it: Snackbar) -> Unit)?,
    ) {
        val snackbar = Snackbar.make(view, content, Snackbar.LENGTH_INDEFINITE)
        onSnackBarCreate?.invoke(snackbar)
        snackbar.show()
    }
    
    fun openByOtherApp(filePath: String?): Boolean {
        return if (filePath == null) {
            false
        } else {
            this.openByOtherApp(File(filePath))
        }
    }
    
    fun openByOtherApp(file: File): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                
                val uri = FileProvider.getUriForFile(
                    App.applicationContext,
                    App.applicationContext.packageName + ".provider",
                    file
                )
                intent.setDataAndType(uri, App.fileUtil.getMimeType(file.absolutePath))
            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(
                    Uri.fromFile(file),
                    App.fileUtil.getMimeType(file.absolutePath),
                )
            }
            if (App.activityManager.currentActivity == null) {
                return false
            }
            App.activityManager.currentActivity?.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun shareToOtherApp(filePath: String?): Boolean {
        return if (filePath == null) {
            false
        } else {
            this.shareToOtherApp(File(filePath))
        }
    }
    
    fun shareToOtherApp(file: File): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = App.fileUtil.getMimeType(file.absolutePath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                
                val uri = FileProvider.getUriForFile(
                    App.applicationContext,
                    App.applicationContext.packageName + ".provider",
                    file
                )
                intent.putExtra(Intent.EXTRA_STREAM, uri)
            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            }
            if (App.activityManager.currentActivity == null) {
                return false
            }
            App.activityManager.currentActivity?.startActivity(
                Intent.createChooser(
                    intent,
                    App.resources.getString(R.string.share),
                ),
            )
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun openLinkWithBrowser(url: String): Boolean {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            App.applicationContext.startActivity(intent)
        } catch (e: Exception) {
            logE(TAG, e.stackTraceToString())
            return false
        }
        return true
    }
    
    fun showImageLongClickDialog(filePath: String?) {
        ImageLongClickDialog.build(filePath = filePath)?.show()
    }
    
    fun showDeleteCurrentImageDialog(positiveCallback: (dialogInterface: DialogInterface?, witch: Int) -> Unit) {
        if (App.activityManager.currentActivity == null) {
            return
        }
        val alertDialog = MaterialAlertDialogBuilder(App.activityManager.currentActivity!!)
            .setTitle(App.resources.getString(R.string.prompt))
            .setMessage(
                App.resources.getString(
                    R.string.delete_confirmation_prompt_single,
                    this.dataSource.getCurrentImageInfo()!!.path
                )
            )
            .setPositiveButton(App.resources.getString(R.string.determine)) { dialog: DialogInterface?, witch: Int ->
                positiveCallback(
                    dialog,
                    witch
                )
            }
            .setNegativeButton(App.resources.getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        alertDialog.show()
        App.alertDialogUtil.enableMessageSelection(alertDialog)
    }
    
    fun showDeleteCheckedImagesDialog(positiveCallback: (dialogInterface: DialogInterface?, witch: Int) -> Unit) {
        val allCheckedImagePaths = this.dataSource.getAllCheckedImageInfos()
//        val items: MutableList<String> = mutableListOf()
        val stringBuilder: StringBuilder = StringBuilder()
        for ((i, imageInfo) in allCheckedImagePaths.withIndex()) {
            if (i > 0) {
                stringBuilder.append(App.resources.getString(R.string.backslash_n_x2))
            }
            if (imageInfo == null) {
                stringBuilder.append(App.resources.getString(R.string.image_path_is_empty))
//                items.add("图片路径为空")
            } else {
                stringBuilder.append("${imageInfo.path}")
//                items.add("$imageInfo")
            }
        }
        if (App.activityManager.currentActivity == null) {
            return
        }
        val alertDialog = MaterialAlertDialogBuilder(App.activityManager.currentActivity!!)
            .setTitle(
                App.resources.getString(
                    R.string.delete_confirmation_prompt,
                    allCheckedImagePaths.size.toString()
                )
            )
            .setMessage(stringBuilder.toString())
//                .setItems(items.toTypedArray()) { dialog: DialogInterface?, witch: Int ->
//                    this.showDeleteCheckedImagesDialog(positiveCallback)
//                }
            .setPositiveButton(App.resources.getString(R.string.determine)) { dialog: DialogInterface?, witch: Int ->
                positiveCallback(
                    dialog,
                    witch
                )
            }
            .setNegativeButton(App.resources.getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        alertDialog.show()
        App.alertDialogUtil.enableMessageSelection(alertDialog)
    }
    
    fun showPathButtonClickDialog() {
        if (App.activityManager.currentActivity == null) {
            return
        }
        val alertDialog = MaterialAlertDialogBuilder(App.activityManager.currentActivity!!)
            .setTitle(App.resources.getString(R.string.current_image_path))
            .setMessage("${this.dataSource.getCurrentImageInfo()!!.path}")
            .setNegativeButton(App.resources.getString(R.string.close)) { dialog: DialogInterface?, witch: Int -> dialog?.cancel() }
            .setNeutralButton(App.resources.getString(R.string.copy)) { dialog: DialogInterface, _: Int ->
                App.input.copyCurrentImagePath()
            }
            .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        alertDialog.show()
        App.alertDialogUtil.enableMessageSelection(alertDialog)
    }
    
    /**
     * 显示隐私政策弹窗
     */
    fun showPrivacyPolicyDialog(callback: (() -> Unit)? = null) {
        if (App.activityManager.currentActivity == null) {
            return
        }
        val resources = App.applicationContext.resources
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(App.applicationContext)
        val editor = sharedPreferences.edit()
        val alertDialog = MaterialAlertDialogBuilder(App.activityManager.currentActivity!!)
            .setTitle(resources.getString(R.string.privacy_policy))
            .setMessage(resources.getString(R.string.privacy_policy_content))
            .setPositiveButton(resources.getString(R.string.agree)) { dialog, which ->
                editor.putBoolean("privacyPolicyShowed", true)
                editor.apply()
                dialog.dismiss()
                callback?.invoke()
            }
            .setNegativeButton(resources.getString(R.string.disagree_and_exit)) { dialog, which ->
                editor.putBoolean("privacyPolicyShowed", false)
                editor.apply()
                dialog.dismiss()
                App.activityManager.finishAll()
            }
            .setCancelable(false)
            .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        alertDialog.show()
        App.alertDialogUtil.enableMessageSelection(alertDialog)
    }
    
    /**
     * 尝试显示隐私政策弹窗，如果之前显示过了，就不显示了
     */
    fun tryShowPrivacyPolicyDialog(callback: (() -> Unit)? = null) {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(App.applicationContext)
        if (sharedPreferences.getBoolean("privacyPolicyShowed", false)) {
            callback?.invoke()
            return
        }
        this.showPrivacyPolicyDialog(callback)
    }
    
    /**
     * 显示图片详情弹窗
     */
    fun showImageDetailsDialog(imageInfo: ImageInfoBean?) {
        Thread {
            val imageDetails = this.dataSource.getImageDetails(imageInfo) ?: return@Thread
            
            val configs = arrayOf(
                Pair(R.string.image_name, imageDetails.displayName),
                Pair(R.string.image_path, imageDetails.data),
                Pair(
                    R.string.image_size,
                    Formatter.formatFileSize(App.applicationContext, imageDetails.size)
                ),
                Pair(R.string.image_width, imageDetails.width.toString()),
                Pair(R.string.image_height, imageDetails.height.toString()),
                Pair(
                    R.string.image_date_added,
                    App.timeUtil.formatTimestampToSystemDefaultFormat(imageDetails.dateAdded * 1000),
                ),
                Pair(
                    R.string.image_date_modified,
                    App.timeUtil.formatTimestampToSystemDefaultFormat(imageDetails.dateModified * 1000),
                ),
                Pair(R.string.image_mime_type, imageDetails.mimeType),
            )
            
            App.activityManager.currentActivity!!.runOnUiThread {
                if (App.activityManager.currentActivity == null) {
                    return@runOnUiThread
                }
                
                fun getListTitleColor(): String {
                    val typedValue = TypedValue()
                    App.activityManager.currentActivity!!.theme.resolveAttribute(
                        R.attr.colorAccent,
                        typedValue,
                        true
                    )
                    return "#" + Integer.toHexString(typedValue.data).substring(2)
                }
                
                val alertDialog = MaterialAlertDialogBuilder(App.activityManager.currentActivity!!)
                    .setTitle(App.resources.getString(R.string.image_details))
                    .setItems(
                        configs.map {
                            // val str =
                            //     App.appResources.getString(it.first) + App.appResources.getString(
                            //         R.string.format_colon
                            //     ) + it.second
                            val str = """
                                <pre><font color="${getListTitleColor()}"><strong>${
                                App.resources.getString(
                                    it.first
                                )
                            }${
                                App.resources.getString(
                                    R.string.format_colon
                                )
                            }</strong></font>${it.second}</pre>
                            """.trimIndent()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT)
                            } else {
                                Html.fromHtml(str)
                            }
                        }.toTypedArray()
                    ) { _: DialogInterface?, _: Int -> }
                    .setPositiveButton(App.resources.getString(R.string.close)) { dialog: DialogInterface?, witch: Int -> dialog?.cancel() }
                    .create()
                alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                alertDialog.show()
                App.alertDialogUtil.disableAutoDismissWhenItemClick(alertDialog) { adapterView, view, i, l ->
                    App.clipboardUtil.setText(configs[i].second)
                    App.output.showToast(App.resources.getString(R.string.copied))
                }
            }
        }.start()
    }
    
}