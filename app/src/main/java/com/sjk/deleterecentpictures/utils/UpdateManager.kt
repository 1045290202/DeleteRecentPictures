package com.sjk.deleterecentpictures.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.sjk.deleterecentpictures.common.App
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import androidx.core.content.edit
import androidx.core.net.toUri

data class UpdateInfo(
    val versionName: String,
    val apkUrl: String?,
    val apkSize: Long,
    val releaseNotes: String,
    val htmlUrl: String,
)

object UpdateManager {
    private const val TAG = "UpdateManager"
    private const val RELEASES_LATEST_URL =
        "https://api.github.com/repos/1045290202/DeleteRecentPictures/releases/latest"
    private const val PREF_LAST_NOTIFIED_VERSION = "lastNotifiedUpdateVersion"
    private const val APK_FILE_NAME = "app-release.apk"
    private const val DOWNLOAD_DIR_NAME = "update"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private val downloading = AtomicBoolean(false)

    sealed class CheckResult {
        data class Available(val updateInfo: UpdateInfo) : CheckResult()
        object UpToDate : CheckResult()
        data class Failed(val message: String?) : CheckResult()
    }

    fun getCurrentVersionName(context: Context = App.applicationContext): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    /**
     * 比较版本号，remote 比 local 新时返回 true
     */
    fun isNewerVersion(remote: String?, local: String?): Boolean {
        return VersionUtil.isNewerVersion(remote, local)
    }

    /**
     * 请求 GitHub Releases 最新版本（后台线程）
     */
    fun checkUpdate(callback: (CheckResult) -> Unit) {
        Thread {
            val result = try {
                this.fetchLatestRelease()
            } catch (e: Exception) {
                CheckResult.Failed(e.message)
            }
            this.mainHandler.post { callback(result) }
        }.start()
    }

    private fun fetchLatestRelease(): CheckResult {
        val request = Request.Builder()
            .url(RELEASES_LATEST_URL)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "DeleteRecentPictures")
            .build()
        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@use CheckResult.Failed("HTTP ${response.code}")
            }
            val body = response.body.string()
            if (body.isBlank()) {
                return@use CheckResult.Failed("empty body")
            }
            val json = JSONObject(body)
            val tagName = json.optString("tag_name", "")
            val versionName = tagName.removePrefix("v").removePrefix("V")
            val htmlUrl = json.optString("html_url", "")
            val releaseNotes = json.optString("body", "")
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        val apkUrl = asset.optString("browser_download_url", "")
                        val apkSize = asset.optLong("size", 0L)
                        return@use this.toCheckResult(
                            UpdateInfo(
                                versionName,
                                apkUrl.ifBlank { null },
                                apkSize,
                                releaseNotes,
                                htmlUrl
                            )
                        )
                    }
                }
            }
            CheckResult.Failed("no apk asset in release")
        }
    }

    private fun toCheckResult(updateInfo: UpdateInfo): CheckResult {
        return if (this.isNewerVersion(updateInfo.versionName, this.getCurrentVersionName())) {
            CheckResult.Available(updateInfo)
        } else {
            CheckResult.UpToDate
        }
    }

    /**
     * 启动静默检查，仅在有新版本且未提醒过时弹窗
     */
    fun silentCheckOnStart(activity: Activity) {
        this.checkUpdate { result ->
            if (result !is CheckResult.Available) {
                return@checkUpdate
            }
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(App.applicationContext)
            val lastNotified = sharedPreferences.getString(PREF_LAST_NOTIFIED_VERSION, null)
            if (lastNotified == result.updateInfo.versionName) {
                return@checkUpdate
            }
            sharedPreferences.edit {
                putString(PREF_LAST_NOTIFIED_VERSION, result.updateInfo.versionName)
            }
            if (activity.isFinishing || activity.isDestroyed) {
                return@checkUpdate
            }
            App.output.showUpdateDialog(result.updateInfo)
        }
    }

    /**
     * 下载 APK 并拉起安装（后台线程），完成后回调到主线程
     */
    fun downloadAndInstall(
        activity: Activity,
        updateInfo: UpdateInfo,
        onProgress: (percent: Int) -> Unit,
        onDone: (file: File?, errorMessage: String?) -> Unit,
    ) {
        val apkUrl = updateInfo.apkUrl
        if (apkUrl.isNullOrBlank()) {
            this.mainHandler.post { onDone(null, "no apk url") }
            return
        }
        if (!this.downloading.compareAndSet(false, true)) {
            this.mainHandler.post { onDone(null, "downloading") }
            return
        }
        Thread {
            try {
                val targetFile = this.prepareApkFile()
                this.downloadApk(apkUrl, targetFile) { percent ->
                    this.mainHandler.post { onProgress(percent) }
                }
                this.mainHandler.post { onDone(targetFile, null) }
            } catch (e: Exception) {
                this.mainHandler.post { onDone(null, e.message) }
            } finally {
                this.downloading.set(false)
            }
        }.start()
    }

    private fun prepareApkFile(): File {
        val dir = File(App.applicationContext.cacheDir, DOWNLOAD_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, APK_FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
        return file
    }

    private fun downloadApk(url: String, targetFile: File, onProgress: (Int) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "DeleteRecentPictures")
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            val body = response.body
            val contentLength = body.contentLength()
            var downloaded = 0L
            val buffer = ByteArray(8 * 1024)
            body.byteStream().use { input ->
                targetFile.outputStream().use { output ->
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) {
                            break
                        }
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (contentLength > 0) {
                            onProgress((downloaded * 100 / contentLength).toInt())
                        }
                    }
                    output.flush()
                }
            }
            if (contentLength > 0 && downloaded != contentLength) {
                throw IOException("incomplete download")
            }
            onProgress(100)
        }
    }

    /**
     * 通过 FileProvider 拉起系统安装界面
     */
    fun installApk(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists()) {
            return false
        }
        return try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Android 8+ 若未授权安装未知应用，引导用户去系统设置开启
     */
    fun canInstallPackages(context: Context = App.applicationContext): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openUnknownSourcesSettings(context: Context = App.applicationContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                "package:${context.packageName}".toUri()
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
