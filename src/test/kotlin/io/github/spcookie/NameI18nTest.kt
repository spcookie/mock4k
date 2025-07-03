package io.github.spcookie

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class NameI18nTest {

    @Test
    fun testChineseNames() {
        MockRandom.setLocale(Locale.CHINESE)

        val firstName = MockRandom.first()
        val lastName = MockRandom.last()
        val fullName = MockRandom.name()

        assertNotNull(firstName)
        assertNotNull(lastName)
        assertNotNull(fullName)
        assertTrue(firstName.isNotEmpty())
        assertTrue(lastName.isNotEmpty())
        assertTrue(fullName.isNotEmpty())

        println("Chinese names - First: $firstName, Last: $lastName, Full: $fullName")
    }

    @Test
    fun testEnglishNames() {
        MockRandom.setLocale(Locale.ENGLISH)

        val firstName = MockRandom.first()
        val lastName = MockRandom.last()
        val fullName = MockRandom.name()

        assertNotNull(firstName)
        assertNotNull(lastName)
        assertNotNull(fullName)
        assertTrue(firstName.isNotEmpty())
        assertTrue(lastName.isNotEmpty())
        assertTrue(fullName.isNotEmpty())

        println("English names - First: $firstName, Last: $lastName, Full: $fullName")
    }

    @Test
    fun testNameGeneration() {
        // Test multiple name generations to ensure variety
        MockRandom.setLocale(Locale.ENGLISH)

        val names = mutableSetOf<String>()
        repeat(10) {
            names.add(MockRandom.name())
        }

        // Should have some variety (not all identical)
        assertTrue(names.size > 1, "Generated names should have some variety")

        println("Generated names: $names")
    }
}