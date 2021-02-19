/**
 * 这个已经废弃啦
 */

package com.sjk.deleterecentpictures

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dayNightModeChange(resources.configuration, false)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onCreate: " + newConfig.uiMode)
        dayNightModeChange(newConfig, true)
    }

    /**
     * 设置日夜切换
     *
     * @param config config
     */
    private fun dayNightModeChange(config: Configuration, change: Boolean) {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val dayNightMode = config.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (dayNightMode == Configuration.UI_MODE_NIGHT_YES) {
            Log.d(TAG, "onCreate: " + "夜间模式")
            setTheme(R.style.SettingsActivityDarkTheme)
            window.statusBarColor = android.R.attr.colorPrimary
        } else if (dayNightMode == Configuration.UI_MODE_NIGHT_NO) {
            Log.d(TAG, "onCreate: " + "非夜间模式")
            setTheme(R.style.SettingsActivityLightTheme)
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.white) // resources.getColor(android.R.color.white)
            var ui = window.decorView.systemUiVisibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ui = ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            window.decorView.systemUiVisibility = ui
        }
        if (change) {
            recreate()
        }
    }

    companion object {
        private const val TAG = "SettingsActivity"
    }
}