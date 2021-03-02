package com.sjk.deleterecentpictures.activity.image

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.dialog.MaterialDialogs
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.BaseActivity
import com.sjk.deleterecentpictures.utils.FileUtil
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io.File
import java.lang.Exception
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
        viewPagerAdapter.imagePaths = this.getDataSource().getRecentImagePaths()
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = viewPagerAdapter
        viewPager.setCurrentItem(this.getDataSource().getCurrentImagePathIndex(), false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                this@ImageActivity.getInput().setCurrentImagePathIndex(position)
        
                if (this@ImageActivity.getDataSource().getRecentImagePaths().size == 0) {
                    this@ImageActivity.getInput().setCurrentImagePathIndex(0)
                    return
                }
            }
        })
    }
    
    private fun buttonClickEventBind() {
    
    }
    
    
    private fun setFullScreen() {
        window.clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS or
                LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) //允许页面可以拉伸到顶部状态栏并且定义顶部状态栏透名
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or  //设置全屏显示
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //        window.setStatusBarColor(Color.TRANSPARENT); //设置状态栏为透明
//        window.navigationBarColor = Color.parseColor("#44000000") //设置虚拟键为透明
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) { // 强制在屏幕安全区域显示内容（刘海屏等等）
            val lp = window.attributes;
            lp.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            window.attributes = lp;
        };
        
    }
    
}

internal class ImageActivityViewPagerAdapter : RecyclerView.Adapter<ImageActivityViewPagerAdapter.ViewPagerViewHolder>() {
    private var viewPagerViewHolders: MutableList<ViewPagerViewHolder> = ArrayList()
    var imagePaths: List<String?> = ArrayList()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val viewPagerViewHolder = ViewPagerViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_view_pager_item, parent, false))
        viewPagerViewHolders.add(viewPagerViewHolder)
        return viewPagerViewHolder
    }
    
    override fun onBindViewHolder(holder: ImageActivityViewPagerAdapter.ViewPagerViewHolder, position: Int) {
        holder.imagePath = imagePaths[position]
        if (holder.imagePath == null) {
            holder.gifImageView.visibility = View.GONE
            holder.imageView.visibility = View.GONE
            return
        }
        
        if (!App.fileUtil.existsFile(holder.imagePath)) {
            return
        }
        
        if (position < imagePaths.size) {
            holder.isGif = FileUtil.isGifFile(holder.imagePath)
            if (holder.isGif) {
                holder.gifImageView.visibility = View.VISIBLE
                holder.imageView.visibility = View.GONE
                
                return
            }
            holder.gifImageView.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE
        }
    }
    
    override fun onViewDetachedFromWindow(holder: ImageActivityViewPagerAdapter.ViewPagerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        
        if (!holder.isGif) {
            holder.imageView.recycle()
            return
        }
        
        val gifDrawable = holder.gifImageView.drawable as GifDrawable
        if (gifDrawable.isRecycled) {
            return
        }
        
        gifDrawable.recycle()
    }
    
    override fun onViewAttachedToWindow(holder: ImageActivityViewPagerAdapter.ViewPagerViewHolder) {
        super.onViewAttachedToWindow(holder)

//        if (holder.hasImage){
//            return
//        }
        if (holder.imagePath == null) {
            App.output.showToast("文件路径为空")
            return
        }
        
        if (!App.fileUtil.existsFile(holder.imagePath)) {
            App.output.showToast("文件不存在，可能已被其他软件删除")
            return
        }
        
        if (!holder.isGif) {
            holder.imageView.setImage(ImageSource.uri(holder.imagePath!!))
            return
        }
        
        val gifFromPath = GifDrawable(holder.imagePath!!)
        holder.gifImageView.setImageDrawable(gifFromPath)
        gifFromPath.start()
    }
    
    override fun getItemCount(): Int {
        return this.imagePaths.size
    }
    
    internal inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: SubsamplingScaleImageView = itemView.findViewById(R.id.imageView)
        var gifImageView: GifImageView = itemView.findViewById(R.id.gifImageView)
        var imagePath: String? = null
        var isGif = false
        
        init {
            this.imageView.setOnLongClickListener {
                App.output.showImageLongClickDialog(this.imagePath)
                
                return@setOnLongClickListener true
            }
            this.gifImageView.setOnLongClickListener {
                App.output.showImageLongClickDialog(this.imagePath)
                
                return@setOnLongClickListener true
            }
        }
        
        
    }
    
}