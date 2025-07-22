package com.sjk.deleterecentpictures.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.activity.main.MainActivity
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.logD
import com.sjk.deleterecentpictures.service.ImageWidgetService

class ImageWidget : AppWidgetProvider() {

    companion object {
        private const val TAG = "ImageWidget"
    }

    private val appWidgetSet: MutableSet<Int> = mutableSetOf()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach {
            this.appWidgetSet.add(it)
            updateAppWidget(context, appWidgetManager, it)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        logD(TAG, "onEnabled")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.stopService(Intent(context, ImageWidgetService::class.java))
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds?.forEach {
            this.appWidgetSet.remove(it)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (context == null || intent == null) {
            return
        }

        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                appWidgetIds?.forEach {
                    updateAppWidget(context, appWidgetManager, it)
                }
            }

            "OPEN" -> {
                val mainActivityIntent = Intent(context, MainActivity::class.java)
                mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(mainActivityIntent)
            }

            "DELETE" -> {
                val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                val imageId = intent.getStringExtra("imageId")
                val imagePath = intent.getStringExtra("imagePath")
                val serviceIntent = Intent(context, ImageWidgetService::class.java)
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                serviceIntent.putExtra("imageId", imageId)
                serviceIntent.putExtra("imagePath", imagePath)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                // 广播
                // val deleteIntent = Intent()
                // deleteIntent.action = "${context.packageName}.deleteImage"
                // deleteIntent.putExtra("imageId", imageId)
                // deleteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                // context.sendBroadcast(deleteIntent)

                // val imagePath = intent.getStringExtra("imagePath")
                // Thread {
                //     val deleted = this.deleteImage(context, imageId)
                //     // if (deleted) {
                //     //     App.output.showToast(
                //     //         context.getString(
                //     //             R.string.successfully_deleted,
                //     //             imagePath
                //     //         )
                //     //     )
                //     // } else {
                //     //     App.output.showToast(context.getString(R.string.delete_failed_because_no_information))
                //     // }
                //     val appWidgetManager = AppWidgetManager.getInstance(context)
                //     val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                //     appWidgetIds?.forEach {
                //         updateAppWidget(context, appWidgetManager, it)
                //     }
                // }.start()
            }

            else -> {
            }
        }
    }

    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        logD(
            TAG,
            "onAppWidgetOptionsChanged: $context, $appWidgetManager, $appWidgetId, $newOptions"
        )
        if (context == null || appWidgetManager == null) {
            return
        }
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        logD(TAG, "updateAppWidget appWidgetId: $appWidgetId")
        val imageInfoBean: ImageInfoBean? = App.imageScannerUtil.getLatest(
            context,
            App.dataSource.getSelection(),
            sortOrder = App.dataSource.getSortOrder(),
        )

        val views = RemoteViews(context.packageName, R.layout.image_widget)

        val refreshIntent = Intent(context, ImageWidget::class.java)
        refreshIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        views.setOnClickPendingIntent(
            R.id.refreshButton,
            PendingIntent.getBroadcast(
                context,
                0,
                refreshIntent,
                PendingIntent.FLAG_MUTABLE,
            )
        )

        val openIntent = Intent(context, ImageWidget::class.java)
        openIntent.action = "OPEN"
        openIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        views.setOnClickPendingIntent(
            R.id.imageView,
            PendingIntent.getBroadcast(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_MUTABLE,
            )
        )

        val deleteIntent = Intent(context, ImageWidget::class.java)
        deleteIntent.action = "DELETE"
        deleteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        deleteIntent.putExtra("imageId", "${imageInfoBean?.id ?: -1}")
        deleteIntent.putExtra("imagePath", imageInfoBean?.path ?: "")
        views.setOnClickPendingIntent(
            R.id.deleteButton,
            PendingIntent.getBroadcast(
                context,
                1,
                deleteIntent,
                PendingIntent.FLAG_MUTABLE,
            )
        )

        if (imageInfoBean == null) {
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }

        views.setImageViewUri(R.id.imageView, null)
        appWidgetManager.updateAppWidget(appWidgetId, views)

        Thread {
            // val appWidgetImageViewTarget = AppWidgetTarget(context, R.id.imageView, views, appWidgetId)
            Glide.with(context)
                .asBitmap()
                .override(300, 300)
                .load(imageInfoBean.uri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?,
                    ) {
                        views.setImageViewBitmap(R.id.imageView, resource)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
            // .into(appWidgetImageViewTarget)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }.start()
    }
}