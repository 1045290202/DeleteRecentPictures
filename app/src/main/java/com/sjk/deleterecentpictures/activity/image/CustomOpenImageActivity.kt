package com.sjk.deleterecentpictures.activity.image

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.flyjingfish.openimagelib.OpenImageActivity
import com.flyjingfish.openimagelib.databinding.OpenImageActivityViewpagerBinding
import com.flyjingfish.openimagelib.widget.TouchCloseLayout
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.DataSource
import com.sjk.deleterecentpictures.common.Input
import com.sjk.deleterecentpictures.common.Output

/**
 * 自定义的 OpenImage 查看器活动，功能与 OpenImage 默认活动类
 * （StandardOpenImageActivity）保持一致，可直接通过
 * OpenImage.with(...).setOpenImageActivityCls(...) 启用。
 *
 * OpenImage 要求活动类必须继承 OpenImageActivity，受 Java 单继承限制无法
 * 同时继承 [com.sjk.deleterecentpictures.common.BaseActivity]，因此在此
 * 复刻 BaseActivity 的活动栈管理与便捷访问方法，使行为与继承
 * BaseActivity 的其他活动保持一致。
 */
open class CustomOpenImageActivity : OpenImageActivity() {

    private lateinit var rootBinding: OpenImageActivityViewpagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.activityManager.push(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.activityManager.remove(this)
    }

    protected fun getOutput(): Output {
        return App.output
    }

    protected fun getInput(): Input {
        return App.input
    }

    protected fun getDataSource(): DataSource {
        return App.dataSource
    }

    protected fun getGlobalData(key: String, default: Any?): Any? {
        return App.globalData.getData(key, default)
    }

    protected fun setGlobalData(key: String, value: Any?) {
        App.globalData.setData(key, value)
    }

    protected fun removeGlobalData(key: String) {
        App.globalData.removeData(key)
    }

    override fun getContentView(): View {
        this.rootBinding = OpenImageActivityViewpagerBinding.inflate(this.layoutInflater)
        return this.rootBinding.root
    }

    override fun getBgView(): View {
        return this.rootBinding.vBg
    }

    override fun getTouchCloseLayout(): TouchCloseLayout {
        return this.rootBinding.root
    }

    override fun getViewPager2Container(): FrameLayout {
        return this.rootBinding.flTouchView
    }

    override fun getViewPager2(): ViewPager2 {
        return this.rootBinding.viewPager
    }
}
