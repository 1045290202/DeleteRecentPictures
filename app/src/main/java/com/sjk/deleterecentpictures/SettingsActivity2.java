package com.sjk.deleterecentpictures;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import com.sjk.deleterecentpictures.utils.CheckApkExist;

import java.util.Objects;

public class SettingsActivity2 extends AppCompatActivity {
    
    private static final String TAG = "SettingsActivity";
    
    private static boolean preferenceChanged = false;
    protected static SettingsActivity2 settingsActivity2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settingsActivity2 = this;
        
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
            
            /*EditTextPreference customizePathPreference = findPreference("customizePath");
            if (customizePathPreference != null) {
//                MaterialTextView customizePathTextView =
//                        ((AppCompatActivity) customizePathPreference.getPreferenceManager().getContext()).findViewById(android.R.id.message);
                *//*LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(
                        getResources().getLayout(R.layout.layout_customize_path_description),
                        null
                );
//                LinearLayout layout = (LinearLayout) view.getChildAt(0);
//                Log.d(TAG, "bindPreferenceEvent: " + (layout.getChildCount()));
                MaterialTextView customizePathTextView = layout.findViewById(android.R.id.message);
                customizePathTextView.setText(Html.fromHtml("<a href=\"https://www.w3school.com.cn/sql/sql_wildcards.asp\">123141</a>"));
                customizePathTextView.setClickable(true);*//*
                
            }*/
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
                case "customizePathDescription": {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setTitle(getResources().getString(R.string.customize_path_description_title))
                            .create();
                    LayoutInflater inflater = Objects.requireNonNull(alertDialog.getWindow()).getLayoutInflater();
                    @SuppressLint("InflateParams")
                    View view = inflater.inflate(R.layout.layout_customize_path_description, null);
                    TextView textView = view.findViewById(R.id.text);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    textView.setText(Html.fromHtml("<p>自定义路径需要省略外置存储目录，无需填写完整路径，如：<strong>Pictures/</strong></p>" +
                            "<p>同时兼容了SQl语句中的通配符：<br>" +
                            "&nbsp;&nbsp;&nbsp;&nbsp;<strong>_</strong>&nbsp;——&nbsp;仅替代一个字符；<br>" +
                            "&nbsp;&nbsp;&nbsp;&nbsp;<strong>%</strong>&nbsp;——&nbsp;替代一个或多个字符；<br>" +
                            "&nbsp;&nbsp;&nbsp;&nbsp;<strong>[charlist]</strong>&nbsp;——&nbsp;替代字符列中的任何单一字符；<br>" +
                            "&nbsp;&nbsp;&nbsp;&nbsp;<strong>[^charlist]</strong>或<strong>[!charlist]</strong>&nbsp;——&nbsp;替代不在字符列中的任何单一字符；<br>" +
                            "如果需要匹配_和%，可以在他们前面添加反斜杠（\\），如果需要输匹配\\，则需要输入\\\\</p>" +
                            "<a href=\"https://www.w3school.com.cn/sql/sql_wildcards.asp\">点此查看更详细的教程</a>"
                    ));
                    alertDialog.setView(view);
                    
                    Button positiveButton = view.findViewById(R.id.positiveButton);
                    positiveButton.setOnClickListener(v -> alertDialog.dismiss());
                    
                    alertDialog.show();
    
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