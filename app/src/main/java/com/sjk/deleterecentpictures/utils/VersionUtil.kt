package com.sjk.deleterecentpictures.utils

/**
 * 纯版本号比较工具，不依赖 Android 框架，便于单元测试
 */
object VersionUtil {

    fun isNewerVersion(remote: String?, local: String?): Boolean {
        val remoteParts = normalizeVersion(remote)
        val localParts = normalizeVersion(local)
        if (remoteParts.isEmpty() || localParts.isEmpty()) {
            return false
        }
        val length = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until length) {
            val remotePart = remoteParts.getOrElse(i) { 0 }
            val localPart = localParts.getOrElse(i) { 0 }
            if (remotePart != localPart) {
                return remotePart > localPart
            }
        }
        return false
    }

    fun normalizeVersion(version: String?): List<Int> {
        if (version.isNullOrBlank()) {
            return emptyList()
        }
        return version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore('-')
            .substringBefore('+')
            .split('.')
            .mapNotNull { it.toIntOrNull() }
    }
}
