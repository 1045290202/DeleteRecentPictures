package com.sjk.deleterecentpictures

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.github.chrisbanes.photoview.PhotoView
//import com.sjk.deleterecentpictures.ImageActivity.ImageActivityHandlerMsgWhat
import com.sjk.deleterecentpictures.ImageActivity.ViewPagerAdapter.ViewPagerViewHolder
import java.util.*

class ImageActivity : AppCompatActivity() {
    private var maximum = 10
    private val viewPagerAdapter = ViewPagerAdapter()

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (Objects.requireNonNull(ImageActivityHandlerMsgWhat.getByValue(msg.what))) {
                ImageActivityHandlerMsgWhat.NOTIFY_DATA_SET_CHANGED -> {

                    //刷新数据
                    Log.d(TAG, "handleMessage: " + "刷新数据")
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
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val str = sp.getString("numberOfPictures", "10")
        val numberOfPictures: Int = if (str == null || str == "") {
            10
        } else {
            str.toInt()
        }
        this.maximum = numberOfPictures
    }

    private fun init() {
//        List<Bitmap> images = new ArrayList<>();
//        images.add(MainActivity.theLatestImage);

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
            viewPagerAdapter.images = MainActivity.theLatestImages!!
            viewPagerAdapter.imagePaths = it
            viewPagerAdapter.itemCount
            viewPagerAdapter.activity = this
            val viewPager = findViewById<ViewPager2>(R.id.viewPager)
            viewPager.adapter = viewPagerAdapter
            viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    //                Log.d(TAG, "onPageScrolled: " + position);
                    if (position < maximum - 1) {
                        createNewImage(position + 1)
                    }
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == 0) {
                        createNewImage(position)
                    }
                }
            })
        }
    }

    private fun buttonClickEventBind() {

    }

    private fun createNewImage(position: Int) {
        try {
            MainActivity.theLatestImages?.get(position)
        } catch (e: Exception) {
            Thread {
                val bitmap = BitmapFactory.decodeFile(MainActivity.imagePaths?.get(position))
                MainActivity.theLatestImages?.add(bitmap)
                val message = Message()
                message.what = ImageActivityHandlerMsgWhat.NOTIFY_DATA_SET_CHANGED.index
                handler.sendMessage(message)
            }.run()
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
            if (position < images.size) {
                holder.photoView.setImageBitmap(images[position])
            }
            holder.imagePath = imagePaths[position]
        }

        override fun getItemCount(): Int {
            return maximum
        }

        internal inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var photoView: PhotoView = itemView.findViewById(R.id.photoView)
            var imagePath: String? = null

            init {
                photoView.maximumScale = 10f
                photoView.mediumScale = 4f
                photoView.setOnClickListener { activity!!.finish() }
            }
        }
    }

    internal enum class ImageActivityHandlerMsgWhat(val index: Int) {
        //错误
        NOTIFY_DATA_SET_CHANGED(0);

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

    companion object {
        private const val TAG = "ImageActivity"
    }
}