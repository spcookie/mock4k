package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class LocaleManagerTest {

    @Test
    fun testSetAndGetLocale() {
        // Test setting Chinese locale
        LocaleManager.setLocale(Locale.CHINESE)
        assertEquals(Locale.CHINESE, LocaleManager.getCurrentLocale())

        // Test setting English locale
        LocaleManager.setLocale(Locale.ENGLISH)
        assertEquals(Locale.ENGLISH, LocaleManager.getCurrentLocale())
    }

    @Test
    fun testLoadLocaleData() {
        // Test loading Chinese data
        val chineseProperties = LocaleManager.loadLocaleData(Locale.CHINESE)
        assertNotNull(chineseProperties)
        assertTrue(chineseProperties.containsKey("words"))

        // Test loading English data
        val englishProperties = LocaleManager.loadLocaleData(Locale.ENGLISH)
        assertNotNull(englishProperties)
        assertTrue(englishProperties.containsKey("words"))
    }

    @Test
    fun testGetDataList() {
        // Test Chinese words
        LocaleManager.setLocale(Locale.CHINESE)
        val chineseWords = LocaleManager.getDataList("words")
        assertTrue(chineseWords.isNotEmpty())
        assertTrue(chineseWords.any { it.matches(Regex("[\\u4e00-\\u9fa5]+")) })

        // Test English words
        LocaleManager.setLocale(Locale.ENGLISH)
        val englishWords = LocaleManager.getDataList("words")
        assertTrue(englishWords.isNotEmpty())
        assertTrue(englishWords.any { it.matches(Regex("[a-zA-Z]+")) })
    }

    @Test
    fun testIsLocaleSupported() {
        assertTrue(LocaleManager.isLocaleSupported(Locale.CHINESE))
        assertTrue(LocaleManager.isLocaleSupported(Locale.ENGLISH))
        assertFalse(LocaleManager.isLocaleSupported(Locale.FRENCH))
        assertFalse(LocaleManager.isLocaleSupported(Locale.GERMAN))
    }

    @Test
    fun testGetSupportedLocales() {
        val supportedLocales = LocaleManager.getSupportedLocales()
        assertEquals(2, supportedLocales.size)
        assertTrue(supportedLocales.contains(Locale.ENGLISH))
        assertTrue(supportedLocales.contains(Locale.CHINESE))
    }

    @Test
    fun testClearCache() {
        // Load some data to populate cache
        LocaleManager.setLocale(Locale.CHINESE)
        LocaleManager.getDataList("words")

        LocaleManager.setLocale(Locale.ENGLISH)
        LocaleManager.getDataList("words")

        // Clear cache
        LocaleManager.clearCache()

        // Data should still be accessible (will be reloaded)
        val words = LocaleManager.getDataList("words")
        assertTrue(words.isNotEmpty())
    }

    @Test
    fun testDataListWithSpecificLocale() {
        // Test getting data for specific locale without changing current locale
        LocaleManager.setLocale(Locale.ENGLISH)

        val chineseWords = LocaleManager.getDataList("words", Locale.CHINESE)
        val englishWords = LocaleManager.getDataList("words", Locale.ENGLISH)

        // Current locale should still be English
        assertEquals(Locale.ENGLISH, LocaleManager.getCurrentLocale())

        // But we should get different data
        assertNotEquals(chineseWords, englishWords)
        assertTrue(chineseWords.any { it.matches(Regex("[\\u4e00-\\u9fa5]+")) })
        assertTrue(englishWords.any { it.matches(Regex("[a-zA-Z]+")) })
    }

    @Test
    fun testEmptyDataList() {
        LocaleManager.setLocale(Locale.ENGLISH)
        val nonExistentData = LocaleManager.getDataList("nonExistentKey")
        assertTrue(nonExistentData.isEmpty())
    }
}