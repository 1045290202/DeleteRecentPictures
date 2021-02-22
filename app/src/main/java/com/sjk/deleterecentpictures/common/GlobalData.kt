package com.sjk.deleterecentpictures.common

object GlobalData {
    private var data: MutableMap<String, Any?> = mutableMapOf()
    
    fun setData(key: String, value: Any?) {
        this.data[key] = value
    }
    
    fun getData(key: String): Any? {
        return this.data[key]
    }
    
    fun removeData(key: String) {
        this.data.remove(key)
    }
}