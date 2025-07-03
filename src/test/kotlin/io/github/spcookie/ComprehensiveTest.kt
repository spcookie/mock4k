package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

/**
 * 综合测试套件 - 覆盖所有规则、修饰符、国际化支持和复杂场景
 */
class ComprehensiveTest {

    @BeforeEach
    fun setUp() {
        // 重置为默认语言环境
        MockRandom.setLocale(Locale.ENGLISH)
    }

    // ==================== 基础规则测试 ====================

    @Test
    fun testAllBasicRules() {
        val template = mapOf(
            // 字符串规则
            "stringCount" to "Hello|3",
            "stringRange" to "World|2-5",

            // 数字规则
            "intRange" to "number|1-100",
            "floatRange" to "decimal|1-10.1-3",
            "floatFixed" to "price|100.2",

            // 布尔规则
            "booleanCount" to "flag|1",
            "booleanRange" to "status|3-7",

            // 数组规则
            "arrayCount" to listOf("item1", "item2", "item3") + "|2",
            "arrayRange" to listOf("a", "b", "c", "d") + "|1-3"
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 验证所有字段都存在且不为空
        result.forEach { (key, value) ->
            assertNotNull(value, "Field $key should not be null")
            println("$key: $value")
        }
    }

    // ==================== 规则修饰符测试 ====================

    @Test
    fun testIncrementModifier() {
        val template = mapOf(
            "users|3" to listOf(
                mapOf(
                    "id|+1" to 1000,
                    "sequence|+5" to 100,
                    "name" to "User"
                )
            )
        )

        val result = Mock.mock(template) as Map<String, Any>
        val users = result["users"] as List<Map<String, Any>>

        assertEquals(3, users.size)

        // 验证递增规则
        for (i in users.indices) {
            val user = users[i]
            val expectedId = 1000 + i
            val expectedSequence = 100 + (i * 5)

            assertEquals(expectedId, user["id"])
            assertEquals(expectedSequence, user["sequence"])
        }

        println("Increment modifier result: $result")
    }

    @Test
    fun testRangeModifier() {
        val template = mapOf(
            "age|18-65" to 25,
            "score|0-100" to 50,
            "temperature|-10-40" to 20,
            "items|2-8" to listOf("apple", "banana", "orange")
        )

        val result = Mock.mock(template) as Map<String, Any>

        val age = result["age"] as Int
        val score = result["score"] as Int
        val temperature = result["temperature"] as Int
        val items = result["items"] as List<*>

        assertTrue(age in 18..65, "Age should be in range 18-65, got $age")
        assertTrue(score in 0..100, "Score should be in range 0-100, got $score")
        assertTrue(temperature in -10..40, "Temperature should be in range -10-40, got $temperature")
        assertTrue(items.size in 2..8, "Items count should be in range 2-8, got ${items.size}")

        println("Range modifier result: $result")
    }

    @Test
    fun testCountModifier() {
        val template = mapOf(
            "tags|5" to listOf("tag1", "tag2", "tag3"),
            "letters|10" to "A",
            "flags|3" to true
        )

        val result = Mock.mock(template) as Map<String, Any>

        val tags = result["tags"] as List<*>
        val letters = result["letters"] as String
        val flags = result["flags"] as Boolean

        assertEquals(5, tags.size)
        assertEquals(10, letters.length)
        assertEquals(true, flags)

        println("Count modifier result: $result")
    }

    @Test
    fun testFloatModifiers() {
        val template = mapOf(
            "price|100-999.2" to 199.99,
            "weight|10-50.1-3" to 25.5,
            "percentage|0-100.2-4" to 75.25
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            when (value) {
                is Double -> {
                    assertTrue(value >= 0, "$key should be positive, got $value")
                    println("$key: $value")
                }

                is Float -> {
                    assertTrue(value >= 0, "$key should be positive, got $value")
                    println("$key: $value")
                }
            }
        }
    }

    // ==================== 占位符测试 ====================

    @Test
    fun testBasicPlaceholders() {
        val template = mapOf(
            "name" to "@NAME",
            "firstName" to "@FIRST",
            "lastName" to "@LAST",
            "email" to "@EMAIL",
            "phone" to "@PHONENUMBER",
            "city" to "@CITY",
            "company" to "@COMPANY",
            "profession" to "@PROFESSION",
            "province" to "@PROVINCE",
            "street" to "@STREETNAME"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty")
            assertFalse(valueStr.startsWith("@"), "$key placeholder should be resolved, got: $valueStr")
            println("$key: $valueStr")
        }
    }

    @Test
    fun testDateTimePlaceholders() {
        val template = mapOf(
            "date" to "@DATE",
            "time" to "@TIME",
            "datetime" to "@DATETIME",
            "now" to "@NOW"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty")
            assertFalse(valueStr.startsWith("@"), "$key placeholder should be resolved, got: $valueStr")
            println("$key: $valueStr")
        }
    }

    @Test
    fun testNetworkPlaceholders() {
        val template = mapOf(
            "url" to "@URL",
            "domain" to "@DOMAIN",
            "ip" to "@IP",
            "tld" to "@TLD",
            "emailDomain" to "@EMAILDOMAIN"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty")
            assertFalse(valueStr.startsWith("@"), "$key placeholder should be resolved, got: $valueStr")
            println("$key: $valueStr")
        }
    }

    @Test
    fun testTextPlaceholders() {
        val template = mapOf(
            "word" to "@WORD",
            "sentence" to "@SENTENCE",
            "paragraph" to "@PARAGRAPH",
            "title" to "@TITLE"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty")
            assertFalse(valueStr.startsWith("@"), "$key placeholder should be resolved, got: $valueStr")
            println("$key: $valueStr")
        }
    }

    @Test
    fun testParameterizedPlaceholders() {
        val template = mapOf(
            "naturalNumber" to "@NATURAL(10, 100)",
            "integerNumber" to "@INTEGER(-50, 50)",
            "floatNumber" to "@FLOAT(1.0, 10.0)",
            "randomString" to "@STRING(15)",
            "sentenceWithLength" to "@SENTENCE(5, 10)"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty")
            assertFalse(valueStr.startsWith("@"), "$key placeholder should be resolved, got: $valueStr")
            println("$key: $valueStr")
        }
    }

    @Test
    fun testPhoneTypePlaceholders() {
        val template = mapOf(
            "mobile2" to "@PHONENUMBER(PT.M)",
            "landline2" to "@PHONENUMBER(PT.L)",
            "tollFree2" to "@PHONENUMBER(PT.TF)",
            "premium2" to "@PHONENUMBER(PT.P)"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val phoneNumber = value.toString()
            assertNotNull(value, "Phone number for $key should not be null")
            assertTrue(phoneNumber.isNotEmpty(), "Phone number for $key should not be empty")
            assertFalse(phoneNumber.startsWith("@"), "Phone placeholder $key should be resolved, got: $phoneNumber")
            assertTrue(phoneNumber.any { it.isDigit() }, "Phone number $key should contain digits: $phoneNumber")
            println("$key: $phoneNumber")
        }
    }

    // ==================== 国际化测试 ====================

    @Test
    fun testChineseLocalization() {
        MockRandom.setLocale(Locale.CHINESE)

        val template = mapOf(
            "name" to "@NAME",
            "city" to "@CITY",
            "company" to "@COMPANY",
            "province" to "@PROVINCE",
            "profession" to "@PROFESSION",
            "word" to "@WORD",
            "sentence" to "@SENTENCE"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty")
            assertFalse(valueStr.startsWith("@"), "$key placeholder should be resolved, got: $valueStr")
            println("Chinese $key: $valueStr")
        }
    }

    @Test
    fun testJapaneseLocalization() {
        MockRandom.setLocale(Locale.JAPANESE)

        val template = mapOf(
            "name" to "@NAME",
            "city" to "@CITY",
            "company" to "@COMPANY",
            "word" to "@WORD"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty")
            assertFalse(valueStr.startsWith("@"), "$key placeholder should be resolved, got: $valueStr")
            println("Japanese $key: $valueStr")
        }
    }

    @Test
    fun testKoreanLocalization() {
        MockRandom.setLocale(Locale.KOREAN)

        val template = mapOf(
            "name" to "@NAME",
            "city" to "@CITY",
            "company" to "@COMPANY",
            "word" to "@WORD"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key should not be null")
            assertTrue(valueStr.isNotEmpty(), "$key should not be empty")
            assertFalse(valueStr.startsWith("@"), "$key placeholder should be resolved, got: $valueStr")
            println("Korean $key: $valueStr")
        }
    }

    @Test
    fun testMultipleLocales() {
        val locales = listOf(
            Locale.ENGLISH to "English",
            Locale.CHINESE to "Chinese",
            Locale.JAPANESE to "Japanese",
            Locale.KOREAN to "Korean"
        )

        val template = mapOf(
            "name" to "@NAME",
            "city" to "@CITY",
            "company" to "@COMPANY"
        )

        locales.forEach { (locale, localeName) ->
            MockRandom.setLocale(locale)
            assertEquals(locale, MockRandom.getCurrentLocale())

            val result = Mock.mock(template) as Map<String, Any>

            result.forEach { (key, value) ->
                val valueStr = value.toString()
                assertNotNull(value, "$localeName $key should not be null")
                assertTrue(valueStr.isNotEmpty(), "$localeName $key should not be empty")
                assertFalse(valueStr.startsWith("@"), "$localeName $key placeholder should be resolved")
                println("$localeName $key: $valueStr")
            }
        }
    }

    // ==================== 复杂场景测试 ====================

    @Test
    fun testComplexNestedStructure() {
        val template = mapOf(
            "users|3-5" to listOf(
                mapOf(
                    "id|+1" to 1000,
                    "profile" to mapOf(
                        "name" to "@NAME",
                        "email" to "@EMAIL",
                        "age|18-65" to 25,
                        "address" to mapOf(
                            "city" to "@CITY",
                            "province" to "@PROVINCE",
                            "street" to "@STREETNAME",
                            "zipCode|10000-99999" to 12345
                        ),
                        "contacts" to mapOf(
                            "phone" to "@PHONENUMBER(PT.M)",
                            "workPhone" to "@PHONENUMBER(PT.L)",
                            "emergencyContact" to mapOf(
                                "name" to "@NAME",
                                "phone" to "@PHONENUMBER(PT.M)",
                                "relationship" to "@WORD"
                            )
                        )
                    ),
                    "preferences|2-4" to mapOf(
                        "theme" to "dark",
                        "language" to "en",
                        "notifications" to true,
                        "privacy" to false
                    ),
                    "tags|1-5" to listOf("vip", "premium", "active", "verified"),
                    "scores|3" to listOf(
                        mapOf(
                            "subject" to "@WORD",
                            "score|0-100" to 85,
                            "date" to "@DATE"
                        )
                    )
                )
            )
        )

        val result = Mock.mock(template) as Map<String, Any>
        val users = result["users"] as List<Map<String, Any>>

        assertTrue(users.size in 3..5, "Users count should be in range 3-5")

        users.forEachIndexed { index, user ->
            // 验证ID递增
            assertEquals(1000 + index, user["id"])

            // 验证嵌套结构
            val profile = user["profile"] as Map<String, Any>
            assertNotNull(profile["name"])
            assertNotNull(profile["email"])

            val address = profile["address"] as Map<String, Any>
            assertNotNull(address["city"])
            assertNotNull(address["province"])

            val contacts = profile["contacts"] as Map<String, Any>
            assertNotNull(contacts["phone"])
            assertNotNull(contacts["workPhone"])

            val emergencyContact = contacts["emergencyContact"] as Map<String, Any>
            assertNotNull(emergencyContact["name"])
            assertNotNull(emergencyContact["phone"])

            // 验证数组
            val preferences = user["preferences"] as List<*>
            assertTrue(preferences.size in 2..4)

            val tags = user["tags"] as List<*>
            assertTrue(tags.size in 1..5)

            val scores = user["scores"] as List<*>
            assertEquals(3, scores.size)
        }

        println("Complex nested structure result: $result")
    }

    @Test
    fun testMixedRulesAndPlaceholders() {
        val template = mapOf(
            "products|5-10" to listOf(
                mapOf(
                    "id|+1" to 1,
                    "name" to "Product @WORD",
                    "price|10-1000.2" to 99.99,
                    "category" to "@WORD",
                    "inStock|1" to true,
                    "tags|2-5" to listOf("@WORD", "@WORD", "@WORD"),
                    "description" to "@SENTENCE(10, 20)",
                    "manufacturer" to mapOf(
                        "name" to "@COMPANY",
                        "location" to "@CITY, @PROVINCE",
                        "contact" to "@EMAIL"
                    ),
                    "reviews|0-3" to listOf(
                        mapOf(
                            "rating|1-5" to 4,
                            "comment" to "@SENTENCE(5, 15)",
                            "reviewer" to "@NAME",
                            "date" to "@DATE"
                        )
                    )
                )
            )
        )

        val result = Mock.mock(template) as Map<String, Any>
        val products = result["products"] as List<Map<String, Any>>

        assertTrue(products.size in 5..10, "Products count should be in range 5-10")

        products.forEachIndexed { index, product ->
            assertEquals(1 + index, product["id"])

            val name = product["name"] as String
            assertTrue(name.startsWith("Product "), "Product name should start with 'Product '")

            val price = product["price"]
            assertTrue(price is Number, "Price should be a number")

            val tags = product["tags"] as List<*>
            assertTrue(tags.size in 2..5, "Tags count should be in range 2-5")

            val manufacturer = product["manufacturer"] as Map<String, Any>
            assertNotNull(manufacturer["name"])
            assertNotNull(manufacturer["location"])
            assertNotNull(manufacturer["contact"])

            val reviews = product["reviews"] as List<*>
            assertTrue(reviews.size in 0..3, "Reviews count should be in range 0-3")
        }

        println("Mixed rules and placeholders result: $result")
    }

    @Test
    fun testErrorHandlingAndEdgeCases() {
        // 测试边界情况
        val template = mapOf(
            "emptyArray|0" to listOf("item"),
            "singleItem|1" to listOf("only"),
            "zeroRange|0-0" to 5,
            "negativeRange|-5--1" to 0,
            "largeRange|1000-9999" to 5000,
            "emptyString|0" to "text",
            "longString|50" to "A"
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 验证边界情况处理
        val emptyArray = result["emptyArray"] as List<*>
        assertEquals(0, emptyArray.size, "Empty array should have 0 items")

        val singleItem = result["singleItem"] as List<*>
        assertEquals(1, singleItem.size, "Single item array should have 1 item")

        val longString = result["longString"] as String
        assertEquals(50, longString.length, "Long string should have 50 characters")

        println("Error handling and edge cases result: $result")
    }

    @Test
    fun testPerformanceWithLargeData() {
        val template = mapOf(
            "largeDataset|100" to listOf(
                mapOf(
                    "id|+1" to 1,
                    "data" to mapOf(
                        "field1" to "@WORD",
                        "field2" to "@SENTENCE",
                        "field3|1-100" to 50,
                        "field4" to "@EMAIL",
                        "field5" to "@PHONENUMBER"
                    )
                )
            )
        )

        val startTime = System.currentTimeMillis()
        val result = Mock.mock(template) as Map<String, Any>
        val endTime = System.currentTimeMillis()

        val largeDataset = result["largeDataset"] as List<*>
        assertEquals(100, largeDataset.size, "Large dataset should have 100 items")

        val executionTime = endTime - startTime
        println("Performance test: Generated 100 items in ${executionTime}ms")
        assertTrue(executionTime < 5000, "Performance test should complete within 5 seconds")
    }
}