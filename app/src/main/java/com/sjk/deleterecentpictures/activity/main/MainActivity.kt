package com.sjk.deleterecentpictures.activity.main


import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.activity.settings.SettingsActivity
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.BaseActivity
import com.sjk.deleterecentpictures.common.Event
import com.sjk.deleterecentpictures.common.logD
import com.sjk.deleterecentpictures.common.logW
import com.sjk.deleterecentpictures.utils.PermissionUtil.checkPermissionGranted
import com.sjk.deleterecentpictures.utils.PermissionUtil.requestPermission
import kotlin.math.max


class MainActivity : BaseActivity() {
    private var isLoaded = false
    private var viewPager: ViewPager2? = null
    private val viewPagerAdapter = MainActivityViewPagerAdapter(this)
    private val event: Event = App.newEvent

    //    private var viewPagerCurrentPosition = 0
    /**
     * API 30+ 情况下，是否正跳转至所有文件授权
     */
    private var jumpedForAllFilesPermission = false

    // 菜单配置
    private val menuConfig = mapOf<Int, () -> Any>(
        R.id.action_refresh to { this.onMenuItemActionRefreshClick() },
        R.id.action_details to { this.getOutput().showImageDetailsDialog(this.getDataSource().getCurrentImageInfo()) },
    )

    companion object {
        private const val TAG = "MainActivity"

        //    public static Bitmap theLatestImage;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.getOutput().tryShowPrivacyPolicyDialog {
            // 设置默认偏好
            PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
            this.initView()
            this.requestWritePermission()
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (viewPager == null) {
            return
        }
        if (this.viewPager?.currentItem != App.dataSource.getCurrentImageInfoIndex()) {
            this.viewPager?.setCurrentItem(App.dataSource.getCurrentImageInfoIndex(), false)
        }
        // API 30+ 授权后检查
        if (jumpedForAllFilesPermission) {
            if (checkPermissionGranted()) {
                this.jumpedForAllFilesPermission = false
                this.refreshAll()
            } else {
                this.getOutput()
                    .showToast(this.getString(R.string.not_getting_manage_external_storage_permission))
                App.activityManager.finishAll()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.recycleBinManager.deleteOldImageInRecycleBin()
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        }
        this.recreate()
    }

    override fun finish() {
        super.finish()
        App.recentImages.clearImagePaths()
        App.recentImages.clearImageChecks()
        App.imageScannerUtil.close()
        // Thread {
        //     App.fileUtil.clearCacheFolder()
        // }.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!this.menuConfig.containsKey(item.itemId)) {
            return super.onOptionsItemSelected(item)
        }
        return this.menuConfig[item.itemId]?.invoke() != false
    }

    private fun onMenuItemActionRefreshClick() {
        this.refreshAll {
            App.input.setAllImageChecksFalse()
            this.viewPagerAdapter.setAllHolderChecked(false)
            App.output.showToast(this.getString(R.string.refresh_successful))
        }
    }

    private fun onMenuItemActionRefreshLongClick() {
        this.refreshAll {
            App.input.setAllImageChecksFalse()
            this.viewPagerAdapter.setAllHolderChecked(false)
            this.viewPager?.setCurrentItem(0, true)
            App.output.showToast(this.getString(R.string.refresh_successful_and_go_back))
        }
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        val enableMultiWindowLayout = this.getDataSource().getSP().getBoolean("enableMultiWindowLayout", false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && enableMultiWindowLayout && this.isInMultiWindowMode) {
            this.setTheme(R.style.MultiWindowTheme)
            this.setContentView(R.layout.activity_main_multi_window)
        } else {
            this.setTheme(R.style.DialogTheme)
            this.setContentView(R.layout.activity_main)
        }

        // 聚焦当前活动
        this.window.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        )

        // this.setContentView(R.layout.activity_main)
        this.setTitle(R.string.app_name)
        this.setSupportActionBar(this.findViewById(R.id.toolbar))
        this.buttonClickEventBind()
        ScrollButtonManager.init(this)

        this.viewPager = this.findViewById(R.id.viewPager)
        this.viewPager!!.adapter = this.viewPagerAdapter
        this.viewPagerAdapter.imageInfos = App.dataSource.getRecentImageInfos()
        this.viewPagerAdapter.imageChecks = App.dataSource.getImageChecks()
        this.viewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                App.input.setCurrentImagePathIndex(position)

                if (App.dataSource.getRecentImageInfos().size == 0) {
                    App.input.setCurrentImagePathIndex(0)
                    return
                }

                this@MainActivity.refreshCurrentImagePath()
            }
        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (this.isLoaded) {
            return
        }

        isLoaded = true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        logD(TAG, "onActivityResult: $requestCode $requestCode $data")
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getBooleanExtra("preferenceChanged", false)
            if (result) {
                this.startActivity(Intent(this@MainActivity, MainActivity::class.java))
                this.finish()
                this.getOutput().showToast(this.getString(R.string.settings_reloaded))
            }
        }/* else {
//            Toast.makeText(this, "无返回值", Toast.LENGTH_SHORT).show();
        }*/
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // API 29- 授权后检查
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                this.getOutput().showToast(this.getString(R.string.storage_permission_missing))
                logW(TAG, "Storage permission not obtained")
                App.activityManager.finishAll()
                return
            }
        }

        this.refreshAll()
    }

    private fun requestWritePermission() {
        if (!checkPermissionGranted()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.permission_request_title))
                .setMessage(getString(R.string.permission_request_hint))
                .setPositiveButton(R.string.ok) {_,_->
                    // API 30+ 的授权流程需要此flag
                    jumpedForAllFilesPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    requestPermission()
                }
                .setNegativeButton(R.string.cancel) {_,_->
                    App.activityManager.finishAll()
                }
                .show()
        } else {
            this.refreshAll()
        }
    }

    private fun buttonClickEventBind() {
        val currentPicturePathButton = findViewById<Button>(R.id.currentPicturePathButton)
        currentPicturePathButton.setOnClickListener {
            App.output.showImageDetailsDialog(App.dataSource.getCurrentImageInfo())
        }
        currentPicturePathButton.setOnLongClickListener {
            App.input.copyCurrentImageName()
            true
        }

        val refreshButton = this.findViewById<Button>(R.id.refreshButton)
        refreshButton.setOnClickListener {
            this.refreshAll {
                App.input.setAllImageChecksFalse()
                this.viewPagerAdapter.setAllHolderChecked(false)
                App.output.showToast(this.getString(R.string.refresh_successful))
            }
        }
        refreshButton.setOnLongClickListener {
            this.refreshAll {
                App.input.setAllImageChecksFalse()
                this.viewPagerAdapter.setAllHolderChecked(false)
                this.viewPager?.setCurrentItem(0, true)
                App.output.showToast(this.getString(R.string.refresh_successful_and_go_back))
            }
            true
        }

        val cancelButton = this.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener { this.finish() }

        // 删除按钮
        val deleteButton = this.findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            if (this.hasPicturesChecked()) {
                App.output.showDeleteCheckedImagesDialog(positiveCallback = { dialogInterface: DialogInterface?, witch: Int ->
                    this.deleteCheckedImages {
                        App.input.setAllImageChecksFalse()
                        this.viewPagerAdapter.setAllHolderChecked(false)
                    }
                })
                return@setOnClickListener
            }

            val deleteDirectly = this.getDataSource().getSP().getBoolean("deleteDirectly", false)
            if (deleteDirectly) {
                this.deleteCurrentImage {
                    App.input.setAllImageChecksFalse()
                    this.viewPagerAdapter.setAllHolderChecked(false)
                }
            } else {
                App.output.showDeleteCurrentImageDialog(positiveCallback = { dialogInterface: DialogInterface?, witch: Int ->
                    this.deleteCurrentImage {
                        App.input.setAllImageChecksFalse()
                        this.viewPagerAdapter.setAllHolderChecked(false)
                    }
                })
            }
        }
        deleteButton.setOnLongClickListener {
            if (this.hasPicturesChecked()) {
                this.deleteCheckedImages {
                    this.finish()
                }
                return@setOnLongClickListener true
            }
            this.deleteCurrentImage(false) {
                this.finish()
            }
            true
        }

        val settingsButton = this.findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            // val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, settingsButton, "settings")
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            this.startActivityForResult(intent, 1/*, options.toBundle()*/)
        }
    }

    private fun hasPicturesChecked(): Boolean {
        return App.dataSource.getAllCheckedImageInfos().size != 0
    }

    private fun deleteCheckedImages(
        needToRefresh: Boolean = true,
        callback: () -> Unit = fun() {},
    ) {
        val deleteButton: Button = findViewById(R.id.deleteButton)
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
            this.runOnUiThread {
                when {
                    allDeleted -> {
                        App.output.showToast(this.getString(R.string.all_deleted))
                    }

                    someDeleted -> {
                        App.output.showToast(this.getString(R.string.partial_deletion_failed))
                    }

                    else -> {
                        App.output.showToast(this.getString(R.string.failed_to_delete_all))
                    }
                }

                if (App.dataSource.getSP().getBoolean("closeApp", true)) {
                    deleteButton.isEnabled = true
                    callback()
                    this.finish()
                    return@runOnUiThread
                }

                if (needToRefresh) {
                    this.refreshAll {
                        this.refreshCurrentImagePath()
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
            this.getString(
                R.string.successfully_deleted,
                this.getDataSource().getCurrentImageInfo()!!.path
            )
        )
    }

    /**
     * 显示已删除的SnackBar
     */
    @SuppressLint("RestrictedApi", "MissingInflatedId", "InflateParams")
    private fun showDeletedSnackBar() {
        this.getOutput().showSnackBarIndefinite(
            this.findViewById(R.id.viewPager),
            this.getString(
                R.string.successfully_deleted,
                // 由于上面刷新了媒体信息，所以这里只能从回收站拿到刚才删掉的媒体信息
                this.getDataSource()
                    .getFileNameByPath(App.recycleBinManager.deletedImageInfo?.info?.path)
            ) + "  ",
        ) {
            val sb = it
            it.setAnchorView(this.findViewById<View>(R.id.viewPagerOverlay))

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
                this@MainActivity.getOutput().showToast("${App.recycleBinManager.deletedImageInfo?.info?.path}")
            }

            val snackbarContentLayout = it.view as Snackbar.SnackbarLayout
            val viewGroup = snackbarContentLayout.getChildAt(0) as ViewGroup
            val customView = this@MainActivity.layoutInflater.inflate(
                R.layout.layout_deleted_snackbar_buttons,
                null
            )
            viewGroup.addView(customView)

            val closeButton = customView.findViewById<Button>(R.id.closeButton)
            closeButton.setOnClickListener {
                Thread {
                    App.recycleBinManager.deleteOldImageInRecycleBin()
                    this@MainActivity.runOnUiThread {
                        sb.dismiss()
                    }
                }.start()
            }

            val revokeButton = customView.findViewById<Button>(R.id.revokeButton)
            revokeButton.setOnClickListener {
                // 撤回操作
                Thread {
                    App.recycleBinManager.recover(onSuccess = { _, _ ->
                        this@MainActivity.refreshAll {
                            this@MainActivity.refreshCurrentImagePath()
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
        if (this.getDataSource().getCurrentImageInfo()?.uri == null) {
            this.getOutput()
                .showToast(this.getString(R.string.delete_failed_because_no_information))
            return
        }

        val deleteButton: Button = this.findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false

        Thread {
            val undelete = this.getDataSource().getSP().getBoolean("undelete", false)
            // 删除图片并判断
            val deleted: Boolean = this.deleteImage(undelete)
            this.runOnUiThread {
                if (!deleted) {
                    deleteButton.isEnabled = true
                    return@runOnUiThread
                }

                if (!undelete) {
                    showDeletedToast()
                }

                if (App.dataSource.getSP().getBoolean("closeApp", true)) {
                    deleteButton.isEnabled = true
                    callback?.invoke()
                    this.finish()
                    return@runOnUiThread
                }
                if (needToRefresh) {
                    this.refreshAll {
                        this.refreshCurrentImagePath()
                    }

                    if (undelete) {
                        showDeletedSnackBar()
                    }
                }
                deleteButton.isEnabled = true
                callback?.invoke()
            }
        }.start()
    }

    private fun refreshAll(callback: () -> Unit = fun() {}) {
        this.refreshImages(callback)
    }

    private fun refreshCurrentImagePath() {
        val currentPicturePathButton = findViewById<Button>(R.id.currentPicturePathButton)
        currentPicturePathButton.text =
            this.getDataSource().getFileNameByPath(this.getDataSource().getCurrentImageInfo())
                ?: this.getText(R.string.no_path)
    }

    /**
     * 刷新图片
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun refreshImages(callback: () -> Unit = fun() {}) {
        Thread {
            App.imageScannerUtil.init(
                this,
                this.getDataSource().getSelection(),
                sortOrder = this.getDataSource().getSortOrder(),
            )
//        App.recentImages.resetCurrentImagePathIndex()
            App.recentImages.clearImagePaths()

            var i = this.getDataSource().getNumberOfPictures()
            val maxI = i
            while (i > 0) {
                val imageInfo: ImageInfoBean =
                    (if (maxI == i) App.imageScannerUtil.getCurrent() else App.imageScannerUtil.getNext()) ?: break
                this.getDataSource().getRecentImageInfos().add(imageInfo)
                i--
            }
            for (index in this.viewPagerAdapter.imageInfos.indices) {
                this.viewPagerAdapter.imageChecks.add(false)
            }

            this.runOnUiThread {
                this.viewPagerAdapter.notifyDataSetChanged()
                val currentIndex = this.getDataSource().getCurrentImageInfoIndex()
                if (currentIndex >= this.getDataSource().getRecentImageInfos().size) {
                    this.viewPager?.setCurrentItem(max(currentIndex - 1, 0), false)
                }
                this.refreshCurrentImagePath()
                callback()
            }
        }.start()
    }

    fun jumpToNextImage() {
        this.viewPager?.setCurrentItem(this.viewPager?.currentItem?.plus(1) ?: 0, true)
    }

    fun jumpToPreviousImage() {
        this.viewPager?.setCurrentItem(this.viewPager?.currentItem?.minus(1) ?: 0, true)
    }

}
