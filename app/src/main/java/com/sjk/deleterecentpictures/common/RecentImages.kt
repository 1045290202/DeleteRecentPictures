package com.sjk.deleterecentpictures.common

import com.sjk.deleterecentpictures.entity.ImageInfoEntity
import com.sjk.deleterecentpictures.utils.ImageScannerUtil

object RecentImages {
    var imageInfos: MutableList<ImageInfoEntity?> = ArrayList()
    var currentImageInfoIndex: Int = 0

    val currentImageInfo: ImageInfoEntity?
        get() {
            if (this.currentImageInfoIndex >= this.imageInfos.size || this.currentImageInfoIndex < 0) {
                return null
            }

            return this.imageInfos[this.currentImageInfoIndex]
        }

    val currentImagePath: String?
        get() {
            return this.currentImageInfo?.path
        }

    val imageChecks: MutableList<Boolean> = ArrayList()

    fun clearImagePaths() {
        this.imageInfos = ArrayList()
    }

    /**
     * 挂载懒加载图片列表：列表仅持有游标视图，
     * 翻到哪一页才向媒体库查询哪一张，不再一次性物化所有图片
     */
    fun resetImageInfos(maxCount: Int) {
        this.imageInfos = LazyImageInfos(App.imageScannerUtil, maxCount)
    }

    /**
     * 重置勾选状态，使勾选列表与图片列表长度严格一致
     */
    fun resetImageChecks(size: Int) {
        this.imageChecks.clear()
        repeat(size) {
            this.imageChecks.add(false)
        }
    }

    fun resetCurrentImagePathIndex() {
        this.currentImageInfoIndex = 0
    }

    fun clearImageChecks() {
        this.imageChecks.clear()
    }
}

/**
 * 基于媒体库游标的懒加载列表：
 * size 来自查询结果总数，get(position) 时才读取对应行数据并缓存，
 * 避免启动/刷新时把全部图片信息物化到内存
 */
class LazyImageInfos(
    private val scanner: ImageScannerUtil,
    private val maxCount: Int,
) : AbstractMutableList<ImageInfoEntity?>() {

    companion object {
        private const val TAG = "LazyImageInfos"

        // 缓存上限，防止内存无限增长；超出后淘汰最早访问的条目
        private const val MAX_CACHE_SIZE = 128
    }

    private val cache = object : LinkedHashMap<Int, ImageInfoEntity?>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, ImageInfoEntity?>?): Boolean {
            return this.size > MAX_CACHE_SIZE
        }
    }

    override val size: Int
        get() = minOf(this.scanner.getCount(), this.maxCount)

    override fun get(index: Int): ImageInfoEntity? {
        if (index < 0 || index >= this.size) {
            return null
        }
        synchronized(this.cache) {
            if (this.cache.containsKey(index)) {
                return this.cache[index]
            }
        }
        val imageInfo = this.scanner.getAt(index)
        if (imageInfo == null) {
            logD(TAG, "get: position $index returned null")
            return null
        }
        synchronized(this.cache) {
            this.cache[index] = imageInfo
        }
        return imageInfo
    }

    override fun add(index: Int, element: ImageInfoEntity?) {
        throw UnsupportedOperationException("LazyImageInfos 不支持修改，请通过 refresh 重建")
    }

    override fun set(index: Int, element: ImageInfoEntity?): ImageInfoEntity? {
        throw UnsupportedOperationException("LazyImageInfos 不支持修改，请通过 refresh 重建")
    }

    override fun removeAt(index: Int): ImageInfoEntity? {
        throw UnsupportedOperationException("LazyImageInfos 不支持修改，请通过 refresh 重建")
    }

    override fun clear() {
        synchronized(this.cache) {
            this.cache.clear()
        }
    }
}
