package com.sjk.deleterecentpictures

//import android.view.View
//import com.sjk.deleterecentpictures.ImageActivity
//import com.sjk.deleterecentpictures.MainActivity
//import com.sjk.deleterecentpictures.MainActivity.MainActivityHandlerMsgWhat
//import android.net.Uri
//import android.provider.Settings
//import com.zxy.tiny.Tiny

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjk.deleterecentpictures.common.BaseActivity
import com.sjk.deleterecentpictures.utils.FileUtil
import com.sjk.deleterecentpictures.utils.ImageScanner
import com.sjk.deleterecentpictures.utils.TransformUtil
import java.util.*


open class MainActivity : BaseActivity() {
    
    private var isLoaded = false
    
    companion object {
        private const val TAG = "MainActivity"
        var imagePaths: MutableList<String?>? = null
        
        //    private static String imagePath;
        var theLatestImages: MutableList<Bitmap>? = null
        
        //    public static Bitmap theLatestImage;
        private val PERMISSIONS_STORAGE = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
        )
    }
    
    @SuppressLint("HandlerLeak")
    protected val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (Objects.requireNonNull(MainActivityHandlerMsgWhat.getByValue(msg.what))) {
                MainActivityHandlerMsgWhat.ERROR -> {
        
                    //错
                    val string = msg.obj.toString()
                    Toast.makeText(applicationContext, "出错了：$string", Toast.LENGTH_SHORT).show()
                }
                MainActivityHandlerMsgWhat.REFRESH_TEXT -> {
        
                    //设置文本
//                    String imagePath = msg.obj.toString();
//                    MainActivity.imagePath = imagePath;
                    val latestPicturePathButton = findViewById<Button>(R.id.latestPicturePathButton)
                    latestPicturePathButton.text = imagePaths!![0]
                }
                MainActivityHandlerMsgWhat.REFRESH_IMAGE -> {
        
                    //设置图片
//                    val bitmap = msg.obj as Bitmap
                    val latestPictureImageView = findViewById<SubsamplingScaleImageView>(R.id.latestPictureImageView)
                    val bitmap = TransformUtil.filePath2Bitmap(imagePaths!![0]) ?: return
                    latestPictureImageView.setImage(ImageSource.bitmap(bitmap))
                    latestPictureImageView.contentDescription = imagePaths!![0]
                }
                MainActivityHandlerMsgWhat.DELETE_IMAGE_SUCCESS -> {
                    Toast.makeText(applicationContext, "删除图片 ${imagePaths!![0]} 成功", Toast.LENGTH_SHORT).show()
                    val sp = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    val closeApp = sp.getBoolean("closeApp", true)
                    if (closeApp) {
                        finish()
                        return
                    }
        
                    read()
                }
                MainActivityHandlerMsgWhat.DELETE_IMAGE_FAIL -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                        Toast.makeText(applicationContext, "删除图片 ${imagePaths!![0]} 失败，请检查是否授予所有文件访问权限", Toast.LENGTH_SHORT).show()
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:$packageName")
                        startActivityForResult(intent, 1)
                    }else{
                        Toast.makeText(applicationContext, "删除图片 ${imagePaths!![0]} 失败", Toast.LENGTH_SHORT).show()
                        read()
                    }
                }
            }
        }
    }
    
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        //设置默认偏好
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        initView()
    }
    
    private fun initView() {
        setContentView(R.layout.activity_main)
        buttonClickEventBind()
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (this.isLoaded) {
            return
        }
        requestWritePermission()
        isLoaded = true
    }
    
    private fun initList() {
        theLatestImages = ArrayList()
        imagePaths = ArrayList()
    }
    
    private fun buttonClickEventBind() {
        val latestPicturePathButton = findViewById<Button>(R.id.latestPicturePathButton)
        latestPicturePathButton.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(resources.getString(R.string.app_name), imagePaths!![0])
            clipboard.setPrimaryClip(clip)
            Toast.makeText(applicationContext, "已复制到剪切板", Toast.LENGTH_SHORT).show()
        }
        latestPicturePathButton.setOnLongClickListener {
            Toast.makeText(this, "当前查找目录下有${imagePaths!!.size}张图片", Toast.LENGTH_SHORT).show()
            true
        }
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        refreshButton.setOnClickListener {
            Toast.makeText(this, "开始刷新", Toast.LENGTH_SHORT).show()
            read()
        }
        val openImageActivityButton = findViewById<Button>(R.id.openImageActivityButton)
        openImageActivityButton.setOnClickListener {
            //打开图片查看界面
            val intent = Intent(applicationContext, ImageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener { finish() }
        
        //删除按钮
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            val sp = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
            val deleteDirectly = sp.getBoolean("deleteDirectly", false)
            if (deleteDirectly) {
                onDeleteButtonClick()
            } else {
                try {
                    MaterialAlertDialogBuilder(this@MainActivity)
                            .setTitle("警告")
                            .setMessage("请确认是否删除\n${imagePaths!![0]}".trimIndent())
                            .setPositiveButton("确定") { _: DialogInterface?, _: Int -> onDeleteButtonClick() }
                            .setNegativeButton("取消") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                            .show()
                } catch (e: IndexOutOfBoundsException) {
                    val message = Message()
                    message.what = MainActivityHandlerMsgWhat.ERROR.index
                    message.obj = "没有获取到图片路径，删除失败"
                    handler.sendMessage(message)
                }
            }
        }
        deleteButton.setOnLongClickListener {
            onDeleteButtonClick()
            finish()
            true
        }
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity2::class.java)
            startActivityForResult(intent, 1)
        }
    }
    
    /**
     * 点击删除按钮触发事件
     */
    private fun onDeleteButtonClick() {
        Thread {
//          Log.d("imagePath", "run: " + imagePaths.get(0));
            val message = Message()
            
            if (imagePaths!!.size == 0) {
                message.what = MainActivityHandlerMsgWhat.ERROR.index
                message.obj = "没有获取到图片路径，删除失败"
                handler.sendMessage(message)
                return@Thread
            }
            
            imagePaths!![0]?.let {
                val scanType: String = this.getDataSource().getSP().getString("scanType", "1")!!
                
                //删除图片并判断
                if (FileUtil.deleteFile(it)) {
                    message.what = MainActivityHandlerMsgWhat.DELETE_IMAGE_SUCCESS.index

//                    FileUtil.updateFileFromDatabase(applicationContext, imagePaths!![0]) { path: String, uri: Uri? ->
//                        handler.sendMessage(message)
//                    }
                } else {
                    message.what = MainActivityHandlerMsgWhat.DELETE_IMAGE_FAIL.index
                }
                
                ImageScanner.refreshMediaLibraryByPath(applicationContext, it, scanType.toInt()) { path: String, uri: Uri? ->
                    handler.sendMessage(message)
                }
            }
            
            /*if (imagePaths!![0] != null && imagePaths!![0] != "") {
                val message = Message()
                //删除图片并判断
                if (FileUtil.deleteFile(imagePaths!![0])) {
                    message.what = MainActivityHandlerMsgWhat.DELETE_IMAGE_SUCCESS.index
                    val scanType: String = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("scanType", "1")!!

                    ImageScanner.refreshMediaLibraryByPath(applicationContext, imagePaths!![0], scanType.toInt()) { path: String, uri: Uri? ->
                        handler.sendMessage(message)
                    }
//                    FileUtil.updateFileFromDatabase(applicationContext, imagePaths!![0]) { path: String, uri: Uri? ->
//
//                        handler.sendMessage(message)
//                    }
                } else {
                    message.what = MainActivityHandlerMsgWhat.DELETE_IMAGE_FAIL.index
                    handler.sendMessage(message)
                }
            } else {
                val message = Message()
                message.what = MainActivityHandlerMsgWhat.ERROR.index
                message.obj = "没有获取到图片路径，删除失败"
                handler.sendMessage(message)
            }*/
        }.run()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: $requestCode $requestCode $data")
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getBooleanExtra("preferenceChanged", false)
            if (result) {
                startActivity(Intent(this@MainActivity, MainActivity::class.java))
                finish()
                Toast.makeText(this, "已重新加载", Toast.LENGTH_SHORT).show()
            }
        }/* else {
//            Toast.makeText(this, "无返回值", Toast.LENGTH_SHORT).show();
        }*/
        super.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                val message = Message()
                message.what = MainActivityHandlerMsgWhat.ERROR.index
                message.obj = "没有获取到读/写权限，自动退出"
                handler.sendMessage(message)
                finish()
                return
            }
        }
        
        /*if (requestCode == 1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()){
                val message = Message()
                message.what = MainActivityHandlerMsgWhat.ERROR.index
                message.obj = "没有获取到读/写权限，自动退出"
                handler.sendMessage(message)
                finish()
                return
            }
        }*/
        //        initView();
        read()
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
            /*if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 0)
                return
            }*/
//                initView();
            read()
            return
        }
        
        read()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
////            intent.type = "*/*"
//            startActivityForResult(intent, 2)
//        }
    }
    
    protected fun read() {
        Thread(Runnable {
            initList()
            val message = Message()
//            ImageScanner.getA(applicationContext)
            ImageScanner.searchImages(applicationContext, selection, true)
            imagePaths = ImageScanner.imagePaths
            if (imagePaths == null) {
                message.what = MainActivityHandlerMsgWhat.ERROR.index
                message.obj = "查询路径不合法，请重新设置"
                handler.sendMessage(message)
                return@Runnable
            }
            if (imagePaths!!.isEmpty()) {
                message.what = MainActivityHandlerMsgWhat.ERROR.index
                message.obj = "没有找到图片"
                handler.sendMessage(message)
                return@Runnable
            }
            val imagePath = imagePaths!![0]
            if (imagePath != null) {
                val textViewMessage = Message()
                textViewMessage.what = MainActivityHandlerMsgWhat.REFRESH_TEXT.index
                //                        textViewMessage.obj = imagePath;
                handler.sendMessage(textViewMessage)
                try {
//                    bitmap = BitmapFactory.decodeFile(imagePaths!![0])
                    /*val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val sp = PreferenceManager.getDefaultSharedPreferences(this)
                        val thumbnailSize = sp.getInt("thumbnailSize", 512)
                        ThumbnailUtils.createImageThumbnail(
                                File(imagePath),
                                Size(thumbnailSize, thumbnailSize),
                                CancellationSignal()
                        )
                    } else {
                        ImageScanner.firstImageThumbnail
                    }*/
            
                    message.what = MainActivityHandlerMsgWhat.REFRESH_IMAGE.index
//                    message.obj = ImageScanner.firstImageThumbnail
                    handler.sendMessage(message)
                    /* val latestPictureImageView: ImageView = findViewById(R.id.latestPictureImageView)
                    val uncompressedBitmap: Bitmap = BitmapFactory.decodeFile(imagePaths!![0])
                    val options = Tiny.BitmapCompressOptions()
//                    options.config = Bitmap.Config.RGB_565
//                    options.height = latestPictureImageView.height
                    Tiny.getInstance().source(imagePaths!![0]).asBitmap().withOptions(options).compress { isSucceeded: Boolean, compressedBitmap: Bitmap?, t: Throwable? ->
                        message.what = MainActivityHandlerMsgWhat.REFRESH_IMAGE.index
                        message.obj = compressedBitmap
                        handler.sendMessage(message)
                    }*/
            
                } catch (e: Exception) {
                    e.printStackTrace()
                    message.what = MainActivityHandlerMsgWhat.ERROR.index
                    message.obj = "读取图片失败"
                    handler.sendMessage(message)
                    return@Runnable
                }
            } else {
                message.what = MainActivityHandlerMsgWhat.ERROR.index
                message.obj = "查找图片失败"
                handler.sendMessage(message)
            }
        }).run()
    }
    
    //        Log.d(TAG, "read: " + sp.getString("path", strings[0]));
    private val selection: String?
        get() {
            val strings = resources.getStringArray(R.array.path_values)
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val pathType = sp.getString("path", strings[0])
            //        Log.d(TAG, "read: " + sp.getString("path", strings[0]));
            var selection: String? = null
            when (pathType) {
                strings[0] -> {
                    selection = null
                }
                strings[1] -> {
                    selection = ImageScanner.screenshotsPath
                }
                strings[2] -> {
                    val externalFilesDir = Environment.getExternalStorageDirectory()
        
                    selection = if (externalFilesDir != null) {
                        externalFilesDir.path + "/" + sp.getString("customizePath", "")
                    } else {
                        Toast.makeText(this, "无法获取外置存储位置, 替换为默认查询", Toast.LENGTH_SHORT).show()
                        null
                    }
                }
            }
            Log.d(TAG, "getSelection: 查询目录$selection")
            return selection
        }
    
    /*public static Bitmap bitmapCompress(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 10) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
            if (options <= 0) {
                break;
            }
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        return BitmapFactory.decodeStream(isBm, null, null);
    }*/
    internal enum class MainActivityHandlerMsgWhat(val index: Int) {
        //错误
        ERROR(-1),  //刷新文本
        REFRESH_TEXT(0),  //刷新图片
        REFRESH_IMAGE(1),  //删除图片成功
        DELETE_IMAGE_SUCCESS(2),  //删除图片失败
        DELETE_IMAGE_FAIL(3);
        
        companion object {
            fun getByValue(what: Int): MainActivityHandlerMsgWhat? {
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