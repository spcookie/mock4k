package io.github.spcookie

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class PhoneI18nTest {

    @Test
    fun testChinesePhoneFormats() {
        MockRandom.setLocale(Locale.CHINESE)

        val phoneNumbers = (1..10).map { MockRandom.phoneNumber() }

        phoneNumbers.forEach { phone ->
            assertNotNull(phone)
            assertTrue(phone.isNotEmpty(), "Phone number should not be empty")
            println("Chinese phone: $phone")

            // Check if it matches Chinese phone patterns
            val isValidChineseFormat = phone.matches(Regex("1\\d{10}")) || // 1##########
                    phone.matches(Regex("\\d{3}-\\d{4}-\\d{4}")) || // ###-####-####
                    phone.matches(Regex("\\d{3} \\d{4} \\d{4}")) || // ### #### ####
                    phone.matches(Regex("\\+86-\\d{3}-\\d{4}-\\d{4}")) || // +86-###-####-####
                    phone.matches(Regex("\\+86 \\d{3} \\d{4} \\d{4}")) // +86 ### #### ####

            assertTrue(isValidChineseFormat, "Phone should match Chinese format: $phone")
        }
    }

    @Test
    fun testEnglishPhoneFormats() {
        MockRandom.setLocale(Locale.ENGLISH)

        val phoneNumbers = (1..10).map { MockRandom.phoneNumber() }

        phoneNumbers.forEach { phone ->
            assertNotNull(phone)
            assertTrue(phone.isNotEmpty(), "Phone number should not be empty")
            println("English phone: $phone")

            // Check if it matches US phone patterns
            val isValidUSFormat = phone.matches(Regex("\\(\\d{3}\\) \\d{3}-\\d{4}")) || // (###) ###-####
                    phone.matches(Regex("\\d{3}-\\d{3}-\\d{4}")) || // ###-###-####
                    phone.matches(Regex("\\d{3} \\d{3} \\d{4}")) || // ### ### ####
                    phone.matches(Regex("\\d{3}\\.\\d{3}\\.\\d{4}")) || // ###.###.####
                    phone.matches(Regex("\\+1-\\d{3}-\\d{3}-\\d{4}")) // +1-###-###-####

            assertTrue(isValidUSFormat, "Phone should match US format: $phone")
        }
    }

    @Test
    fun testCustomPhoneFormat() {
        MockRandom.setLocale(Locale.ENGLISH)

        val customFormat = "(###) ###-####"
        val phone = MockRandom.phoneNumber(format = customFormat)

        assertTrue(
            phone.matches(Regex("\\(\\d{3}\\) \\d{3}-\\d{4}")),
            "Custom format should be respected: $phone"
        )
        println("Custom format phone: $phone")
    }

    @Test
    fun testDefaultPhoneGeneration() {
        // Test default phone generation (should use internationalized format)
        MockRandom.setLocale(Locale.ENGLISH)
        val defaultPhone = MockRandom.phoneNumber()
        assertNotNull(defaultPhone)
        assertTrue(defaultPhone.isNotEmpty(), "Default phone should not be empty")
        println("Default phone: $defaultPhone")

        // Should match one of the English phone formats
        val isValidFormat = defaultPhone.matches(Regex("\\(\\d{3}\\) \\d{3}-\\d{4}")) ||
                defaultPhone.matches(Regex("\\d{3}-\\d{3}-\\d{4}")) ||
                defaultPhone.matches(Regex("\\d{3} \\d{3} \\d{4}")) ||
                defaultPhone.matches(Regex("\\d{3}\\.\\d{3}\\.\\d{4}")) ||
                defaultPhone.matches(Regex("\\+1-\\d{3}-\\d{3}-\\d{4}"))

        assertTrue(isValidFormat, "Default phone should match English format: $defaultPhone")
    }

    @Test
    fun testPhoneFormatSwitching() {
        // Test Chinese format
        MockRandom.setLocale(Locale.CHINESE)
        val chinesePhone = MockRandom.phoneNumber()
        println("Chinese locale phone: $chinesePhone")

        // Switch to English format
        MockRandom.setLocale(Locale.ENGLISH)
        val englishPhone = MockRandom.phoneNumber()
        println("English locale phone: $englishPhone")

        // Phones should be different formats (though not guaranteed due to randomness)
        assertNotNull(chinesePhone)
        assertNotNull(englishPhone)
        assertTrue(chinesePhone.isNotEmpty())
        assertTrue(englishPhone.isNotEmpty())
    }

    @Test
    fun testPhoneNumberDigitsOnly() {
        MockRandom.setLocale(Locale.ENGLISH)

        val phone = MockRandom.phoneNumber()
        val digitsOnly = phone.replace(Regex("[^\\d]"), "")

        // Should have reasonable number of digits (7-15 is typical for phone numbers)
        assertTrue(
            digitsOnly.length >= 7 && digitsOnly.length <= 15,
            "Phone should have 7-15 digits, got ${digitsOnly.length}: $phone"
        )
        println("Phone digits: $digitsOnly (length: ${digitsOnly.length})")
    }
}