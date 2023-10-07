package com.sjk.deleterecentpictures.utils

import java.text.DateFormat
import java.util.*

object TimeUtil {
    
    /**
     * 将时间戳格式化为系统默认格式
     * @param timestamp 时间戳
     */
    fun formatTimestampToSystemDefaultFormat(timestamp: Long): String {
        val dateFormat = DateFormat.getDateTimeInstance()
        return dateFormat.format(Date(timestamp))
    }
}