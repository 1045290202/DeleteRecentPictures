package com.sjk.deleterecentpictures.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.sjk.deleterecentpictures.MainActivity

@RequiresApi(api = Build.VERSION_CODES.N)
class QuickSettingOpenService : TileService() {
    private var intent: Intent? = null

    //当用户从Edit栏添加到快速设定中调用
    override fun onTileAdded() {}

    //当用户从快速设定栏中移除的时候调用
    override fun onTileRemoved() {}

    // 点击的时候
    override fun onClick() {
        intent = Intent(this, MainActivity::class.java)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityAndCollapse(intent)
    }

    // 打开下拉菜单的时候调用,当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
    //在TleAdded之后会调用一次
    override fun onStartListening() {}

    // 关闭下拉菜单的时候调用,当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
    // 在onTileRemoved移除之前也会调用移除
    override fun onStopListening() {}
}