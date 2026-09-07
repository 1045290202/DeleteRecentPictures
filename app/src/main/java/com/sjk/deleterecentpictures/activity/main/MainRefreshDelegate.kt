package com.sjk.deleterecentpictures.activity.main


import android.annotation.SuppressLint
import android.widget.Button
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App
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
            // 懒加载：列表仅持有游标视图，翻到哪一页才查询哪一张，不再一次性物化所有图片
            App.recentImages.resetImageInfos(App.dataSource.getNumberOfPictures())
            App.recentImages.resetImageChecks(App.dataSource.getRecentImageInfos().size)
            this.activity.viewPagerAdapter.imageInfos = App.dataSource.getRecentImageInfos()

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
