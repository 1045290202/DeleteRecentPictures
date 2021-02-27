package com.sjk.deleterecentpictures.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.currentActivity = this
    }
    
    protected fun getOutPut(): Output {
        return Output
    }
    
    protected fun getDataSource(): DataSource {
        return DataSource
    }
    
    protected fun getGlobalData(key: String, default: Any?): Any? {
        return GlobalData.getData(key, default)
    }
    
    protected fun setGlobalData(key: String, value: Any?) {
        GlobalData.setData(key, value)
    }
    
    protected fun removeGlobalData(key: String) {
        GlobalData.removeData(key)
    }
}