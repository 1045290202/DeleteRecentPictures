package com.sjk.deleterecentpictures.common

object Switch {
    val ENABLE_LOG_LEVELS: Map<LoggerLevelEnum, Boolean> = mapOf(
            LoggerLevelEnum.VERBOSE to false,
            LoggerLevelEnum.DEBUG to false,
            LoggerLevelEnum.INFO to false,
            LoggerLevelEnum.WARN to false,
            LoggerLevelEnum.ERROR to false,
            LoggerLevelEnum.LOG to false
    )
    const val ENABLE_LOG_TIME = true
}