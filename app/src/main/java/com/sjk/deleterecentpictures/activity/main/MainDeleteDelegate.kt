package com.sjk.deleterecentpictures.activity.main


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App


/**
 * MainActivity 的删除委托，负责删除按钮的事件绑定与图片删除、撤回逻辑
 */
class MainDeleteDelegate(private val activity: MainActivity) {

    /**
     * 绑定删除按钮事件
     */
    fun bindDeleteButton() {
        val deleteButton = this.activity.findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            if (this.hasPicturesChecked()) {
                App.output.showDeleteCheckedImagesDialog(positiveCallback = { _: DialogInterface?, _: Int ->
                    this.deleteCheckedImages {
                        App.input.setAllImageChecksFalse()
                        this.activity.viewPagerAdapter.setAllHolderChecked(false)
                    }
                })
                return@setOnClickListener
            }

            val deleteDirectly = App.dataSource.getSP().getBoolean("deleteDirectly", false)
            if (deleteDirectly) {
                this.deleteCurrentImage {
                    App.input.setAllImageChecksFalse()
                    this.activity.viewPagerAdapter.setAllHolderChecked(false)
                }
            } else {
                App.output.showDeleteCurrentImageDialog(positiveCallback = { _: DialogInterface?, _: Int ->
                    this.deleteCurrentImage {
                        App.input.setAllImageChecksFalse()
                        this.activity.viewPagerAdapter.setAllHolderChecked(false)
                    }
                })
            }
        }
        deleteButton.setOnLongClickListener {
            if (this.hasPicturesChecked()) {
                this.deleteCheckedImages {
                    this.activity.finish()
                }
                return@setOnLongClickListener true
            }
            this.deleteCurrentImage(false) {
                this.activity.finish()
            }
            true
        }
    }

    private fun hasPicturesChecked(): Boolean {
        return App.dataSource.getAllCheckedImageInfos().size != 0
    }

    private fun deleteCheckedImages(
        needToRefresh: Boolean = true,
        callback: () -> Unit = fun() {},
    ) {
        val deleteButton: Button = this.activity.findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false

        Thread {
            var allDeleted = true
            var someDeleted = false
            val allCheckedImageInfos = App.dataSource.getAllCheckedImageInfos()
            for (imageInfo in allCheckedImageInfos) {
                // 删除图片并判断
                if (App.fileUtil.deleteImage(imageInfo)) {
                    someDeleted = true
                } else {
                    allDeleted = false
                }
            }
            this.activity.runOnUiThread {
                when {
                    allDeleted -> {
                        App.output.showToast(this.activity.getString(R.string.all_deleted))
                    }

                    someDeleted -> {
                        App.output.showToast(this.activity.getString(R.string.partial_deletion_failed))
                    }

                    else -> {
                        App.output.showToast(this.activity.getString(R.string.failed_to_delete_all))
                    }
                }

                if (App.dataSource.getSP().getBoolean("closeApp", true)) {
                    deleteButton.isEnabled = true
                    callback()
                    this.activity.finish()
                    return@runOnUiThread
                }

                if (needToRefresh) {
                    this.activity.refreshDelegate.refreshAll {
                        this.activity.refreshCurrentImagePath()
                    }
                }

                deleteButton.isEnabled = true
                callback()
            }
        }.start()
    }

    /**
     * 显示已删除的Toast
     */
    private fun showDeletedToast() {
        App.output.showToast(
            this.activity.getString(
                R.string.successfully_deleted,
                App.dataSource.getCurrentImageInfo()!!.path
            )
        )
    }

    /**
     * 显示已删除的SnackBar
     */
    @SuppressLint("RestrictedApi", "MissingInflatedId", "InflateParams")
    private fun showDeletedSnackBar() {
        App.output.showSnackBarIndefinite(
            this.activity.findViewById(R.id.viewPager),
            this.activity.getString(
                R.string.successfully_deleted,
                // 由于上面刷新了媒体信息，所以这里只能从回收站拿到刚才删掉的媒体信息
                App.dataSource
                    .getFileNameByPath(App.recycleBinManager.deletedImageInfo?.info?.path)
            ) + "  ",
        ) {
            val sb = it
            it.anchorView = this.activity.findViewById<View>(R.id.viewPagerOverlay)

            val snackbarTextView =
                it.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            snackbarTextView.isSingleLine = true
            snackbarTextView.maxWidth = 0 // maxWidth设置成0可以，防止snackbar占满屏幕的宽度，原因未知
            snackbarTextView.ellipsize = TextUtils.TruncateAt.MIDDLE
            // 设置layoutWeight为1
            snackbarTextView.layoutParams = snackbarTextView.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight = 1f
            }
            snackbarTextView.setOnClickListener {
                App.output.showToast("${App.recycleBinManager.deletedImageInfo?.info?.path}")
            }

            val snackbarContentLayout = it.view as Snackbar.SnackbarLayout
            val viewGroup = snackbarContentLayout.getChildAt(0) as ViewGroup
            val customView = this.activity.layoutInflater.inflate(
                R.layout.layout_deleted_snackbar_buttons,
                null
            )
            viewGroup.addView(customView)

            val closeButton = customView.findViewById<Button>(R.id.closeButton)
            closeButton.setOnClickListener {
                Thread {
                    App.recycleBinManager.deleteOldImageInRecycleBin()
                    this.activity.runOnUiThread {
                        sb.dismiss()
                    }
                }.start()
            }

            val revokeButton = customView.findViewById<Button>(R.id.revokeButton)
            revokeButton.setOnClickListener {
                // 撤回操作
                Thread {
                    App.recycleBinManager.recover(onSuccess = { _, _ ->
                        this.activity.refreshDelegate.refreshAll {
                            this.activity.refreshCurrentImagePath()
                            sb.dismiss()
                        }
                    })
                }.start()
            }
        }
    }

    /**
     * 删除图片，返回成功或者失败
     */
    private fun deleteImage(undelete: Boolean): Boolean {
        val deleted: Boolean
        if (undelete) { // 可撤销的时候移动到回收站
            deleted =
                App.recycleBinManager.moveToRecycleBin(App.dataSource.getCurrentImageInfo())
            if (deleted) {
                App.fileUtil.deleteImage(App.dataSource.getCurrentImageInfo())
            }
        } else {
            deleted = App.fileUtil.deleteImage(App.dataSource.getCurrentImageInfo())
        }
        return deleted
    }

    private fun deleteCurrentImage(needToRefresh: Boolean = true, callback: (() -> Unit)?) {
        if (App.dataSource.getCurrentImageInfo()?.uri == null) {
            App.output
                .showToast(this.activity.getString(R.string.delete_failed_because_no_information))
            return
        }

        val deleteButton: Button = this.activity.findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false

        Thread {
            val undelete = App.dataSource.getSP().getBoolean("undelete", false)
            // 删除图片并判断
            val deleted: Boolean = this.deleteImage(undelete)
            this.activity.runOnUiThread {
                if (!deleted) {
                    deleteButton.isEnabled = true
                    return@runOnUiThread
                }

                if (!undelete) {
                    this.showDeletedToast()
                }

                if (App.dataSource.getSP().getBoolean("closeApp", true)) {
                    deleteButton.isEnabled = true
                    callback?.invoke()
                    this.activity.finish()
                    return@runOnUiThread
                }
                if (needToRefresh) {
                    this.activity.refreshDelegate.refreshAll {
                        this.activity.refreshCurrentImagePath()
                    }

                    if (undelete) {
                        this.showDeletedSnackBar()
                    }
                }
                deleteButton.isEnabled = true
                callback?.invoke()
            }
        }.start()
    }
}
