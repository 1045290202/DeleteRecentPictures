package com.sjk.deleterecentpictures.activity.image

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.BaseActivity

class ImageActivity : BaseActivity() {
    private val viewPagerAdapter = ImageActivityViewPagerAdapter()
    private lateinit var viewPager: ViewPager2

    companion object {
        private const val TAG = "ImageActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        this.setFullScreen()
        this.setContentView(R.layout.activity_image)
        
        this.init()
        
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@ImageActivity.viewPagerAdapter.resetImageScaleWithAnimation(this@ImageActivity.viewPager)
                this@ImageActivity.supportFinishAfterTransition()
            }
        })
    }

    private fun init() {
//        val imagePath: String? = this.getGlobalData("currentImagePath", null) as String?
        this.viewPagerAdapter.imageInfos = this.getDataSource().getRecentImageInfos()
        this.viewPager = this.findViewById(R.id.viewPager)
        this.viewPager.adapter = viewPagerAdapter
        this.viewPager.setCurrentItem(this.getDataSource().getCurrentImageInfoIndex(), false)
        this.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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

    private fun setFullScreen() {
        this.window.clearFlags(
            LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        ) // 允许页面可以拉伸到顶部状态栏并且定义顶部状态栏透名
        this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or  // 设置全屏显示
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        this.window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //        window.setStatusBarColor(Color.TRANSPARENT); //设置状态栏为透明
//        window.navigationBarColor = Color.parseColor("#44000000") //设置虚拟键为透明

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // 强制在屏幕安全区域显示内容（刘海屏等等）
            val lp = this.window.attributes
            lp.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            this.window.attributes = lp
        }

    }
}
