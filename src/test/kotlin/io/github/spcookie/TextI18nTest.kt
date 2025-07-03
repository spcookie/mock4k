package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class TextI18nTest {

    @Test
    fun testChineseText() {
        MockRandom.setLocale(Locale.CHINESE)

        val word = MockRandom.word()
        val sentence = MockRandom.sentence()
        val paragraph = MockRandom.paragraph()
        val title = MockRandom.title()
        val words = MockRandom.words(3)

        assertNotNull(word)
        assertNotNull(sentence)
        assertNotNull(paragraph)
        assertNotNull(title)
        assertNotNull(words)

        assertTrue(word.isNotEmpty())
        assertTrue(sentence.isNotEmpty())
        assertTrue(paragraph.isNotEmpty())
        assertTrue(title.isNotEmpty())
        assertEquals(3, words.size)

        println("Chinese text - Word: $word")
        println("Chinese text - Sentence: $sentence")
        println("Chinese text - Paragraph: $paragraph")
        println("Chinese text - Title: $title")
        println("Chinese text - Words: $words")
    }

    @Test
    fun testEnglishText() {
        MockRandom.setLocale(Locale.ENGLISH)

        val word = MockRandom.word()
        val sentence = MockRandom.sentence()
        val paragraph = MockRandom.paragraph()
        val title = MockRandom.title()
        val words = MockRandom.words(3)

        assertNotNull(word)
        assertNotNull(sentence)
        assertNotNull(paragraph)
        assertNotNull(title)
        assertNotNull(words)

        assertTrue(word.isNotEmpty())
        assertTrue(sentence.isNotEmpty())
        assertTrue(paragraph.isNotEmpty())
        assertTrue(title.isNotEmpty())
        assertEquals(3, words.size)

        println("English text - Word: $word")
        println("English text - Sentence: $sentence")
        println("English text - Paragraph: $paragraph")
        println("English text - Title: $title")
        println("English text - Words: $words")
    }

    @Test
    fun testWordWithLength() {
        MockRandom.setLocale(Locale.ENGLISH)

        val shortWord = MockRandom.word(3, 5)
        val longWord = MockRandom.word(8, 12)

        assertTrue(shortWord.length >= 3 && shortWord.length <= 5)
        assertTrue(longWord.length >= 8 && longWord.length <= 12)

        println("Short word (3-5): $shortWord (length: ${shortWord.length})")
        println("Long word (8-12): $longWord (length: ${longWord.length})")
    }

    @Test
    fun testSentenceStructure() {
        MockRandom.setLocale(Locale.ENGLISH)

        val sentence = MockRandom.sentence()

        // Should start with uppercase and end with period
        assertTrue(sentence.first().isUpperCase(), "Sentence should start with uppercase")
        assertTrue(sentence.endsWith("."), "Sentence should end with period")

        // Should contain multiple words
        val wordCount = sentence.dropLast(1).split(" ").size
        assertTrue(wordCount >= 12, "Sentence should have at least 12 words")

        println("Generated sentence: $sentence")
        println("Word count: $wordCount")
    }

    @Test
    fun testTitleFormat() {
        MockRandom.setLocale(Locale.ENGLISH)

        val title = MockRandom.title()

        // Each word should start with uppercase
        val words = title.split(" ")
        words.forEach { word ->
            assertTrue(word.first().isUpperCase(), "Each word in title should start with uppercase: $word")
        }

        println("Generated title: $title")
    }

    @Test
    fun testImageTextI18n() {
        // Test English image text
        MockRandom.setLocale(Locale.ENGLISH)
        val englishImageUrl = MockRandom.image()
        assertTrue(englishImageUrl.contains("text="), "Image URL should contain text parameter")
        println("English image URL: $englishImageUrl")

        // Test Chinese image text
        MockRandom.setLocale(Locale.CHINESE)
        val chineseImageUrl = MockRandom.image()
        assertTrue(chineseImageUrl.contains("text="), "Image URL should contain text parameter")
        println("Chinese image URL: $chineseImageUrl")

        // Test custom text (should override i18n)
        val customImageUrl = MockRandom.image(text = "Custom")
        assertTrue(customImageUrl.contains("text=Custom"), "Custom text should be used")
        println("Custom image URL: $customImageUrl")
    }
}