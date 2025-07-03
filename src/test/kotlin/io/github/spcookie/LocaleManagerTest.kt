package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class LocaleManagerTest {

    @Test
    fun testSetAndGetLocale() {
        // Test setting English locale
        LocaleManager.setLocale(Locale.ENGLISH)
        assertEquals(Locale.ENGLISH, LocaleManager.getCurrentLocale())
        
        // Test setting Chinese locale
        LocaleManager.setLocale(Locale.CHINESE)
        assertEquals(Locale.CHINESE, LocaleManager.getCurrentLocale())
    }

    @Test
    fun testGetDataList() {
        // Test English words
        LocaleManager.setLocale(Locale.ENGLISH)
        val englishWords = LocaleManager.getDataList("words")
        assertTrue(englishWords.isNotEmpty())
        println("English words: ${englishWords.take(5)}")
        
        // Test Chinese words
        LocaleManager.setLocale(Locale.CHINESE)
        val chineseWords = LocaleManager.getDataList("words")
        assertTrue(chineseWords.isNotEmpty())
        println("Chinese words: ${chineseWords.take(5)}")
    }

    @Test
    fun testIsLocaleSupported() {
        assertTrue(LocaleManager.isLocaleSupported(Locale.CHINESE))
        assertTrue(LocaleManager.isLocaleSupported(Locale.ENGLISH))
        assertFalse(LocaleManager.isLocaleSupported(Locale.FRENCH))
    }

    @Test
    fun testGetSupportedLocales() {
        val supportedLocales = LocaleManager.getSupportedLocales()
        assertEquals(2, supportedLocales.size)
        assertTrue(supportedLocales.contains(Locale.ENGLISH))
        assertTrue(supportedLocales.contains(Locale.CHINESE))
    }
}