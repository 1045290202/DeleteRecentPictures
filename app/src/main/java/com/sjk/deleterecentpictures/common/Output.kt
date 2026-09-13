package com.sjk.deleterecentpictures.common

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.format.Formatter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.activity.common.ImageLongClickDialog
import com.sjk.deleterecentpictures.entity.ImageInfoEntity
import com.sjk.deleterecentpictures.utils.UpdateInfo
import com.sjk.deleterecentpictures.utils.UpdateManager
import java.io.File


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
            intent.data = url.toUri()
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

    fun showImageLongClickDialog(context: Activity?, filePath: String?) {
        ImageLongClickDialog.build(context, filePath)?.show()
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
            .setNegativeButton(App.resources.getString(R.string.close)) { dialog: DialogInterface?, _: Int -> dialog?.cancel() }
            .setNeutralButton(App.resources.getString(R.string.copy)) { _: DialogInterface, _: Int ->
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
            .setPositiveButton(resources.getString(R.string.agree)) { dialog, _ ->
                editor.putBoolean("privacyPolicyShowed", true)
                editor.apply()
                dialog.dismiss()
                callback?.invoke()
            }
            .setNegativeButton(resources.getString(R.string.disagree_and_exit)) { dialog, _ ->
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
     * 显示发现新版本弹窗
     */
    fun showUpdateDialog(updateInfo: UpdateInfo) {
        val activity = App.activityManager.currentActivity ?: return
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        val versionLine = App.resources.getString(
            R.string.update_available_message,
            updateInfo.versionName,
            UpdateManager.getCurrentVersionName()
        )
        val sizeLine = if (updateInfo.apkSize > 0) {
            "\n" + App.resources.getString(
                R.string.update_apk_size,
                Formatter.formatFileSize(activity, updateInfo.apkSize)
            )
        } else {
            ""
        }
        val notes = updateInfo.releaseNotes
            .substringBefore("Full Changelog:")
            .trim()
        val notesLine = if (notes.isNotBlank()) {
            "\n\n$notes"
        } else {
            ""
        }
        MaterialAlertDialogBuilder(activity)
            .setTitle(App.resources.getString(R.string.update_available_title))
            .setMessage("$versionLine$sizeLine$notesLine")
            .setPositiveButton(App.resources.getString(R.string.update_download)) { _, _ ->
                this.showUpdateDownloadDialog(updateInfo)
            }
            .setNeutralButton(App.resources.getString(R.string.update_open_release_page)) { _, _ ->
                try {
                    activity.startActivity(
                        Intent(Intent.ACTION_VIEW, updateInfo.htmlUrl.toUri())
                    )
                } catch (e: Exception) {
                    this.showToast(App.resources.getString(R.string.failed_to_invoke_open_method))
                }
            }
            .setNegativeButton(App.resources.getString(R.string.later), null)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                show()
                App.alertDialogUtil.enableMessageSelection(this)
            }
    }

    /**
     * 显示下载进度弹窗，下载完成后拉起安装
     */
    fun showUpdateDownloadDialog(updateInfo: UpdateInfo) {
        val activity = App.activityManager.currentActivity ?: return
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        val contentView = LayoutInflater.from(activity)
            .inflate(R.layout.layout_update_download, null, false)
        val progressBar = contentView.findViewById<ProgressBar>(R.id.progressBar)
        val progressText = contentView.findViewById<TextView>(R.id.progressText)
        val alertDialog = MaterialAlertDialogBuilder(activity)
            .setTitle(App.resources.getString(R.string.downloading_update_title))
            .setView(contentView)
            .setNegativeButton(App.resources.getString(R.string.cancel), null)
            .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        alertDialog.show()
        App.alertDialogUtil.disableMessageSelection(alertDialog)

        UpdateManager.downloadAndInstall(
            activity,
            updateInfo,
            onProgress = { percent ->
                progressBar.progress = percent
                progressText.text =
                    App.resources.getString(R.string.downloading_update_progress, percent)
            },
            onDone = { file, errorMessage ->
                if (alertDialog.isShowing) {
                    alertDialog.dismiss()
                }
                if (file == null) {
                    // 用户重复点击下载等场景不弹窗
                    if (errorMessage != "downloading") {
                        this.showUpdateDownloadFailedDialog(updateInfo)
                    }
                    return@downloadAndInstall
                }
                if (!UpdateManager.canInstallPackages()) {
                    this.showToast(App.resources.getString(R.string.update_need_install_permission))
                    UpdateManager.openUnknownSourcesSettings()
                    return@downloadAndInstall
                }
                if (!UpdateManager.installApk(activity, file)) {
                    this.showToast(App.resources.getString(R.string.update_install_failed))
                }
            }
        )
    }

    /**
     * 下载失败弹窗，引导用户去浏览器下载
     */
    private fun showUpdateDownloadFailedDialog(updateInfo: UpdateInfo) {
        val activity = App.activityManager.currentActivity ?: return
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        val pageUrl = updateInfo.htmlUrl.ifBlank {
            App.resources.getString(R.string.github_url)
        }
        MaterialAlertDialogBuilder(activity)
            .setTitle(App.resources.getString(R.string.update_download_failed_title))
            .setMessage(App.resources.getString(R.string.update_download_failed_message))
            .setPositiveButton(App.resources.getString(R.string.update_download_in_browser)) { _, _ ->
                try {
                    activity.startActivity(Intent(Intent.ACTION_VIEW, pageUrl.toUri()))
                } catch (e: Exception) {
                    this.showToast(App.resources.getString(R.string.failed_to_invoke_open_method))
                }
            }
            .setNegativeButton(App.resources.getString(R.string.cancel), null)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                show()
            }
    }

    /**
     * 手动检查更新的结果反馈
     */
    fun checkUpdateManually() {
        this.showToast(App.resources.getString(R.string.checking_update))
        UpdateManager.checkUpdate { result ->
            when (result) {
                is UpdateManager.CheckResult.Available -> {
                    this.showUpdateDialog(result.updateInfo)
                }

                is UpdateManager.CheckResult.UpToDate -> {
                    this.showToast(
                        App.resources.getString(
                            R.string.already_up_to_date,
                            UpdateManager.getCurrentVersionName()
                        )
                    )
                }

                is UpdateManager.CheckResult.Failed -> {
                    this.showToast(App.resources.getString(R.string.check_update_failed))
                }
            }
        }
    }

    /**
     * 显示图片详情弹窗
     */
    fun showImageDetailsDialog(imageInfo: ImageInfoEntity?) {
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
                    .setPositiveButton(App.resources.getString(R.string.close)) { dialog: DialogInterface?, _: Int -> dialog?.cancel() }
                    .create()
                alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                alertDialog.show()
                App.alertDialogUtil.disableAutoDismissWhenItemClick(alertDialog) { _, _, i, _ ->
                    App.clipboardUtil.setText(configs[i].second)
                    App.output.showToast(App.resources.getString(R.string.copied))
                }
            }
        }.start()
    }

}