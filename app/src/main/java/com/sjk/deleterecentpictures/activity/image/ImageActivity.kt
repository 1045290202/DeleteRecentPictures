package com.sjk.deleterecentpictures.activity.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.GlideImageViewFactory
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.BaseActivity
import java.util.*

class ImageActivity : BaseActivity() {
    private val viewPagerAdapter = ImageActivityViewPagerAdapter()

    companion object {
        private const val TAG = "ImageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()
        setContentView(R.layout.activity_image)
        init()
    }

    private fun init() {
//        val imagePath: String? = this.getGlobalData("currentImagePath", null) as String?
        viewPagerAdapter.imageInfos = this.getDataSource().getRecentImageInfos()
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = viewPagerAdapter
        viewPager.setCurrentItem(this.getDataSource().getCurrentImageInfoIndex(), false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                this@ImageActivity.getInput().setCurrentImagePathIndex(position)

                if (this@ImageActivity.getDataSource().getRecentImageInfos().size == 0) {
                    this@ImageActivity.getInput().setCurrentImagePathIndex(0)
                    return
                }
            }
        })
    }

    private fun buttonClickEventBind() {

    }


    private fun setFullScreen() {
        window.clearFlags(
            LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        ) //允许页面可以拉伸到顶部状态栏并且定义顶部状态栏透名
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or  //设置全屏显示
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //        window.setStatusBarColor(Color.TRANSPARENT); //设置状态栏为透明
//        window.navigationBarColor = Color.parseColor("#44000000") //设置虚拟键为透明

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // 强制在屏幕安全区域显示内容（刘海屏等等）
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            window.attributes = lp
        }

    }

}
