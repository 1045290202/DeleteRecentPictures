package com.sjk.deleterecentpictures.common

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.activity.common.ImageLongClickDialog
import java.io.File
import kotlin.system.exitProcess


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
    
    fun showSnackBarIndefinite(view: View, content: CharSequence, onSnackBarCreate: ((it: Snackbar) -> Unit)?) {
        val snackbar = Snackbar.make(view, content, Snackbar.LENGTH_INDEFINITE)
        onSnackBarCreate?.invoke(snackbar)
        snackbar.show()
    }
    
    fun openByOtherApp(filePath: String?): Boolean {
        if (filePath == null) {
            return false
        }
        
        return this.openByOtherApp(File(filePath))
    }
    
    fun openByOtherApp(file: File): Boolean {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                
                val uri = FileProvider.getUriForFile(
                    App.context,
                    App.context.packageName + ".provider",
                    file
                )
                intent.setDataAndType(uri, App.fileUtil.getMimeType(file.absolutePath))
            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(
                    Uri.fromFile(file),
                    App.fileUtil.getMimeType(file.absolutePath)
                )
            }
            App.context.startActivity(intent)
        } catch (e: Exception) {
            return false
        }
        
        return true
    }
    
    fun shareToOtherApp(filePath: String?): Boolean {
        if (filePath == null) {
            return false
        }
        
        return this.shareToOtherApp(File(filePath))
    }
    
    fun shareToOtherApp(file: File): Boolean {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = App.fileUtil.getMimeType(file.absolutePath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                
                val uri = FileProvider.getUriForFile(
                    App.context,
                    App.context.packageName + ".provider",
                    file
                )
                intent.putExtra(Intent.EXTRA_STREAM, uri)
            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            }
            App.context.startActivity(intent)
        } catch (e: Exception) {
            return false
        }
        
        return true
    }
    
    fun openLinkWithBrowser(url: String): Boolean {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            App.context.startActivity(intent)
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
            .setTitle(App.appResources.getString(R.string.prompt))
            .setMessage(
                App.appResources.getString(
                    R.string.delete_confirmation_prompt_single,
                    this.dataSource.getCurrentImageInfo()!!.path
                ))
            .setPositiveButton(App.appResources.getString(R.string.determine)) { dialog: DialogInterface?, witch: Int ->
                positiveCallback(
                    dialog,
                    witch
                )
            }
            .setNegativeButton(App.appResources.getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
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
                stringBuilder.append(App.appResources.getString(R.string.backslash_n_x2))
            }
            if (imageInfo == null) {
                stringBuilder.append(App.appResources.getString(R.string.image_path_is_empty))
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
                App.appResources.getString(
                    R.string.delete_confirmation_prompt,
                    allCheckedImagePaths.size.toString()
                ))
            .setMessage(stringBuilder.toString())
//                .setItems(items.toTypedArray()) { dialog: DialogInterface?, witch: Int ->
//                    this.showDeleteCheckedImagesDialog(positiveCallback)
//                }
            .setPositiveButton(App.appResources.getString(R.string.determine)) { dialog: DialogInterface?, witch: Int ->
                positiveCallback(
                    dialog,
                    witch
                )
            }
            .setNegativeButton(App.appResources.getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
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
            .setTitle(App.appResources.getString(R.string.current_image_path))
            .setMessage("${this.dataSource.getCurrentImageInfo()!!.path}")
            .setNegativeButton(App.appResources.getString(R.string.close)) { dialog: DialogInterface?, witch: Int -> dialog?.cancel() }
            .setNeutralButton(App.appResources.getString(R.string.copy)) { dialog: DialogInterface, _: Int ->
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
        val resources = App.context.resources
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.context)
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.context)
        if (sharedPreferences.getBoolean("privacyPolicyShowed", false)) {
            callback?.invoke()
            return
        }
        this.showPrivacyPolicyDialog(callback)
    }
    
}