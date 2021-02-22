package com.sjk.deleterecentpictures.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

@SuppressLint("StaticFieldLeak")
object DataSource {
    var context: Context? = null
        get() = field
        set(value) {
            field = value
        }
    
    fun getSP(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(this.context)
    }
    
    fun getNumberOfPictures(): Int {
        val str = this.getSP().getString("numberOfPictures", Const.DEFAULT_NUMBER_OF_PICTURES.toString())
        var numberOfPictures: Int = if (str == null || str == "") Const.DEFAULT_NUMBER_OF_PICTURES else str.toInt()
        if (numberOfPictures == 0) {
            numberOfPictures = Const.DEFAULT_NUMBER_OF_PICTURES
        }
        return numberOfPictures
    }
}