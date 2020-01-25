package com.sjk.deleterecentpictures;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import com.sjk.deleterecentpictures.utils.CheckApkExist;

public class SettingsActivity2 extends AppCompatActivity {
    
    private static final String TAG = "SettingsActivity";
    
    private static boolean preferenceChanged = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preferenceChanged = false;
        
        dayNightModeChange(getResources().getConfiguration(), false);
        
        setContentView(R.layout.activity_settings2);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        bindSharedPreferenceEvent();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
            default: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
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
//            Log.d(TAG, "onCreate: " + "夜间模式");
            setTheme(R.style.SettingsActivityDarkTheme);
            
            window.setStatusBarColor(android.R.attr.colorPrimary);
        } else if (dayNightMode == Configuration.UI_MODE_NIGHT_NO) {
//            Log.d(TAG, "onCreate: " + "非夜间模式");
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
    
    /**
     * 绑定SharedPreference事件
     */
    public void bindSharedPreferenceEvent() {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
//                    Log.d(TAG, "testOnSharedPreferenceChangedWrong key =" + key);
                    preferenceChanged = true;
                    /*switch (key) {
                        case "thumbnailSize": {
                            int number;
                            try {
                                number = Integer.parseInt(sharedPreferences.getString("thumbnailSize", "512"));
                            } catch (Exception e) {
                                e.printStackTrace();
                                sharedPreferences.edit()
                                        .putString("thumbnailSize", "512")
                                        .apply();
                                recreate();
                                Toast.makeText(this, "出错了：必须输入整数，已还原成默认数值", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            sharedPreferences.edit()
                                    .putString("thumbnailSize", "" + number)
                                    .apply();
                            
                            break;
                        }
                        default: {
                            break;
                        }
                    }*/
                });
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("preferenceChanged", preferenceChanged);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
    
    public static class SettingsFragment extends PreferenceFragmentCompat {
        private static final String COOLAPK_PACKAGE_NAME = "com.coolapk.market";
        
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            
            bindPreferenceEvent();
        }
        
        /**
         * 绑定Preference事件
         */
        void bindPreferenceEvent() {
            SeekBarPreference thumbnailSizePreference = findPreference("thumbnailSize");
            if (thumbnailSizePreference != null) {
                String thumbnailSizePreferenceSummary = thumbnailSizePreference.getSummary().toString();
                thumbnailSizePreference.setSummary(thumbnailSizePreferenceSummary + "\n当前：" + thumbnailSizePreference.getValue() + "px");
                
                thumbnailSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    preference.setSummary(thumbnailSizePreferenceSummary + "\n当前：" + newValue + "px");
//                    Toast.makeText(getContext(), "" + newValue, Toast.LENGTH_SHORT).show();
                    return true;
                });
            }
        }
        
        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
//            Log.d(TAG, "onPreferenceTreeClick: " + "click");
            switch (preference.getKey()) {
                case "author": {
                    Intent intent;
                    if (CheckApkExist.checkApkExist(getContext(), COOLAPK_PACKAGE_NAME)) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("coolmarket://u/458995"));
                        intent.setPackage(COOLAPK_PACKAGE_NAME);
                    } else {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.coolapk.com/u/458995"));
                    }
                    startActivity(intent);
                    break;
                }
                case "github": {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.github_url)));
                    startActivity(intent);
                    break;
                }
                default: {
                    break;
                }
            }
            return super.onPreferenceTreeClick(preference);
        }
    }
}