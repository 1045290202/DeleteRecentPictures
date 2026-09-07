package com.sjk.deleterecentpictures.activity.main


import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.BaseActivity
import com.sjk.deleterecentpictures.common.Event
import com.sjk.deleterecentpictures.common.logD


class MainActivity : BaseActivity() {
    private var isLoaded = false
    internal var viewPager: ViewPager2? = null
    internal val viewPagerAdapter = MainActivityViewPagerAdapter()
    private val event: Event = App.newEvent

    internal val viewDelegate = MainViewDelegate(this)
    internal val refreshDelegate = MainRefreshDelegate(this)
    internal val deleteDelegate = MainDeleteDelegate(this)
    internal val permissionDelegate = MainPermissionDelegate(this)

    // 菜单配置
    private val menuConfig = mapOf<Int, () -> Any>(
        R.id.action_refresh to { this.refreshDelegate.refreshWithChecksReset() },
        R.id.action_details to {
            this.getOutput().showImageDetailsDialog(this.getDataSource().getCurrentImageInfo())
        },
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
            this.viewDelegate.initView()
            this.refreshDelegate.bindRefreshButton()
            this.deleteDelegate.bindDeleteButton()
            this.permissionDelegate.requestWritePermission()
        }
        this.viewPagerAdapter.setOnPageClick(this.viewDelegate::onPageClick)
    }

    override fun onRestart() {
        super.onRestart()
        if (viewPager == null) {
            return
        }
        this.viewDelegate.syncCurrentItem()
        // API 30+ 授权后检查
        this.permissionDelegate.checkAfterRestart()
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
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
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
        this.permissionDelegate.onRequestPermissionsResult(requestCode, grantResults)
    }

    /**
     * 刷新当前图片路径按钮的显示
     */
    internal fun refreshCurrentImagePath() {
        val currentPicturePathButton = findViewById<Button>(R.id.currentPicturePathButton)
        currentPicturePathButton.text =
            this.getDataSource().getFileNameByPath(this.getDataSource().getCurrentImageInfo())
                ?: this.getText(R.string.no_path)
    }
}
