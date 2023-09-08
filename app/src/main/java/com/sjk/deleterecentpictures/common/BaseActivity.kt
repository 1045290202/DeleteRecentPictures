package com.sjk.deleterecentpictures.common

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.currentActivity = this
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
//        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
    }
    
    override fun onResume() {
        super.onResume()
        App.currentActivity = this
    }
    
    override fun onDestroy() {
        super.onDestroy()
        App.currentActivity = null
    }
    
    protected fun getOutput(): Output {
        return App.output
    }
    
    protected fun getInput(): Input {
        return App.input
    }
    
    protected fun getDataSource(): DataSource {
        return App.dataSource
    }
    
    protected fun getGlobalData(key: String, default: Any?): Any? {
        return App.globalData.getData(key, default)
    }
    
    protected fun setGlobalData(key: String, value: Any?) {
        App.globalData.setData(key, value)
    }
    
    protected fun removeGlobalData(key: String) {
        App.globalData.removeData(key)
    }
}