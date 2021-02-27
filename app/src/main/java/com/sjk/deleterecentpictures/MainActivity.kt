package com.sjk.deleterecentpictures


import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjk.deleterecentpictures.common.*
import com.sjk.deleterecentpictures.utils.FileUtil
import com.sjk.deleterecentpictures.utils.ImageScannerUtil
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.util.*


open class MainActivity : BaseActivity() {
    
    private var isLoaded = false
    private lateinit var viewPager: ViewPager2
    private val viewPagerAdapter = MainActivityViewPagerAdapter()
    private var currentImagePath: String? = null
    private val event: Event = App.newEvent
    private var viewPagerCurrentPosition = 0
    
    companion object {
        private const val TAG = "MainActivity"
        
        //    public static Bitmap theLatestImage;
        private val PERMISSIONS_STORAGE = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        //设置默认偏好
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        
        initView()
        requestWritePermission()
    }
    
    private fun initView() {
        setContentView(R.layout.activity_main)
        buttonClickEventBind()
        
        this.viewPager = findViewById(R.id.viewPager)
        this.viewPager.adapter = viewPagerAdapter
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (this.isLoaded) {
            return
        }
        
        isLoaded = true
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: $requestCode $requestCode $data")
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getBooleanExtra("preferenceChanged", false)
            if (result) {
                startActivity(Intent(this@MainActivity, MainActivity::class.java))
                finish()
                this.getOutPut().showToast("已重新加载设置")
            }
        }/* else {
//            Toast.makeText(this, "无返回值", Toast.LENGTH_SHORT).show();
        }*/
        super.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                this.getOutPut().showToast("未获取到存储权限")
                logW(TAG, "Storage permission not obtained")
                finish()
                return
            }
        }
        
        this.refreshAll()
    }
    
    private fun requestWritePermission() {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 1)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PERMISSIONS_STORAGE.iterator().forEach {
                if (checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 0)
                    return
                }
            }
            this.refreshAll()
            return
        }
        
        this.refreshAll()
    }
    
    private fun buttonClickEventBind() {
        val latestPicturePathButton = findViewById<Button>(R.id.latestPicturePathButton)
        latestPicturePathButton.setOnClickListener {
            if (this.currentImagePath == null) {
                return@setOnClickListener
            }
            this.getOutPut().showToastLong("完整路径：${this.currentImagePath}")
        }
        latestPicturePathButton.setOnLongClickListener {
            if (this.currentImagePath == null) {
                this.getOutPut().showToast("无路径")
                return@setOnLongClickListener true
            }
            
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(resources.getString(R.string.app_name), this.currentImagePath)
            clipboard.setPrimaryClip(clip)
            this.getOutPut().showToast("${this.currentImagePath}已复制到剪切板")
            true
        }
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        refreshButton.setOnClickListener {
            this.refreshAll()
            this.getOutPut().showToast("刷新成功")
        }
        /*val openImageActivityButton = findViewById<Button>(R.id.openImageActivityButton)
        openImageActivityButton.setOnClickListener {
            //打开图片查看界面
            val intent = Intent(applicationContext, ImageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }*/
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener { finish() }
        
        //删除按钮
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            
            val deleteDirectly = this.getDataSource().getSP().getBoolean("deleteDirectly", false)
            if (deleteDirectly) {
                deleteCurrentImage()
            } else {
                MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("提示")
                        .setMessage("请确认是否删除\n${this.currentImagePath}")
                        .setPositiveButton("确定") { _: DialogInterface?, _: Int -> deleteCurrentImage() }
                        .setNegativeButton("取消") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                        .show()
            }
        }
        deleteButton.setOnLongClickListener {
            deleteCurrentImage(false)
            finish()
            true
        }
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity2::class.java)
            startActivityForResult(intent, 1)
        }
    }
    
    private fun deleteCurrentImage(needToRefresh: Boolean = true) {
        if (this.currentImagePath == null || this.currentImagePath == "") {
            this.getOutPut().showToast("没有获取到图片路径，删除失败")
            return
        }
        
        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false
        
        val scanType: String = this.getDataSource().getSP().getString("scanType", "1")!!
        
        if (!App.fileUtil.existsFile(this.currentImagePath)) {
            this.getOutPut().showToast("文件不存在，删除失败")
            ImageScannerUtil.refreshMediaLibraryByPath(applicationContext, this.currentImagePath, scanType.toInt()) { path: String, uri: Uri? ->
                // 里面是线程回调，这里面不能刷新UI，除非放到UI线程里执行
                this.runOnUiThread {
                    this.refreshImages()
                    deleteButton.isEnabled = true
                }
            }
            return
        }
        
        //删除图片并判断
        if (FileUtil.deleteFile(this.currentImagePath)) {
            this.getOutPut().showToast("${this.currentImagePath}删除成功")
        } else {
            this.getOutPut().showToast("文件无法删除，请确认是否已给予存储权限")
        }
        ImageScannerUtil.refreshMediaLibraryByPath(applicationContext, this.currentImagePath, scanType.toInt()) { path: String, uri: Uri? ->
            // 里面是线程回调，这里面不能刷新UI，除非放到UI线程里执行
            this.runOnUiThread {
                if (this.getDataSource().getSP().getBoolean("closeApp", true)) {
                    this.finish()
                    return@runOnUiThread
                }
                if (needToRefresh) {
                    this.refreshImages()
                    this.refreshCurrentImagePath()
                }
                deleteButton.isEnabled = true
            }
        }
    }
    
    private fun refreshAll() {
        Thread {
            this.refreshImages()
            this.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    this@MainActivity.viewPagerCurrentPosition = position
        
        
                    if (viewPagerAdapter.imagePaths.size == 0) {
                        this@MainActivity.currentImagePath = null
                        return
                    }
        
                    this@MainActivity.refreshCurrentImagePath()
                }
            })
        }.start()
    }
    
    private fun refreshCurrentImagePath() {
        this@MainActivity.currentImagePath = this.viewPagerAdapter.imagePaths[this.viewPagerCurrentPosition]
        val latestPicturePathButton = findViewById<Button>(R.id.latestPicturePathButton)
        latestPicturePathButton.text = App.dataSource.getSimplifiedPathInExternalStorage(this@MainActivity.currentImagePath)
    }
    
    private fun refreshImages() {
        ImageScannerUtil.init(this, this.getDataSource().getSelection())
        this.viewPagerAdapter.imagePaths = arrayListOf()
        
        var i = this.getDataSource().getNumberOfPictures()
        while (i > 0) {
            val imagePath = App.imageScannerUtil.getNext() ?: break
            if (!App.fileUtil.existsFile(imagePath)) {
                continue
            }
            this.viewPagerAdapter.imagePaths.add(imagePath)
            i--
        }
        /*for (i in 0..this.getDataSource().getNumberOfPictures()) {
            val imagePath = App.imageScannerUtil.getNext() ?: break
            this.viewPagerAdapter.imagePaths.add(imagePath)
        }*/
        
        if (this.viewPagerAdapter.imagePaths.size == 0) {
//            this.getOutPut().showToast("未发现图片")
            val latestPicturePathButton = findViewById<Button>(R.id.latestPicturePathButton)
            latestPicturePathButton.text = "未发现图片"
        }
//        this.viewPager.adapter = viewPagerAdapter
        this.runOnUiThread {
            this.viewPagerAdapter.notifyDataSetChanged()
            this.refreshCurrentImagePath()
        }
    }
}

internal class MainActivityViewPagerAdapter : RecyclerView.Adapter<MainActivityViewPagerAdapter.ViewPagerViewHolder>() {
    private var viewPagerViewHolders: MutableList<ViewPagerViewHolder> = ArrayList()
    var imagePaths: MutableList<String?> = arrayListOf()
    val event: Event = App.newEvent
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val viewPagerViewHolder = ViewPagerViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_main_view_pager_item, parent, false))
        viewPagerViewHolders.add(viewPagerViewHolder)
        return viewPagerViewHolder
    }
    
    fun add(index: Int, path: String?) {
        imagePaths.add(index, path)
//        this.notifyDataSetChanged()
    }
    
    fun addLast(path: String?) {
        imagePaths.add(path)
//        this.notifyDataSetChanged()
    }
    
    fun addFirst(path: String?) {
        imagePaths.add(0, path)
//        this.notifyDataSetChanged()
    }
    
    fun remove(index: Int) {
        imagePaths.removeAt(index)
//        this.notifyDataSetChanged()
    }
    
    fun removeLast() {
        imagePaths.removeAt(imagePaths.size - 1)
//        this.notifyDataSetChanged()
    }
    
    fun removeFirst() {
        imagePaths.removeAt(0)
//        this.notifyDataSetChanged()
    }
    
    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.imagePath = imagePaths[position]
        if (holder.imagePath == null) {
            holder.latestPictureGifImageView.visibility = View.GONE
            holder.gifSign.visibility = View.GONE
            holder.latestPictureImageView.visibility = View.GONE
            return
        }
        
        if (!App.fileUtil.existsFile(holder.imagePath)) {
            return
        }
        
        if (position < imagePaths.size) {
            holder.isGif = FileUtil.isGifFile(holder.imagePath)
            if (holder.isGif) {
                holder.latestPictureGifImageView.visibility = View.VISIBLE
                holder.gifSign.visibility = View.VISIBLE
                holder.latestPictureImageView.visibility = View.GONE
                
                return
            }
            holder.latestPictureGifImageView.visibility = View.GONE
            holder.gifSign.visibility = View.GONE
            holder.latestPictureImageView.visibility = View.VISIBLE
        }
    }
    
    override fun onViewDetachedFromWindow(holder: ViewPagerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        
        if (!holder.isGif) {
            holder.latestPictureImageView.recycle()
            return
        }
        
        val gifDrawable = holder.latestPictureGifImageView.drawable as GifDrawable
        if (gifDrawable.isRecycled) {
            return
        }
        
        gifDrawable.recycle()
    }
    
    override fun onViewAttachedToWindow(holder: ViewPagerViewHolder) {
        super.onViewAttachedToWindow(holder)

//        if (holder.hasImage){
//            return
//        }
        if (holder.imagePath == null) {
            App.outPut.showToast("文件路径为空")
            return
        }
        
        if (!App.fileUtil.existsFile(holder.imagePath)) {
            App.outPut.showToast("文件不存在，可能已被其他软件删除")
            return
        }
        
        if (!holder.isGif) {
            holder.latestPictureImageView.setImage(ImageSource.uri(holder.imagePath!!))
            return
        }
        
        val gifFromPath = GifDrawable(holder.imagePath!!)
        holder.latestPictureGifImageView.setImageDrawable(gifFromPath)
        gifFromPath.start()
    }
    
    override fun getItemCount(): Int {
        return this.imagePaths.size
    }
    
    internal inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var latestPictureImageView: SubsamplingScaleImageView = itemView.findViewById(R.id.latestPictureImageView)
        var latestPictureGifImageView: GifImageView = itemView.findViewById(R.id.latestPictureGifImageView)
        var gifSign: View = itemView.findViewById(R.id.gifSign)
        var imagePath: String? = null
        var isGif = false
        
        init {
            val openImageActivityButton = itemView.findViewById<Button>(R.id.openImageActivityButton)
            openImageActivityButton.setOnClickListener {
                //打开图片查看界面
                if (!App.fileUtil.existsFile(this.imagePath)) {
                    App.outPut.showToast("图片无法查看")
                    return@setOnClickListener
                }
                
                App.globalData.setData("currentImagePath", this.imagePath)
                val intent = Intent(itemView.context, ImageActivity::class.java)
                itemView.context.startActivity(intent)
            }
        }
    }
}