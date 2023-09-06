package com.sjk.deleterecentpictures.common

object Switch {
    const val ENABLE_LOG: Boolean = true
    val ENABLE_LOG_LEVELS: Map<LoggerLevelEnum, Boolean> = mapOf(
            LoggerLevelEnum.VERBOSE to true,
            LoggerLevelEnum.DEBUG to true,
            LoggerLevelEnum.INFO to true,
            LoggerLevelEnum.WARN to true,
            LoggerLevelEnum.ERROR to true,
            LoggerLevelEnum.LOG to true
    )
    const val ENABLE_LOG_TIME = true
}