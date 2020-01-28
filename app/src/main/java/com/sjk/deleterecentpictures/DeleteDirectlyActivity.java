package com.sjk.deleterecentpictures;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

@SuppressLint("Registered")
public class DeleteDirectlyActivity extends MainActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        super.read();
//
        super.onCreate(savedInstanceState);
//        finish();
        
    }
    
    @Override
    protected void onDestroy() {
        Log.d("", "onDestroy: ");
        super.onDeleteButtonClick();
        super.onDestroy();
    }
}
