package io.github.spcookie

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class RegexPatternTest {

    @Test
    fun testEmailRegexPattern() {
        val template = mapOf(
            "email" to "/[a-z]{5,10}@[a-z]{3,8}\\.(com|org|net)/"
        )
        
        val result = Mock.mock(template) as Map<String, Any?>
        val email = result["email"] as String
        
        assertTrue(email.isNotEmpty())
        assertTrue(email.contains("@"))
        assertTrue(email.contains("."))
        println("Generated email: $email")
    }

    @Test
    fun testPhoneNumberRegexPattern() {
        val template = mapOf(
            "phone" to "/\\d{11}/"
        )
        
        val result = Mock.mock(template) as Map<String, Any?>
        val phone = result["phone"] as String
        
        assertEquals(11, phone.length)
        assertTrue(phone.all { it.isDigit() })
        println("Generated phone: $phone")
    }

    @Test
    fun testWordRegexPattern() {
        val template = mapOf(
            "username" to "/\\w{5,15}/"
        )
        
        val result = Mock.mock(template) as Map<String, Any?>
        val username = result["username"] as String
        
        assertTrue(username.length in 5..15)
        assertTrue(username.all { it.isLetterOrDigit() || it == '_' })
        println("Generated username: $username")
    }

    @Test
    fun testCharacterClassRegexPattern() {
        val template = mapOf(
            "lowercase" to "/[a-z]{8}/",
            "uppercase" to "/[A-Z]{6}/",
            "digits" to "/[0-9]{4}/"
        )
        
        val result = Mock.mock(template) as Map<String, Any?>
        
        val lowercase = result["lowercase"] as String
        val uppercase = result["uppercase"] as String
        val digits = result["digits"] as String
        
        assertEquals(8, lowercase.length)
        assertTrue(lowercase.all { it.isLowerCase() })
        
        assertEquals(6, uppercase.length)
        assertTrue(uppercase.all { it.isUpperCase() })
        
        assertEquals(4, digits.length)
        assertTrue(digits.all { it.isDigit() })
        
        println("Generated lowercase: $lowercase")
        println("Generated uppercase: $uppercase")
        println("Generated digits: $digits")
    }

    @Test
    fun testInvalidRegexPattern() {
        val template = mapOf(
            "invalid" to "/[unclosed/"
        )
        
        val result = Mock.mock(template) as Map<String, Any?>
        val invalid = result["invalid"] as String
        
        // 对于无效的正则表达式应该返回原始字符串（不作为正则表达式处理）
        assertEquals("/[unclosed/", invalid)
    }

    @Test
    fun testNonRegexString() {
        val template = mapOf(
            "normal" to "not a regex"
        )
        
        val result = Mock.mock(template) as Map<String, Any?>
        val normal = result["normal"] as String
        
        // 应该返回原始字符串
        assertEquals("not a regex", normal)
    }

    @Test
    fun testRegexWithRuleModifier() {
        val template = mapOf(
            "emails|3" to "/[a-z]{3,6}@[a-z]{2,5}\\.(com|org)/"
        )
        
        val result = Mock.mock(template) as Map<String, Any?>
        val emails = result["emails"]
        
        // 应该重复生成的邮箱3次
        assertNotNull(emails)
        assertTrue(emails.toString().isNotEmpty())
        println("Generated emails with rule: $emails")
    }

    @Test
    fun testMultipleRegexPatterns() {
        val template = mapOf(
            "user" to mapOf(
                "id" to "/\\d{6}/",
                "name" to "/[A-Z][a-z]{4,9}/",
                "email" to "/[a-z]{4,8}@[a-z]{3,6}\\.(com|net)/"
            )
        )
        
        val result = Mock.mock(template) as Map<String, Any?>
        val user = result["user"] as Map<String, Any?>
        
        val id = user["id"] as String
        val name = user["name"] as String
        val email = user["email"] as String
        
        assertEquals(6, id.length)
        assertTrue(id.all { it.isDigit() })
        
        assertTrue(name.length in 5..10)
        assertTrue(name[0].isUpperCase())
        assertTrue(name.substring(1).all { it.isLowerCase() })
        
        assertTrue(email.contains("@"))
        assertTrue(email.contains("."))
        
        println("Generated user: $user")
    }
}