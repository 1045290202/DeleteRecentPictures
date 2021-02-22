package com.sjk.deleterecentpictures.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (this.getDataSource().context == null) {
            this.getDataSource().context = applicationContext
        }
    }
    
    protected fun getDataSource(): DataSource {
        return DataSource
    }
    
    protected fun getGlobalData(key: String): Any? {
        return GlobalData.getData(key)
    }
    
    protected fun setGlobalData(key: String, value: Any?) {
        GlobalData.setData(key, value)
    }
    
    protected fun removeGlobalData(key: String) {
        GlobalData.removeData(key)
    }
}