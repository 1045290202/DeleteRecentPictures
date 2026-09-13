package com.sjk.deleterecentpictures.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionUtilTest {

    @Test
    fun `higher patch is newer`() {
        assertTrue(VersionUtil.isNewerVersion("3.4.1", "3.4.0"))
        assertTrue(VersionUtil.isNewerVersion("v3.4.1", "3.4.0"))
        assertTrue(VersionUtil.isNewerVersion("V3.4.1", "3.4.0"))
    }

    @Test
    fun `higher minor is newer`() {
        assertTrue(VersionUtil.isNewerVersion("3.5.0", "3.4.9"))
    }

    @Test
    fun `higher major is newer`() {
        assertTrue(VersionUtil.isNewerVersion("4.0.0", "3.9.9"))
    }

    @Test
    fun `same version is not newer`() {
        assertFalse(VersionUtil.isNewerVersion("3.4.0", "3.4.0"))
        assertFalse(VersionUtil.isNewerVersion("v3.4.0", "3.4.0"))
    }

    @Test
    fun `older version is not newer`() {
        assertFalse(VersionUtil.isNewerVersion("3.3.9", "3.4.0"))
        assertFalse(VersionUtil.isNewerVersion("2.9.9", "3.4.0"))
    }

    @Test
    fun `debug suffix is ignored`() {
        assertFalse(VersionUtil.isNewerVersion("3.4.0", "3.4.0-debug"))
        assertTrue(VersionUtil.isNewerVersion("3.4.1", "3.4.0-debug"))
    }

    @Test
    fun `build metadata is ignored`() {
        assertFalse(VersionUtil.isNewerVersion("3.4.0+12", "3.4.0"))
    }

    @Test
    fun `different segment counts compare by missing as zero`() {
        assertTrue(VersionUtil.isNewerVersion("3.4", "3.3.9"))
        assertFalse(VersionUtil.isNewerVersion("3.4.0", "3.4"))
        assertTrue(VersionUtil.isNewerVersion("3.4.1", "3.4"))
    }

    @Test
    fun `blank or invalid input is not newer`() {
        assertFalse(VersionUtil.isNewerVersion(null, "3.4.0"))
        assertFalse(VersionUtil.isNewerVersion("", "3.4.0"))
        assertFalse(VersionUtil.isNewerVersion("abc", "3.4.0"))
        assertFalse(VersionUtil.isNewerVersion("3.4.1", null))
        assertFalse(VersionUtil.isNewerVersion("3.4.1", ""))
        assertFalse(VersionUtil.isNewerVersion("3.4.1", "abc"))
    }

    @Test
    fun `normalize strips prefix and suffix`() {
        assertEquals(listOf(3, 4, 0), VersionUtil.normalizeVersion("v3.4.0"))
        assertEquals(listOf(3, 4, 0), VersionUtil.normalizeVersion("V3.4.0"))
        assertEquals(listOf(3, 4, 0), VersionUtil.normalizeVersion("3.4.0-debug"))
        assertEquals(listOf(3, 4, 0), VersionUtil.normalizeVersion(" 3.4.0 "))
        assertEquals(emptyList<Int>(), VersionUtil.normalizeVersion(null))
        assertEquals(emptyList<Int>(), VersionUtil.normalizeVersion("x.y.z"))
    }
}
