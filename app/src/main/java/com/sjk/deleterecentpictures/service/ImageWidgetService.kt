package com.sjk.deleterecentpictures.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.logD

class ImageWidgetService : Service() {
    companion object {
        private const val TAG = "ImageWidgetService"
    }

    private var receiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()

        logD(TAG, "onCreate")

        this.startForeground(1, this.createNotification())

        // this.receiver = object : BroadcastReceiver() {
        //     override fun onReceive(context: Context?, intent: Intent?) {
        //         if (context == null || intent == null) {
        //             return
        //         }
        //         this@ImageWidgetService.deleteImage(context, intent.getLongExtra("imageId", -1L))
        //         val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
        //         val refreshIntent = Intent()
        //         refreshIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        //         refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        //         context.sendBroadcast(refreshIntent)
        //     }
        // }
        // val intentFilter = IntentFilter()
        // intentFilter.addAction("${this.applicationContext.packageName}.deleteImage")
        // this.registerReceiver(this.receiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        // this.unregisterReceiver(this.receiver)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logD(TAG, "onStartCommand: $intent")
        if (intent == null) {
            return START_STICKY
        }

        val imageId = intent.getStringExtra("imageId")
        logD(TAG, "imageId: $imageId")
        // val imagePath = intent.getStringExtra("imagePath")
        // val imageIds = bundle?.getLongArray("imageIds")
        // val imageInfoBean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //     intent.getSerializableExtra("imageInfoBean", ImageInfoBean::class.java)
        // } else {
        //     intent.getSerializableExtra("imageInfoBean") as ImageInfoBean
        // }
        this.deleteImage(this, imageId?.toLong() ?: -1)
        val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
        // val refreshIntent = Intent()
        // refreshIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        // refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        // 发送到对应的widget
        // val pendingIntent = PendingIntent.getBroadcast(this, 0, refreshIntent, PendingIntent.FLAG_MUTABLE)
        val views = RemoteViews(this.packageName, R.layout.image_widget)
        val appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetManager.updateAppWidget(appWidgetIds, views)

        // 结束自己
        this.stopSelf()

        return START_STICKY
    }

    /**
     * 删除图片，返回成功或者失败
     */
    private fun deleteImage(context: Context, imageId: Long?): Boolean {
        if (imageId == -1L) {
            return false
        }
        return App.fileUtil.deleteImage(imageId, context)
    }

    /**
     * 删除图片，返回成功或者失败
     */
    private fun deleteImage(context: Context, imagePath: String?): Boolean {
        if (TextUtils.isEmpty(imagePath)) {
            return false
        }
        return App.fileUtil.deleteImage(imagePath, context)
    }

    private fun deleteImages(context: Context, imageIds: LongArray?) {
        if (imageIds == null) {
            return
        }
        for (imageId in imageIds) {
            this.deleteImage(context, imageId)
        }
    }

    private fun createNotification(): Notification {
        val channelId = "ForegroundServiceChannel"
        val channelName = "Foreground Service Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Foreground Service")
            .setContentText("Service is running in the foreground")
            .setSmallIcon(R.drawable.ic_image)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)

        return notificationBuilder.build()
    }

}