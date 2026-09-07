package com.sjk.deleterecentpictures.activity.main


import android.content.Intent
import android.os.Build
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.viewpager2.widget.ViewPager2
import com.flyjingfish.openimagelib.OpenImage
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.activity.image.ImageActivity
import com.sjk.deleterecentpictures.activity.settings.SettingsActivity
import com.sjk.deleterecentpictures.common.App


/**
 * MainActivity 的视图委托，负责视图初始化、ViewPager 交互与部分按钮的事件绑定
 */
class MainViewDelegate(private val activity: MainActivity) {

    /**
     * 初始化视图
     */
    fun initView() {
        val enableMultiWindowLayout =
            App.dataSource.getSP().getBoolean("enableMultiWindowLayout", false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && enableMultiWindowLayout && this.activity.isInMultiWindowMode) {
            this.activity.setTheme(R.style.MultiWindowTheme)
            this.activity.setContentView(R.layout.activity_main_multi_window)
        } else {
            this.activity.setTheme(R.style.DialogTheme)
            this.activity.setContentView(R.layout.activity_main)
        }

        // 聚焦当前活动
        this.activity.window.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        )

        // this.activity.setContentView(R.layout.activity_main)
        this.activity.setTitle(R.string.app_name)
        this.activity.setSupportActionBar(this.activity.findViewById(R.id.toolbar))
        this.bindButtonClickEvent()
        ScrollButtonManager.init(this.activity)

        this.activity.viewPager = this.activity.findViewById(R.id.viewPager)
        this.activity.viewPager!!.adapter = this.activity.viewPagerAdapter
        this.activity.viewPagerAdapter.imageInfos = App.dataSource.getRecentImageInfos()
        this.activity.viewPagerAdapter.imageChecks = App.dataSource.getImageChecks()
        this.activity.viewPager!!.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                App.input.setCurrentImagePathIndex(position)

                if (App.dataSource.getRecentImageInfos().size == 0) {
                    App.input.setCurrentImagePathIndex(0)
                    return
                }

                this@MainViewDelegate.activity.refreshCurrentImagePath()
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun bindButtonClickEvent() {
        val currentPicturePathButton =
            this.activity.findViewById<Button>(R.id.currentPicturePathButton)
        currentPicturePathButton.setOnClickListener {
            App.output.showImageDetailsDialog(App.dataSource.getCurrentImageInfo())
        }
        currentPicturePathButton.setOnLongClickListener {
            App.input.copyCurrentImageName()
            true
        }

        val cancelButton = this.activity.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener { this.activity.finish() }

        val settingsButton = this.activity.findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            // val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, settingsButton, "settings")
            val intent = Intent(this.activity, SettingsActivity::class.java)
            this.activity.startActivityForResult(intent, 1/*, options.toBundle()*/)
        }
    }

    /**
     * 将 ViewPager 同步到当前图片的下标
     */
    fun syncCurrentItem() {
        if (this.activity.viewPager?.currentItem != App.dataSource.getCurrentImageInfoIndex()) {
            this.activity.viewPager?.setCurrentItem(
                App.dataSource.getCurrentImageInfoIndex(),
                false
            )
        }
    }

    fun jumpToNextImage() {
        this.activity.viewPager?.setCurrentItem(
            this.activity.viewPager?.currentItem?.plus(1) ?: 0,
            true
        )
    }

    fun jumpToPreviousImage() {
        this.activity.viewPager?.setCurrentItem(
            this.activity.viewPager?.currentItem?.minus(1) ?: 0,
            true
        )
    }

    fun onPageClick(position: Int) {
        // 空页（“没有更多图片”）不响应点击，防止查看器取图越界闪退
        if (position >= App.dataSource.getRecentImageInfos().size) {
            return
        }
        val enableNewLargerViewer =
            App.dataSource.getSP().getBoolean("enableNewLargerViewer", false)
        if (!enableNewLargerViewer) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this.activity,
                this.activity.findViewById(R.id.imageAnimationView),
                "image"
            )
            val intent = Intent(this.activity, ImageActivity::class.java)
            this.activity.startActivity(intent, options.toBundle())
            return
        }

        OpenImage.with(this.activity)
            .setClickViewPager2(this.activity.viewPager) { _, _ ->
                return@setClickViewPager2 R.id.imageView
            }
            .setSrcImageViewScaleType(ImageView.ScaleType.FIT_CENTER, true)
            .setImageUrlList(App.dataSource.getRecentImageInfos())
            .setAutoScrollScanPosition(true)
            .setClickPosition(position)
//            .setOnItemLongClickListener { baseInnerFragment, openImageUrl, _ ->
//                App.output.showImageLongClickDialog(
//                    baseInnerFragment.activity,
//                    openImageUrl.imageUrl
//                )
//            }
            .show()
    }
}
