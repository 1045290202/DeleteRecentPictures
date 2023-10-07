package com.sjk.deleterecentpictures.common

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.google.android.material.color.DynamicColors
import com.sjk.deleterecentpictures.utils.*


class App : Application() {
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        
        lateinit var dataSource: DataSource
        lateinit var output: Output
        lateinit var input: Input
        lateinit var const: Const
        lateinit var switch: Switch
        lateinit var globalData: GlobalData
        lateinit var apkUtil: ApkUtil
        lateinit var clipboardUtil: ClipboardUtil
        lateinit var densityUtil: DensityUtil
        lateinit var fileUtil: FileUtil
        lateinit var imageScannerUtil: ImageScannerUtil
        lateinit var qrCodeUtil: QRCodeUtil
        lateinit var recentImages: RecentImages
        lateinit var shellUtil: ShellUtil
        lateinit var appResources: Resources
        lateinit var activityManager: ActivityManager
        lateinit var recycleBinManager: RecycleBinManager
        lateinit var alertDialogUtil: AlertDialogUtil
        lateinit var timeUtil: TimeUtil
        
        val newEvent: Event
            get() {
                return Event()
            }
    }
    
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        dataSource = DataSource
        output = Output
        input = Input
        const = Const
        switch = Switch
        globalData = GlobalData
        apkUtil = ApkUtil
        clipboardUtil = ClipboardUtil
        densityUtil = DensityUtil
        fileUtil = FileUtil
        imageScannerUtil = ImageScannerUtil
        qrCodeUtil = QRCodeUtil
        recentImages = RecentImages
        shellUtil = ShellUtil
        appResources = resources
        activityManager = ActivityManager
        recycleBinManager = RecycleBinManager
        alertDialogUtil = AlertDialogUtil
        timeUtil = TimeUtil
        
        DynamicColors.applyToActivitiesIfAvailable(this)
        BigImageViewer.initialize(GlideImageLoader.with(context))
    }
    
    override fun onTerminate() {
        super.onTerminate()
    }
    
}