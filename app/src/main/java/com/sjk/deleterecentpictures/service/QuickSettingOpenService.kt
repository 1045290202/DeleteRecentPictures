package com.sjk.deleterecentpictures.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.sjk.deleterecentpictures.activity.main.MainActivity

@RequiresApi(api = Build.VERSION_CODES.N)
class QuickSettingOpenService : TileService() {
    private var intent: Intent? = null

    // 当用户从Edit栏添加到快速设定中调用
    override fun onTileAdded() {}

    // 当用户从快速设定栏中移除的时候调用
    override fun onTileRemoved() {}

    // 点击的时候
    
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        this.intent = Intent(this, MainActivity::class.java)
        this.intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            this.startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            this.startActivityAndCollapse(this.intent)
        }
    }

    // 打开下拉菜单的时候调用,当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
    // 在TleAdded之后会调用一次
    override fun onStartListening() {}

    // 关闭下拉菜单的时候调用,当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
    // 在onTileRemoved移除之前也会调用移除
    override fun onStopListening() {}
}