package com.sjk.deleterecentpictures.common

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

fun logV(tag: String, msg: String) {
    Logger.v(tag, msg)
}

fun logD(tag: String, msg: String) {
    Logger.d(tag, msg)
}

fun logI(tag: String, msg: String) {
    Logger.i(tag, msg)
}

fun logW(tag: String, msg: String) {
    Logger.w(tag, msg)
}

fun logE(tag: String, msg: String) {
    Logger.e(tag, msg)
}

fun log(vararg msgs: Any?) {
    Logger.log(*msgs)
}

private object Logger {
    var df: SimpleDateFormat = SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]", Locale.getDefault())
    
    private fun getLogTime(): String {
        if (!App.switch.ENABLE_LOG_TIME) {
            return ""
        }
        
        return this.df.format(Date())
    }
    
    
    fun v(tag: String, msg: String) {
        if (!App.switch.ENABLE_LOG) {
            return
        }
        if (App.switch.ENABLE_LOG_LEVELS[LoggerLevelEnum.VERBOSE] == false) {
            return
        }
        Log.v("v ${this.getLogTime()}$tag", msg)
    }
    
    fun d(tag: String, msg: String) {
        if (!App.switch.ENABLE_LOG) {
            return
        }
        if (App.switch.ENABLE_LOG_LEVELS[LoggerLevelEnum.DEBUG] == false) {
            return
        }
        Log.d("d ${this.getLogTime()}$tag", msg)
    }
    
    fun i(tag: String, msg: String) {
        if (!App.switch.ENABLE_LOG) {
            return
        }
        if (App.switch.ENABLE_LOG_LEVELS[LoggerLevelEnum.INFO] == false) {
            return
        }
        Log.i("i ${this.getLogTime()}$tag", msg)
    }
    
    fun w(tag: String, msg: String) {
        if (!App.switch.ENABLE_LOG) {
            return
        }
        if (App.switch.ENABLE_LOG_LEVELS[LoggerLevelEnum.WARN] == false) {
            return
        }
        Log.w("w ${this.getLogTime()}$tag", msg)
    }
    
    fun e(tag: String, msg: String) {
        if (!App.switch.ENABLE_LOG) {
            return
        }
        if (App.switch.ENABLE_LOG_LEVELS[LoggerLevelEnum.ERROR] == false) {
            return
        }
        Log.e("e ${this.getLogTime()}$tag", msg)
    }
    
    fun log(vararg msgs: Any?) {
        if (!App.switch.ENABLE_LOG) {
            return
        }
        if (App.switch.ENABLE_LOG_LEVELS[LoggerLevelEnum.LOG] == false) {
            return
        }
        val stringBuilder = StringBuilder()
        for ((index, msg) in msgs.withIndex()) {
            if (index != 0){
                stringBuilder.append(" ")
            }
            stringBuilder.append(msg.toString())
        }
        println("${this.getLogTime()}$stringBuilder")
    }
}

enum class LoggerLevelEnum(val index: Int) {
    VERBOSE(1),
    DEBUG(2),
    INFO(3),
    WARN(4),
    ERROR(5),
    LOG(6);
    
    companion object {
        fun getEnumByValue(what: Int): LoggerLevelEnum? {
            for (value in values()) {
                if (value.index == what) {
                    return value
                }
            }
            return null
        }
    }
    
}