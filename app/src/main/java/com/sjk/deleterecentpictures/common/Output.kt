package com.sjk.deleterecentpictures.common

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import java.io.File
import java.lang.Exception


object Output {
    private val dataSource: DataSource = DataSource
    
    fun showToast(content: CharSequence) {
        Toast.makeText(this.dataSource.context, content, Toast.LENGTH_SHORT).show()
    }
    
    fun showToast(content: Int) {
        Toast.makeText(this.dataSource.context, content, Toast.LENGTH_SHORT).show()
    }
    
    fun showToastLong(content: CharSequence) {
        Toast.makeText(this.dataSource.context, content, Toast.LENGTH_LONG).show()
    }
    
    fun showToastLong(content: Int) {
        Toast.makeText(this.dataSource.context, content, Toast.LENGTH_LONG).show()
    }
    
    fun openByOtherApp(filePath: String?): Boolean {
        if (filePath == null) {
            return false
        }
        
        return this.openByOtherApp(File(filePath))
    }
    
    fun openByOtherApp(file: File): Boolean {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                
                val uri = FileProvider.getUriForFile(App.context, App.context.packageName + ".provider", file)
                intent.setDataAndType(uri, App.fileUtil.getMimeType(file.absolutePath))
            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(Uri.fromFile(file), App.fileUtil.getMimeType(file.absolutePath))
            }
            App.context.startActivity(intent)
        } catch (e: Exception) {
            return false
        }
        
        return true
    }
    
    fun shareToOtherApp(filePath: String?): Boolean {
        if (filePath == null) {
            return false
        }
        
        return this.shareToOtherApp(File(filePath))
    }
    
    fun shareToOtherApp(file: File): Boolean {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = App.fileUtil.getMimeType(file.absolutePath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                
                val uri = FileProvider.getUriForFile(App.context, App.context.packageName + ".provider", file)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            }
            App.context.startActivity(intent)
        } catch (e: Exception) {
            return false
        }
        
        return true
    }
    
    fun showImageLongClickDialog(filePath: String?) {
        AlertDialog.Builder(App.currentActivity)
                .setTitle("请选择你的操作")
                .setItems(App.const.IMAGE_LONG_CLICK_DIALOG_ITEMS) { dialogInterface: DialogInterface, i: Int ->
                    this.onImageLongClickDialogItemClick(dialogInterface, i, filePath)
                }
                .show()
    }
    
    private fun onImageLongClickDialogItemClick(dialogInterface: DialogInterface, i: Int, filePath: String?) {
        when (i) {
            0 -> {
                if (!this.openByOtherApp(filePath)) {
                    this.showToast("唤起打开方式失败")
                }
            }
            1 -> {
                if (!this.shareToOtherApp(filePath)) {
                    this.showToast("唤起分享方式失败")
                }
            }
        }
    }
}