package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class MockRandomI18nTest {

    @Test
    fun testChineseLocale() {
        MockRandom.setLocale(Locale.CHINESE)

        // Test word generation
        val word = MockRandom.word()
        assertNotNull(word)
        assertTrue(word.isNotEmpty())

        // Test city generation
        val city = MockRandom.city()
        assertNotNull(city)
        assertTrue(city.isNotEmpty())

        // Test company generation
        val company = MockRandom.company()
        assertNotNull(company)
        assertTrue(company.isNotEmpty())

        println("Chinese - Word: $word, City: $city, Company: $company")
    }

    @Test
    fun testEnglishLocale() {
        MockRandom.setLocale(Locale.ENGLISH)

        // Test word generation
        val word = MockRandom.word()
        assertNotNull(word)
        assertTrue(word.isNotEmpty())

        // Test city generation
        val city = MockRandom.city()
        assertNotNull(city)
        assertTrue(city.isNotEmpty())

        // Test company generation
        val company = MockRandom.company()
        assertNotNull(company)
        assertTrue(company.isNotEmpty())

        println("English - Word: $word, City: $city, Company: $company")
    }

    @Test
    fun testMultipleWords() {
        MockRandom.setLocale(Locale.CHINESE)

        val words = MockRandom.words(5)
        assertEquals(5, words.size)
        words.forEach { word ->
            assertNotNull(word)
            assertTrue(word.isNotEmpty())
        }

        println("Chinese words: $words")
    }

    @Test
    fun testLocaleSwitch() {
        // Test Chinese
        MockRandom.setLocale(Locale.CHINESE)
        assertEquals(Locale.CHINESE, MockRandom.getCurrentLocale())
        val chineseWord = MockRandom.word()

        // Test English
        MockRandom.setLocale(Locale.ENGLISH)
        assertEquals(Locale.ENGLISH, MockRandom.getCurrentLocale())
        val englishWord = MockRandom.word()

        // Words should be different (most likely)
        println("Chinese word: $chineseWord, English word: $englishWord")
    }

    @Test
    fun testAllDataTypes() {
        MockRandom.setLocale(Locale.ENGLISH)

        // Test all data types
        val word = MockRandom.word()
        val city = MockRandom.city()
        val company = MockRandom.company()
        val province = MockRandom.province()
        val profession = MockRandom.profession()
        val streetName = MockRandom.streetName()
        val emailDomain = MockRandom.emailDomain()

        // All should be non-null and non-empty
        listOf(word, city, company, province, profession, streetName, emailDomain).forEach {
            assertNotNull(it)
            assertTrue(it.isNotEmpty())
        }

        println("Generated data: word=$word, city=$city, company=$company, province=$province, profession=$profession, street=$streetName, email=$emailDomain")
    }
}