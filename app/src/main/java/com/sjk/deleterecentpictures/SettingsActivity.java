package com.sjk.deleterecentpictures;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import static android.app.UiModeManager.MODE_NIGHT_AUTO;

public class SettingsActivity extends AppCompatActivity {
    
    private static final String TAG = "SettingsActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dayNightModeChange(getResources().getConfiguration(), false);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onCreate: " + newConfig.uiMode);
        dayNightModeChange(newConfig, true);
    }
    
    /**
     * 设置日夜切换
     *
     * @param config config
     */
    private void dayNightModeChange(Configuration config, boolean change) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        int dayNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (dayNightMode == Configuration.UI_MODE_NIGHT_YES) {
            Log.d(TAG, "onCreate: " + "夜间模式");
            setTheme(R.style.SettingsActivityDarkTheme);
            
            window.setStatusBarColor(android.R.attr.colorPrimary);
        } else if (dayNightMode == Configuration.UI_MODE_NIGHT_NO) {
            Log.d(TAG, "onCreate: " + "非夜间模式");
            setTheme(R.style.SettingsActivityLightTheme);
            
            window.setStatusBarColor(getResources().getColor(android.R.color.white));
            int ui = window.getDecorView().getSystemUiVisibility();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            window.getDecorView().setSystemUiVisibility(ui);
        }
        if (change) {
            recreate();
        }
    }
}
