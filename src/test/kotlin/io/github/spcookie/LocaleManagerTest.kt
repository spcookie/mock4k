package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class LocaleManagerTest {

    @Test
    fun testSetAndGetLocale() {
        // 测试设置英语语言环境
        LocaleManager.setLocale(Locale.ENGLISH)
        assertEquals(Locale.ENGLISH, LocaleManager.getCurrentLocale())

        // 测试设置中文语言环境
        LocaleManager.setLocale(Locale.CHINESE)
        assertEquals(Locale.CHINESE, LocaleManager.getCurrentLocale())
    }

    @Test
    fun testGetDataList() {
        // 测试英语单词
        LocaleManager.setLocale(Locale.ENGLISH)
        val englishWords = LocaleManager.getDataList("words")
        assertTrue(englishWords.isNotEmpty())
        println("English words: ${englishWords.take(5)}")

        // 测试中文单词
        LocaleManager.setLocale(Locale.CHINESE)
        val chineseWords = LocaleManager.getDataList("words")
        assertTrue(chineseWords.isNotEmpty())
        println("Chinese words: ${chineseWords.take(5)}")
    }

    @Test
    fun testIsLocaleSupported() {
        // 测试支持的语言环境
        assertTrue(LocaleManager.isLocaleSupported(Locale.CHINESE))
        assertTrue(LocaleManager.isLocaleSupported(Locale.ENGLISH))
        // 测试不支持的语言环境
        assertFalse(LocaleManager.isLocaleSupported(Locale.FRENCH))
    }

    @Test
    fun testGetSupportedLocales() {
        // 测试获取支持的语言环境列表
        val supportedLocales = LocaleManager.getSupportedLocales()
        assertEquals(2, supportedLocales.size)
        assertTrue(supportedLocales.contains(Locale.ENGLISH))
        assertTrue(supportedLocales.contains(Locale.CHINESE))
    }
}