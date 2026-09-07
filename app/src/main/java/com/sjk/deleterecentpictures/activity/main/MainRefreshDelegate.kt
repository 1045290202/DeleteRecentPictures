package com.sjk.deleterecentpictures.activity.main


import android.annotation.SuppressLint
import android.widget.Button
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.entity.ImageInfoEntity
import kotlin.math.max


/**
 * MainActivity 的刷新委托，负责最近图片列表的刷新与刷新按钮的事件绑定
 */
class MainRefreshDelegate(private val activity: MainActivity) {

    /**
     * 绑定刷新按钮事件
     */
    fun bindRefreshButton() {
        val refreshButton = this.activity.findViewById<Button>(R.id.refreshButton)
        refreshButton.setOnClickListener {
            this.refreshWithChecksReset()
        }
        refreshButton.setOnLongClickListener {
            this.refreshWithChecksReset(backToFirst = true)
            true
        }
    }

    /**
     * 刷新并清除所有勾选状态
     */
    fun refreshWithChecksReset(backToFirst: Boolean = false) {
        this.refreshAll {
            App.input.setAllImageChecksFalse()
            this.activity.viewPagerAdapter.setAllHolderChecked(false)
            if (backToFirst) {
                this.activity.viewPager?.setCurrentItem(0, true)
            }
            App.output.showToast(
                this.activity.getString(
                    if (backToFirst) R.string.refresh_successful_and_go_back else R.string.refresh_successful
                )
            )
        }
    }

    fun refreshAll(callback: () -> Unit = fun() {}) {
        this.refreshImages(callback)
    }

    /**
     * 刷新图片
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun refreshImages(callback: () -> Unit = fun() {}) {
        Thread {
            App.imageScannerUtil.init(
                this.activity,
                App.dataSource.getSelection(),
                sortOrder = App.dataSource.getSortOrder(),
            )
//        App.recentImages.resetCurrentImagePathIndex()
            App.recentImages.clearImagePaths()

            var i = App.dataSource.getNumberOfPictures()
            val maxI = i
            while (i > 0) {
                val imageInfo: ImageInfoEntity =
                    (if (maxI == i) App.imageScannerUtil.getCurrent() else App.imageScannerUtil.getNext())
                        ?: break
                App.dataSource.getRecentImageInfos().add(imageInfo)
                i--
            }
            // 为每张扫描到的图片补一个默认“未勾选”状态，使勾选列表与图片列表长度一致
            repeat(this.activity.viewPagerAdapter.imageInfos.size) {
                this.activity.viewPagerAdapter.imageChecks.add(false)
            }

            this.activity.runOnUiThread {
                this.activity.viewPagerAdapter.notifyDataSetChanged()
                val currentIndex = App.dataSource.getCurrentImageInfoIndex()
                if (currentIndex >= App.dataSource.getRecentImageInfos().size) {
                    this.activity.viewPager?.setCurrentItem(max(currentIndex - 1, 0), false)
                }
                this.activity.refreshCurrentImagePath()
                callback()
            }
        }.start()
    }
}
