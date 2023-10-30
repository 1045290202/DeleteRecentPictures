package com.sjk.deleterecentpictures.common

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.google.android.material.color.DynamicColors
import com.sjk.deleterecentpictures.utils.*


class App : Application() {
    
    companion object {
        lateinit var applicationContext: Context
        lateinit var resources: Resources
        
        val dataSource: DataSource
            get() {
                return DataSource
            }
        val output: Output
            get() {
                return Output
            }
        val input: Input
            get() {
                return Input
            }
        val const: Const
            get() {
                return Const
            }
        val switch: Switch
            get() {
                return Switch
            }
        val globalData: GlobalData
            get() {
                return GlobalData
            }
        val apkUtil: ApkUtil
            get() {
                return ApkUtil
            }
        val clipboardUtil: ClipboardUtil
            get() {
                return ClipboardUtil
            }
        val densityUtil: DensityUtil
            get() {
                return DensityUtil
            }
        val fileUtil: FileUtil
            get() {
                return FileUtil
            }
        val imageScannerUtil: ImageScannerUtil
            get() {
                return ImageScannerUtil
            }
        val qrCodeUtil: QRCodeUtil
            get() {
                return QRCodeUtil
            }
        val recentImages: RecentImages
            get() {
                return RecentImages
            }
        val shellUtil: ShellUtil
            get() {
                return ShellUtil
            }
        val activityManager: ActivityManager
            get() {
                return ActivityManager
            }
        val recycleBinManager: RecycleBinManager
            get() {
                return RecycleBinManager
            }
        val alertDialogUtil: AlertDialogUtil
            get() {
                return AlertDialogUtil
            }
        val timeUtil: TimeUtil
            get() {
                return TimeUtil
            }
        val imageLoadManger: ImageLoadManger
            get() {
                return ImageLoadManger
            }
        val newEvent: Event
            get() {
                return Event()
            }
    }
    
    override fun onCreate() {
        super.onCreate()
        App.applicationContext = this.applicationContext
        App.resources = this.resources
        
        DynamicColors.applyToActivitiesIfAvailable(this)
        recycleBinManager.clearRecycleBin()
        BigImageViewer.initialize(GlideImageLoader.with(applicationContext))
    }
    
}