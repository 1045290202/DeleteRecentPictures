package com.sjk.deleterecentpictures.activity.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.BaseActivity
import com.sjk.deleterecentpictures.utils.ApkUtil
import com.sjk.deleterecentpictures.utils.PermissionUtil.requestPermissionV30

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsActivity = this
        preferenceChanged = false
//        dayNightModeChange(resources.configuration, false)
        setContentView(R.layout.activity_settings2)
        this.supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commitNow()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        this.setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.bindSharedPreferenceEvent()

        this.onBackPressedDispatcher.addCallback {
            this@SettingsActivity.back()
        }
    }


    /**
     * 返回
     */
    fun back() {
        val intent = Intent()
        intent.putExtra("preferenceChanged", preferenceChanged)
        this.setResult(Activity.RESULT_OK, intent)
        this.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.back()
            }

            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    /**
//     * 设置日夜切换
//     *
//     * @param config config
//     */
//     private fun dayNightModeChange(config: Configuration, change: Boolean) {
//         window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//         val dayNightMode = config.uiMode and Configuration.UI_MODE_NIGHT_MASK
//         if (dayNightMode == Configuration.UI_MODE_NIGHT_YES) {
// //            Log.d(TAG, "onCreate: " + "夜间模式");
// //            setTheme(R.style.SettingsActivityDarkTheme)
//             window.statusBarColor = android.R.attr.colorPrimary
//         } else if (dayNightMode == Configuration.UI_MODE_NIGHT_NO) {
// //            Log.d(TAG, "onCreate: " + "非夜间模式");
// //            setTheme(R.style.SettingsActivityLightTheme)
//             window.statusBarColor = ContextCompat.getColor(
//                 this,
//                 android.R.color.white
//             ) //resources.getColor(android.R.color.white)
//             var ui = window.decorView.systemUiVisibility
//             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                 ui = ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//             }
//             window.decorView.systemUiVisibility = ui
//         }
//         if (change) {
//             recreate()
//         }
//     }

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

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            this.setPreferencesFromResource(R.xml.root_preferences, rootKey)
            this.bindPreferenceEvent()
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

            val numberOfPicturesPreference =
                this.findPreference<EditTextPreference>("numberOfPictures")
            numberOfPicturesPreference?.apply {
                this.setOnBindEditTextListener {
                    it.inputType = InputType.TYPE_CLASS_NUMBER
                }
            }

            val allFilesPermissionPreference = this.findPreference<Preference>("allFilesPermission")
            allFilesPermissionPreference?.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            allFilesPermissionPreference?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { _ ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        requireActivity().requestPermissionV30()
                    }
                    true
                }

            val enableMultiWindowLayoutPreference =
                this.findPreference<Preference>("enableMultiWindowLayout")
            enableMultiWindowLayoutPreference?.isEnabled =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

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
                        intent = Intent(Intent.ACTION_VIEW, "coolmarket://u/458995".toUri())
                        intent.setPackage(COOLAPK_PACKAGE_NAME)
                    } else {
                        intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://www.coolapk.com/u/458995".toUri()
                        )
                    }
                    this.startActivity(intent)
                }

                "github" -> {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        resources.getString(R.string.github_url).toUri()
                    )
                    this.startActivity(intent)
                }

                "customizePathDescription" -> {
                    if (App.activityManager.currentActivity == null) {
                        return super.onPreferenceTreeClick(preference)
                    }
                    MaterialAlertDialogBuilder(App.activityManager.currentActivity!!)
                        .setTitle(getString(R.string.customize_path_description_title))
                        .setMessage(
                            HtmlCompat.fromHtml(
                                getString(R.string.custom_path_explanation),
                                HtmlCompat.FROM_HTML_MODE_COMPACT
                            )
                        )
                        .setPositiveButton(R.string.ok, null)
                        .show()
                        .apply {
                            window?.setBackgroundDrawableResource(R.drawable.dialog_background)
                            findViewById<TextView>(android.R.id.message)!!.apply {
                                setTextIsSelectable(true)
                                movementMethod = LinkMovementMethodCompat.getInstance()
                            }
                        }
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