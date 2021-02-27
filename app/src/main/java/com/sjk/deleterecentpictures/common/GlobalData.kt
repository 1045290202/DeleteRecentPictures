package com.sjk.deleterecentpictures.common

object GlobalData {
    private const val TAG: String = "GlobalData"
    private var data: MutableMap<String, Any?> = mutableMapOf()
    
    
    fun setData(key: String, value: Any?) {
        this.data[key] = value
    }
    
    fun getData(key: String, default: Any?): Any? {
        if (this.data[key] == null) {
            return default
        }
        return this.data[key]
    }
    
    fun removeData(key: String) {
        this.data.remove(key)
    }
}