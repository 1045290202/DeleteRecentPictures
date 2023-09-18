package com.sjk.deleterecentpictures.activity.main


import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
// import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import androidx.core.app.ActivityCompat
// import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.GlideImageViewFactory
import com.google.android.material.checkbox.MaterialCheckBox
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.activity.image.ImageActivity
import com.sjk.deleterecentpictures.activity.settings.SettingsActivity
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.*
import java.util.*


open class MainActivity : BaseActivity() {
    
    private var isLoaded = false
    private var viewPager: ViewPager2? = null
    private val viewPagerAdapter = MainActivityViewPagerAdapter()
    private val event: Event = App.newEvent
    
    //    private var viewPagerCurrentPosition = 0
    private var hasAllFilesAccessPermission = true
    
    companion object {
        private const val TAG = "MainActivity"
        
        //    public static Bitmap theLatestImage;
        private val PERMISSIONS_STORAGE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.getOutput().tryShowPrivacyPolicyDialog {
            // 设置默认偏好
            PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
            initView()
            requestWritePermission()
        }
    }
    
    override fun onRestart() {
        super.onRestart()
        if (viewPager == null) {
            return
        }
        if (this.viewPager?.currentItem != App.dataSource.getCurrentImageInfoIndex()) {
            this.viewPager?.setCurrentItem(App.dataSource.getCurrentImageInfoIndex(), false)
        }
        if (!this.hasAllFilesAccessPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                this.hasAllFilesAccessPermission = true
                this.refreshAll()
            } else {
                this.getOutput()
                    .showToast(this.getString(R.string.not_getting_manage_external_storage_permission))
                App.activityManager.finishAll()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        App.recycleBinManager.deleteOldImageInRecycleBin()
    }
    
    override fun finish() {
        super.finish()
        App.recentImages.clearImagePaths()
        App.recentImages.clearImageChecks()
        App.imageScannerUtil.close()
    }
    
    private fun initView() {
        setContentView(R.layout.activity_main)
        buttonClickEventBind()
        
        this.viewPager = this.findViewById(R.id.viewPager)
        this.viewPager!!.adapter = viewPagerAdapter
        this.viewPagerAdapter.imageInfos = App.dataSource.getRecentImageInfos()
        this.viewPagerAdapter.imageChecks = App.dataSource.getImageChecks()
        this.viewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                
                App.input.setCurrentImagePathIndex(position)
                
                if (App.dataSource.getRecentImageInfos().size == 0) {
                    App.input.setCurrentImagePathIndex(0)
                    return
                }
                
                this@MainActivity.refreshCurrentImagePath()
            }
        })
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (this.isLoaded) {
            return
        }
        
        isLoaded = true
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: $requestCode $requestCode $data")
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getBooleanExtra("preferenceChanged", false)
            if (result) {
                this.startActivity(Intent(this@MainActivity, MainActivity::class.java))
                this.finish()
                this.getOutput().showToast("已重新加载设置")
            }
        }/* else {
//            Toast.makeText(this, "无返回值", Toast.LENGTH_SHORT).show();
        }*/
        super.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                this.getOutput().showToast(this.getString(R.string.storage_permission_missing))
                logW(TAG, "Storage permission not obtained")
                App.activityManager.finishAll()
                return
            }
        }
        
        this.refreshAll()
    }
    
    private fun requestWritePermission() {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                this.hasAllFilesAccessPermission = false
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                // startActivityForResult(intent, 2)
                startActivity(intent)
            }
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PERMISSIONS_STORAGE.iterator().forEach {
            if (checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 0)
                return
            }
        }
//            this.refreshAll()
//            return
//        }
        
        this.refreshAll()
    }
    
    private fun buttonClickEventBind() {
        val currentPicturePathButton = findViewById<Button>(R.id.currentPicturePathButton)
        currentPicturePathButton.setOnClickListener {
            App.output.showPathButtonClickDialog()
        }
        currentPicturePathButton.setOnLongClickListener {
            App.input.copyCurrentImageName()
            true
        }
        val refreshButton = this.findViewById<Button>(R.id.refreshButton)
        refreshButton.setOnClickListener {
            this.refreshAll {
                App.input.setAllImageChecksFalse()
                this.viewPagerAdapter.setAllHolderChecked(false)
                App.output.showToast(getString(R.string.refresh_successful))
            }
        }
        refreshButton.setOnLongClickListener {
            this.refreshAll {
                App.input.setAllImageChecksFalse()
                this.viewPagerAdapter.setAllHolderChecked(false)
                this.viewPager?.setCurrentItem(0, true)
                App.output.showToast(getString(R.string.refresh_successful_and_go_back))
            }
            true
        }
        /*val openImageActivityButton = findViewById<Button>(R.id.openImageActivityButton)
        openImageActivityButton.setOnClickListener {
            //打开图片查看界面
            val intent = Intent(applicationContext, ImageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }*/
        val cancelButton = this.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener { this.finish() }
        
        // 删除按钮
        val deleteButton = this.findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            if (this.arePicturesChecked()) {
                App.output.showDeleteCheckedImagesDialog(positiveCallback = { dialogInterface: DialogInterface?, witch: Int ->
                    this.deleteCheckedImages {
                        App.input.setAllImageChecksFalse()
                        this.viewPagerAdapter.setAllHolderChecked(false)
                    }
                })
                return@setOnClickListener
            }
            
            val deleteDirectly = this.getDataSource().getSP().getBoolean("deleteDirectly", false)
            if (deleteDirectly) {
                this.deleteCurrentImage {
                    App.input.setAllImageChecksFalse()
                    this.viewPagerAdapter.setAllHolderChecked(false)
                }
            } else {
                App.output.showDeleteCurrentImageDialog(positiveCallback = { dialogInterface: DialogInterface?, witch: Int ->
                    this.deleteCurrentImage {
                        App.input.setAllImageChecksFalse()
                        this.viewPagerAdapter.setAllHolderChecked(false)
                    }
                })
            }
        }
        deleteButton.setOnLongClickListener {
            if (this.arePicturesChecked()) {
                this.deleteCheckedImages {
                    this.finish()
                }
                return@setOnLongClickListener true
            }
            this.deleteCurrentImage(false) {
                this.finish()
            }
            true
        }
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }
    
    private fun arePicturesChecked(): Boolean {
        return App.dataSource.getAllCheckedImageInfos().size != 0
    }
    
    private fun deleteCheckedImages(
        needToRefresh: Boolean = true,
        callback: () -> Unit = fun() {}
    ) {
        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false
        
        Thread {
            var allDeleted = true
            var someDeleted = false
            val allCheckedImageInfos = App.dataSource.getAllCheckedImageInfos()
            for (imageInfo in allCheckedImageInfos) {
                // 删除图片并判断
                if (App.fileUtil.deleteImage(imageInfo)) {
                    someDeleted = true
                } else {
                    allDeleted = false
                }
            }
            this.runOnUiThread {
                when {
                    allDeleted -> {
                        App.output.showToast(this.getString(R.string.all_deleted))
                    }
                    
                    someDeleted -> {
                        App.output.showToast(this.getString(R.string.partial_deletion_failed))
                    }
                    
                    else -> {
                        App.output.showToast(this.getString(R.string.failed_to_delete_all))
                    }
                }
                
                if (App.dataSource.getSP().getBoolean("closeApp", true)) {
                    callback()
                    this.finish()
                    return@runOnUiThread
                }
                
                if (needToRefresh) {
                    this.refreshImages {
                        this.refreshCurrentImagePath()
                    }
                }
                
                deleteButton.isEnabled = true
                callback()
            }
        }.start()
    }
    
    private fun deleteCurrentImage(needToRefresh: Boolean = true, callback: () -> Unit = fun() {}) {
        if (this.getDataSource().getCurrentImageInfo()?.uri == null) {
            this.getOutput()
                .showToast(this.getString(R.string.delete_failed_because_no_information))
            return
        }
        
        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false
        
        /**
         * 显示已删除的toast
         */
        fun showDeletedToast() {
            App.output.showToast(
                this.getString(
                    R.string.successfully_deleted,
                    this.getDataSource().getCurrentImageInfo()!!.path
                )
            )
        }
        
        Thread {
            // 删除图片并判断
            // val deleted = App.fileUtil.deleteImage(App.dataSource.getCurrentImageInfo())
            val deleted =
                App.recycleBinManager.moveToRecycleBin(App.dataSource.getCurrentImageInfo())
            val undelete = this.getDataSource().getSP().getBoolean("undelete", false)
            this.runOnUiThread {
                if (!deleted) {
                    App.output.showToast(this.getString(R.string.picture_cannot_be_deleted))
                    return@runOnUiThread
                }
                
                if (!undelete) {
                    showDeletedToast()
                }
                
                if (App.dataSource.getSP().getBoolean("closeApp", true)) {
                    callback()
                    this.finish()
                    return@runOnUiThread
                }
                if (needToRefresh) {
                    this.refreshImages {
                        this.refreshCurrentImagePath()
                    }
                    
                    if (undelete) {
                        this.getOutput().showSnackBarIndefinite(
                            this.findViewById(R.id.viewPager),
                            this.getString(
                                R.string.successfully_deleted,
                                this.getDataSource().getCurrentImageInfo()!!.path
                            )
                        ) {
                            
                            it.setAction(this.getString(R.string.revoke)) {
                                // 撤回操作
                                App.recycleBinManager.recover(onSuccess = { _, _ ->
                                    this@MainActivity.refreshImages {
                                        this@MainActivity.refreshCurrentImagePath()
                                    }
                                })
                            }
                            it.setAnchorView(findViewById(R.id.viewPagerOverlay))
                        }
                    }
                }
                deleteButton.isEnabled = true
                callback()
            }
        }.start()
    }

    private fun refreshAll(callback: () -> Unit = fun() {}) {
        Thread {
            this.refreshImages(callback)
        }.start()
    }
    
    private fun refreshCurrentImagePath() {
        val currentPicturePathButton = findViewById<Button>(R.id.currentPicturePathButton)
        currentPicturePathButton.text =
            this.getDataSource().getFileNameByPath(this.getDataSource().getCurrentImageInfo())
    }
    
    private fun refreshImages(callback: () -> Unit = fun() {}) {
        App.imageScannerUtil.init(
            this,
            this.getDataSource().getSelection(),
            sortOrder = App.dataSource.getSortOrder()
        )
//        App.recentImages.resetCurrentImagePathIndex()
        App.recentImages.clearImagePaths()
        
        var i = this.getDataSource().getNumberOfPictures()
        if (i > 0) {
            val imageInfo: ImageInfoBean? = App.imageScannerUtil.getCurrent()
            imageInfo != null && this.getDataSource().getRecentImageInfos().add(imageInfo)
            i--
        }
        while (i-- > 0) {
            val imageInfo: ImageInfoBean = App.imageScannerUtil.getNext() ?: break
            this.getDataSource().getRecentImageInfos().add(imageInfo)
        }
        for (index in this.viewPagerAdapter.imageInfos.indices) {
            this.viewPagerAdapter.imageChecks.add(false)
        }
        /*for (i in 0..this.getDataSource().getNumberOfPictures()) {
            val imagePath = App.imageScannerUtil.getNext() ?: break
            this.viewPagerAdapter.imagePaths.add(imagePath)
        }*/
        
        if (this.getDataSource().getRecentImageInfos().size == 0) {
//            this.getOutPut().showToast("未发现图片")
            this.getDataSource().getRecentImageInfos().add(ImageInfoBean())
        }
//        this.viewPager.adapter = viewPagerAdapter
        this.runOnUiThread {
            this.viewPagerAdapter.notifyDataSetChanged()
            this.refreshCurrentImagePath()
            callback()
        }
    }
}

internal class MainActivityViewPagerAdapter :
    RecyclerView.Adapter<MainActivityViewPagerAdapter.ViewPagerViewHolder>() {
    
    companion object {
        private const val TAG = "MainActivityViewPagerAdapter"
        lateinit var instance: MainActivityViewPagerAdapter
    }
    
    private var viewPagerViewHolders: MutableList<ViewPagerViewHolder> = ArrayList()
    var imageInfos: MutableList<ImageInfoBean?> = ArrayList()
    var imageChecks: MutableList<Boolean> = ArrayList()
    val event: Event = App.newEvent
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        instance = this
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_main_view_pager_item, parent, false)
        val viewPagerViewHolder = ViewPagerViewHolder(view)
        viewPagerViewHolders.add(viewPagerViewHolder)
        return viewPagerViewHolder
    }
    
    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        if (this.imageChecks.size > 0) {
            holder.isChecked = this.imageChecks[position]
        }
        
        holder.imageInfo = this.imageInfos[position]
    }
    
    override fun onViewDetachedFromWindow(holder: ViewPagerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        
        holder.imageView.cancel()
    }
    
    override fun onViewAttachedToWindow(holder: ViewPagerViewHolder) {
        super.onViewAttachedToWindow(holder)
        
        if (holder.imageInfo?.uri == null) {
            holder.checkBox.visibility = View.GONE
            holder.imageView.visibility = View.GONE
            holder.imageView.cancel()
            return
        }
        holder.checkBox.visibility = View.VISIBLE
        holder.checkBox.isChecked = holder.isChecked
        holder.imageView.visibility = View.VISIBLE
        holder.imageView.showImage(holder.imageInfo!!.uri)
    }
    
    override fun getItemCount(): Int {
        return this.imageInfos.size
    }
    
    fun setHolderChecked(position: Int, isChecked: Boolean) {
        this.viewPagerViewHolders[position].run {
            this.checkBox.isChecked = isChecked
            this.isChecked = isChecked
        }
    }
    
    fun setAllHolderChecked(isChecked: Boolean) {
        this.viewPagerViewHolders.forEachIndexed { index: Int, viewPagerViewHolder: ViewPagerViewHolder ->
            this.setHolderChecked(index, isChecked)
        }
    }
    
    internal inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: MaterialCheckBox = itemView.findViewById(R.id.checkbox)
        val imageView: BigImageView = itemView.findViewById(R.id.imageView)
        private val openImageActivityButton =
            itemView.findViewById<Button>(R.id.openImageActivityButton)
        var imageInfo: ImageInfoBean? = null
        var isChecked: Boolean = false
        
        init {
            this.imageView.setImageViewFactory(GlideImageViewFactory())
            this.openImageActivityButton.setOnClickListener {
                if (this.imageInfo?.uri == null) {
                    return@setOnClickListener
                }
                val intent = Intent(itemView.context, ImageActivity::class.java)
                itemView.context.startActivity(intent)
            }
            this.openImageActivityButton.setOnLongClickListener {
                if (this.imageInfo?.uri == null) {
                    return@setOnLongClickListener true
                }
                
                App.output.showImageLongClickDialog(this.imageInfo!!.path)
                
                return@setOnLongClickListener true
            }
            this.checkBox.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                if (this.imageInfo?.uri == null) {
                    return@setOnCheckedChangeListener
                }
                
                if (App.dataSource.getCurrentImageInfo() != this.imageInfo) {
                    return@setOnCheckedChangeListener
                }
                this.isChecked = isChecked
                instance.imageChecks[App.dataSource.getCurrentImageInfoIndex()] = isChecked
            }
            this.checkBox.setOnLongClickListener {
                if (this.imageInfo?.uri == null) {
                    return@setOnLongClickListener true
                }
                
                this@MainActivityViewPagerAdapter.setAllHolderChecked(false)
                App.input.setAllImageChecksFalse()
                App.output.showToast("已取消所有已选择图片")
                true
            }
        }
    }
}