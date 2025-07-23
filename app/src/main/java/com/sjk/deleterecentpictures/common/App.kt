package com.sjk.deleterecentpictures.common

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.flyjingfish.openimagelib.OpenImageConfig
import com.google.android.material.color.DynamicColors
import com.sjk.deleterecentpictures.utils.AlertDialogUtil
import com.sjk.deleterecentpictures.utils.ApkUtil
import com.sjk.deleterecentpictures.utils.BigImageHelperImpl
import com.sjk.deleterecentpictures.utils.ClipboardUtil
import com.sjk.deleterecentpictures.utils.DensityUtil
import com.sjk.deleterecentpictures.utils.FileUtil
import com.sjk.deleterecentpictures.utils.ImageScannerUtil
import com.sjk.deleterecentpictures.utils.QRCodeUtil
import com.sjk.deleterecentpictures.utils.ShellUtil
import com.sjk.deleterecentpictures.utils.TimeUtil


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
        val imageLoadManger: ImageLoadManager
            get() {
                return ImageLoadManager
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

        OpenImageConfig.getInstance().bigImageHelper = BigImageHelperImpl()
    }

}