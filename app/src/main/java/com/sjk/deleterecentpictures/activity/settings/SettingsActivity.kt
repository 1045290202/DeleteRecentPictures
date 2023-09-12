package com.sjk.deleterecentpictures.activity.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.BaseActivity
import com.sjk.deleterecentpictures.utils.ApkUtil
import kotlin.system.exitProcess

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsActivity = this
        preferenceChanged = false
//        dayNightModeChange(resources.configuration, false)
        setContentView(R.layout.activity_settings2)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commitNow()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        bindSharedPreferenceEvent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }

            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 设置日夜切换
     *
     * @param config config
     */
    private fun dayNightModeChange(config: Configuration, change: Boolean) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val dayNightMode = config.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (dayNightMode == Configuration.UI_MODE_NIGHT_YES) {
//            Log.d(TAG, "onCreate: " + "夜间模式");
//            setTheme(R.style.SettingsActivityDarkTheme)
            window.statusBarColor = android.R.attr.colorPrimary
        } else if (dayNightMode == Configuration.UI_MODE_NIGHT_NO) {
//            Log.d(TAG, "onCreate: " + "非夜间模式");
//            setTheme(R.style.SettingsActivityLightTheme)
            window.statusBarColor = ContextCompat.getColor(
                this,
                android.R.color.white
            ) //resources.getColor(android.R.color.white)
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

    /**
     * 绑定SharedPreference事件
     */
    private fun bindSharedPreferenceEvent() {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .registerOnSharedPreferenceChangeListener { _: SharedPreferences?, _: String? ->
//                    Log.d(TAG, "testOnSharedPreferenceChangedWrong key =" + key);
                preferenceChanged = true
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("preferenceChanged", preferenceChanged)
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            bindPreferenceEvent()
        }

        /**
         * 绑定Preference事件
         */
        private fun bindPreferenceEvent() {
            /*val thumbnailSizePreference = findPreference<SeekBarPreference>("thumbnailSize")
            thumbnailSizePreference?.apply {
                val thumbnailSizePreferenceSummary = summary.toString()
                summary = "$thumbnailSizePreferenceSummary\n当前：${value}px".trimIndent()
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, newValue: Any ->
                    preference.summary = "$thumbnailSizePreferenceSummary\n当前：${newValue}px".trimIndent()
                    true
                }
            }*/

            val numberOfPicturesPreference = findPreference<EditTextPreference>("numberOfPictures")
            numberOfPicturesPreference?.apply {
                setOnBindEditTextListener {
                    it.inputType = InputType.TYPE_CLASS_NUMBER
                }
            }

            val allFilesPermissionPreference = findPreference<Preference>("allFilesPermission")
            allFilesPermissionPreference?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { preference: Preference ->
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                        return@OnPreferenceClickListener true
                    }
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:${App.context.packageName}")
                    startActivity(intent)
                    true
                }

            /*EditTextPreference customizePathPreference = findPreference("customizePath");
            if (customizePathPreference != null) {
//                MaterialTextView customizePathTextView =
//                        ((AppCompatActivity) customizePathPreference.getPreferenceManager().getContext()).findViewById(android.R.id.message);
                */
            /*LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(
                        getResources().getLayout(R.layout.layout_customize_path_description),
                        null
                );
//                LinearLayout layout = (LinearLayout) view.getChildAt(0);
//                Log.d(TAG, "bindPreferenceEvent: " + (layout.getChildCount()));
                MaterialTextView customizePathTextView = layout.findViewById(android.R.id.message);
                customizePathTextView.setText(Html.fromHtml("<a href=\"https://www.w3school.com.cn/sql/sql_wildcards.asp\">123141</a>"));
                customizePathTextView.setClickable(true);*/
            /*

            }*/
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
//            Log.d(TAG, "onPreferenceTreeClick: " + "click");
            when (preference.key) {
                "author" -> {
                    val intent: Intent
                    if (ApkUtil.checkApkExist(context, COOLAPK_PACKAGE_NAME)) {
                        intent = Intent(Intent.ACTION_VIEW, Uri.parse("coolmarket://u/458995"))
                        intent.setPackage(COOLAPK_PACKAGE_NAME)
                    } else {
                        intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.coolapk.com/u/458995")
                        )
                    }
                    startActivity(intent)
                }

                "github" -> {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(resources.getString(R.string.github_url))
                    )
                    startActivity(intent)
                }

                "customizePathDescription" -> {
                    if (App.activityManager.currentActivity == null) {
                        return super.onPreferenceTreeClick(preference)
                    }
                    val alertDialog = MaterialAlertDialogBuilder(App.activityManager.currentActivity!!)
                        .setTitle(resources.getString(R.string.customize_path_description_title))
                        .create()

                    alertDialog.window?.let {
                        it.setBackgroundDrawableResource(R.drawable.dialog_background)
                        val inflater = it.layoutInflater
                        val view =
                            inflater.inflate(R.layout.layout_customize_path_description, null)
                        val textView = view.findViewById<TextView>(R.id.text)
                        textView.movementMethod = LinkMovementMethod.getInstance()
                        val text = this.getString(R.string.custom_path_explanation)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            textView.text = Html.fromHtml(
                                text,
                                Html.FROM_HTML_MODE_COMPACT
                            )
                        } else {
                            textView.text = Html.fromHtml(
                                text,
                            )
                        }
                        alertDialog.setView(view)

                        val positiveButton = view.findViewById<Button>(R.id.positiveButton)
                        positiveButton.setOnClickListener { alertDialog.dismiss() }
                    }


                    alertDialog.show()
                }
                
                "privacyPolicy" -> {
                    App.output.showPrivacyPolicyDialog()
                }

                else -> {
                }
            }
            return super.onPreferenceTreeClick(preference)
        }

        companion object {
            private const val COOLAPK_PACKAGE_NAME = "com.coolapk.market"
        }
    }

    companion object {
        private const val TAG = "SettingsActivity2"
        private var preferenceChanged = false
        private var settingsActivity: SettingsActivity? = null
    }
}