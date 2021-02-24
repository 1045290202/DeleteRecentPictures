package com.sjk.deleterecentpictures

//import com.sjk.deleterecentpictures.ImageActivity.ImageActivityHandlerMsgWhat
//import com.zxy.tiny.Tiny
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.sjk.deleterecentpictures.ImageActivity.ViewPagerAdapter.ViewPagerViewHolder
import com.sjk.deleterecentpictures.common.BaseActivity
import com.sjk.deleterecentpictures.utils.FileUtil
import com.sjk.deleterecentpictures.utils.TransformUtil
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.util.*


class ImageActivity : BaseActivity() {
    private var maximum = 10
    private var imagePaths: MutableList<String?>? = null
    private var theLatestImages: MutableList<Bitmap?>? = null
    private val viewPagerAdapter = ViewPagerAdapter()
    
    companion object {
        private const val TAG = "ImageActivity"
    }
    
    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (ImageActivityHandlerMsgWhat.getByValue(msg.what)) {
                ImageActivityHandlerMsgWhat.NOTIFY_DATA_SET_CHANGED -> {
                    //刷新数据
                    Log.d(TAG, "handleMessage: " + "刷新数据")
                    viewPagerAdapter.notifyDataSetChanged()
                }
                ImageActivityHandlerMsgWhat.COMPRESSION_FAILED -> {
                    //刷新数据
                    Log.d(TAG, "handleMessage: " + "压缩失败，使用原图")
                    viewPagerAdapter.notifyDataSetChanged()
                }
                else -> {
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()
        setContentView(R.layout.activity_image)
        init()
    }
    
    private fun initMaximumByPreference() {
        this.maximum = this.getDataSource().getNumberOfPictures()
    }
    
    private fun init() {
//        List<Bitmap> images = new ArrayList<>();
//        images.add(MainActivity.theLatestImage);

//        Tiny.getInstance().init(application);
        this.initList()
        
        this.initMaximumByPreference()
        
        MainActivity.imagePaths?.let {
            if (it.isEmpty()) {
                Toast.makeText(this, "无图片", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            
            /*if (MainActivity.theLatestImages.size() == 0) {
                int l = 1;
                if (MainActivity.imagePaths.size() < 1) {
                    l = MainActivity.imagePaths.size();
                }
                for (int i = 0; i < l; i++) {
                    Bitmap bitmap = BitmapFactory.decodeFile(MainActivity.imagePaths.get(i));
                    MainActivity.theLatestImages.add(bitmap);
                }
            }*/
            
            if (it.size < maximum) {
                maximum = it.size
            }
            Log.d(TAG, "init: $maximum")
            viewPagerAdapter.images = this.theLatestImages!!
            viewPagerAdapter.imagePaths = it
//            viewPagerAdapter.itemCount
            viewPagerAdapter.activity = this
            val viewPager = findViewById<ViewPager2>(R.id.viewPager)
            viewPager.adapter = viewPagerAdapter
            /*viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                private var isInScroll = false
    
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    //                Log.d(TAG, "onPageScrolled: " + position);
                    *//*if (*//**//*position < maximum - 1 && *//**//*positionOffset == 0f && this.isInScroll) {
                        createNewImage(position + 1)
                    }*//*
                }
    
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    this.isInScroll = state == ViewPager2.SCROLL_STATE_DRAGGING
                }
                
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    *//*if (position == 0) {
                        createNewImage(position)
                    }*//*
                }
            })*/
        }
    }
    
    private fun initList() {
        this.theLatestImages = ArrayList()
        this.imagePaths = ArrayList()
    }
    
    private fun buttonClickEventBind() {
    
    }
    
    private fun createNewImage(position: Int) {
        try {
            this.theLatestImages?.get(position)
        } catch (e: Exception) {
            Thread {
//                val bitmap: Bitmap? = TransformUtil.filePath2Bitmap(MainActivity.imagePaths?.get(position))
                val message = Message()
//                this.theLatestImages?.add(position, bitmap)
                message.what = ImageActivityHandlerMsgWhat.COMPRESSION_FAILED.index
                handler.sendMessage(message)
            }.run()
            
            // 开始压缩，防止canvas崩溃
            /*val options = Tiny.BitmapCompressOptions()
            options.config = Bitmap.Config.RGB_565
            options.height = 0
            Tiny.getInstance().source(uncompressedBitmap).asBitmap().withOptions(options).compress { isSucceeded: Boolean, compressedBitmap: Bitmap?, t: Throwable? ->
                val message = Message()
                if (!isSucceeded) {
                    this.theLatestImages?.add(position, uncompressedBitmap)
                    message.what = ImageActivityHandlerMsgWhat.COMPRESSION_FAILED.index
                } else {
                    this.theLatestImages?.add(position, compressedBitmap!!)
                    message.what = ImageActivityHandlerMsgWhat.NOTIFY_DATA_SET_CHANGED.index
                }
                handler.sendMessage(message)
            }*/
        }
    }
    
    //    @Suppress("DEPRECATION")
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
    
    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    val navigationBarHeight: Int
        get() {
            val resources = resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return resources.getDimensionPixelSize(resourceId)
        }
    
    internal inner class ViewPagerAdapter : RecyclerView.Adapter<ViewPagerViewHolder>() {
        private var viewPagerViewHolders: MutableList<ViewPagerViewHolder> = ArrayList()
        var images: List<Bitmap?> = ArrayList()
        var imagePaths: List<String?> = ArrayList()
        var activity: Activity? = null
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
            val viewPagerViewHolder = ViewPagerViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_view_pager_item, parent, false))
            viewPagerViewHolders.add(viewPagerViewHolder)
            return viewPagerViewHolder
        }
        
        override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
            holder.imagePath = imagePaths[position]
            if (position < imagePaths.size) {
                // val img: Bitmap = images[position]!!.copy(Bitmap.Config.RGB_565, true)
                holder.isGif = FileUtil.isGifFile(holder.imagePath!!)
                if (holder.isGif) {
                    holder.gifImageView.visibility = View.VISIBLE
//                    holder.gifSign.visibility = View.VISIBLE
                    holder.photoView.visibility = View.GONE
                    
                    return
                }
                holder.gifImageView.visibility = View.GONE
//                holder.gifSign.visibility = View.GONE
                holder.photoView.visibility = View.VISIBLE
            }
        }
        
        override fun onViewDetachedFromWindow(holder: ViewPagerViewHolder) {
            super.onViewDetachedFromWindow(holder)
            
            if (!holder.isGif) {
                holder.photoView.recycle()
                return
            }
            
            val gifDrawable = holder.gifImageView.drawable as GifDrawable
            if (gifDrawable.isRecycled) {
                return
            }
            
            gifDrawable.recycle()
        }
        
        override fun onViewAttachedToWindow(holder: ViewPagerViewHolder) {
            super.onViewAttachedToWindow(holder)
            
            if (!holder.isGif) {
                holder.photoView.setImage(ImageSource.uri(holder.imagePath!!))
                return
            }
            
            val gifFromPath = GifDrawable(holder.imagePath!!)
            holder.gifImageView.setImageDrawable(gifFromPath)
            gifFromPath.start()
        }
        
        override fun getItemCount(): Int {
            return maximum
        }
        
        internal inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var photoView: SubsamplingScaleImageView = itemView.findViewById(R.id.photoView)
            var gifImageView: GifImageView = itemView.findViewById(R.id.gifImageView)
//            var gifSign: View = itemView.findViewById(R.id.gifSign)
            var imagePath: String? = null
            var isGif = false
            
            init {
//                photoView.maximumScale = 10f
//                photoView.mediumScale = 4f
//                this@ViewPagerViewHolder.gifSign.visibility = View.GONE
//                this@ViewPagerViewHolder.photoView.setOnClickListener { activity!!.finish() }
            }
        }
    }
    
    internal enum class ImageActivityHandlerMsgWhat(val index: Int) {
        NOTIFY_DATA_SET_CHANGED(0),
        COMPRESSION_FAILED(0);
        
        companion object {
            fun getByValue(what: Int): ImageActivityHandlerMsgWhat? {
                for (handlerMsgWhat in values()) {
                    if (handlerMsgWhat.index == what) {
                        return handlerMsgWhat
                    }
                }
                return null
            }
        }
        
    }
}