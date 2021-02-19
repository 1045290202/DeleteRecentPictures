package com.sjk.deleterecentpictures

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log

@SuppressLint("Registered")
class DeleteDirectlyActivity : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        super.read();
//
        super.onCreate(savedInstanceState)
        //        finish();
    }

    override fun onDestroy() {
        Log.d("", "onDestroy: ")
        super.onDeleteButtonClick()
        super.onDestroy()
    }
}