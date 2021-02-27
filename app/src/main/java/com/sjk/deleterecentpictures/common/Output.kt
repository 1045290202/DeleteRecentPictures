package com.sjk.deleterecentpictures.common

import android.widget.Toast


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
}