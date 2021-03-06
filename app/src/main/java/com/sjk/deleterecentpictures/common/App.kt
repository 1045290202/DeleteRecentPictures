package com.sjk.deleterecentpictures.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import com.sjk.deleterecentpictures.utils.*


class App : Application() {
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        @SuppressLint("StaticFieldLeak")
        lateinit var currentActivity: Activity
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
        lateinit var recentImages: RecentImages
        
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
        recentImages = RecentImages
    }
    
}