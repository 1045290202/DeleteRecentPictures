package com.sjk.deleterecentpictures.utils

import com.sjk.deleterecentpictures.common.logE

object ShellUtil {
    const val TAG: String = "ShellUtil"
    
    fun execDeleteFile(filePath: String): Boolean {
        return this.exec("rm -r $filePath")
    }
    
    fun exec(command: String?): Boolean {
        if (command == null) {
            return false
        }
        
        // var bufferedReader: BufferedReader? = null
        val runtime = Runtime.getRuntime()
        return try {
            //Process中封装了返回的结果和执行错误的结果
            val process = runtime.exec(command)
            /*bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            val stringBuffer = StringBuffer()
            val buff = CharArray(1024)
            var ch = 0
            while (bufferedReader.read(buff).also { ch = it } != -1) {
                stringBuffer.append(buff, 0, ch)
            }*/
            true
        } catch (e: Exception) {
            logE(TAG, e.stackTraceToString())
            false
        }
    }
}