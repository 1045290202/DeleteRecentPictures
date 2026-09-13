package com.sjk.deleterecentpictures.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * 依赖外网的集成测试：验证 GitHub Releases API 结构与版本比较逻辑衔接正常
 * 网络不可用时自动跳过
 */
class UpdateApiIntegrationTest {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    @Test
    fun `latest release from github can be parsed and compared`() {
        val reachable = try {
            val probe = Request.Builder().url("https://api.github.com").build()
            httpClient.newCall(probe).execute().use { it.isSuccessful || it.code == 404 }
        } catch (e: Exception) {
            false
        }
        assumeTrue("GitHub API 不可达，跳过集成测试", reachable)

        val request = Request.Builder()
            .url("https://api.github.com/repos/1045290202/DeleteRecentPictures/releases/latest")
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "DeleteRecentPictures-Test")
            .build()
        httpClient.newCall(request).execute().use { response ->
            assertTrue(response.isSuccessful)
            val body = response.body.string()
            val json = JSONObject(body)
            val tagName = json.optString("tag_name", "")
            assertTrue("tag_name 不应为空", tagName.isNotBlank())

            val versionName = tagName.removePrefix("v").removePrefix("V")
            assertTrue(VersionUtil.isNewerVersion(versionName, "0.0.1"))
            assertFalse(VersionUtil.isNewerVersion(versionName, "999.0.0"))

            val assets = json.optJSONArray("assets")
            assertNotNull(assets)
            var hasApk = false
            for (i in 0 until assets!!.length()) {
                val name = assets.getJSONObject(i).optString("name", "")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    hasApk = true
                    val url = assets.getJSONObject(i).optString("browser_download_url", "")
                    assertTrue(url.startsWith("https://"))
                }
            }
            assertTrue("release 应包含 apk 资源", hasApk)
        }
    }
}
