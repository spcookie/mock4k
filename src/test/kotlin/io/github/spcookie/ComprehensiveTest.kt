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
        GlobalMockConf.Locale.setLocale(Locale.ENGLISH)
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

        val result = try {
            mock(template) as Map<String, Any>
        } catch (e: Exception) {
            println("Error in testErrorHandlingAndEdgeCases: ${e.message}")
            println("Exception type: ${e.javaClass.simpleName}")
            println("Template: $template")
            e.printStackTrace()
            throw e
        }

        // 验证所有字段都存在且不为空
        result.forEach { (key, value) ->
            assertNotNull(value, "字段 $key 不应为 null")
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

        val result = try {
            mock(template) as Map<String, Any>
        } catch (e: Exception) {
            println("Error in testErrorHandlingAndEdgeCases: ${e.message}")
            println("Exception type: ${e.javaClass.simpleName}")
            println("Template: $template")
            e.printStackTrace()
            throw e
        }
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
            // 'name|min-max': array - 通过重复属性值 array 生成一个新数组，重复次数大于等于 min，小于等于 max
            "items|2-8" to listOf("apple", "banana", "orange")
        )

        val result = mock(template) as Map<String, Any>

        val age = result["age"] as Int
        val score = result["score"] as Int
        val temperature = result["temperature"] as Int
        val items = result["items"] as List<*>

        assertTrue(age in 18..65, "年龄应该在18-65范围内，实际得到 $age")
        assertTrue(score in 0..100, "分数应该在0-100范围内，实际得到 $score")
        assertTrue(temperature in -10..40, "温度应该在-10-40范围内，实际得到 $temperature")
        // 验证数组重复范围：原数组有3个元素，重复2-8次，所以结果数组大小应该在6-24之间
        assertTrue(
            items.size in 6..24,
            "项目数量应该在6-24范围内（3个元素 * 2-8次重复），实际得到 ${items.size}"
        )
        // 验证所有元素都来自原数组
        assertTrue(items.all { it in listOf("apple", "banana", "orange") }, "所有项目都应该来自原数组")

        println("Range modifier result: $result")
    }

    @Test
    fun testCountModifier() {
        val template = mapOf(
            "tags|5" to listOf("tag1", "tag2", "tag3"),
            "letters|10" to "A",
            "flags|3" to true
        )

        val result = mock(template) as Map<String, Any>

        val tags = result["tags"] as List<*>
        val letters = result["letters"] as String

        assertEquals(15, tags.size)
        assertEquals(10, letters.length)
        assertEquals(true, result["flags"] is Boolean)

        println("Count modifier result: $result")
    }

    @Test
    fun testFloatModifiers() {
        val template = mapOf(
            "price|100-999.2" to 199.99,
            "weight|10-50.1-3" to 25.5,
            "percentage|0-100.2-4" to 75.25
        )

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            when (value) {
                is Double -> {
                    assertTrue(value >= 0, "$key 应该为非负数，实际得到 $value")
                    println("$key: $value")
                }

                is Float -> {
                    assertTrue(value >= 0, "$key 应该为非负数，实际得到 $value")
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

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 不应为空")
            assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析，实际得到: $valueStr")
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

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 不应为空")
            assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析，实际得到: $valueStr")
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

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 不应为空")
            assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析，实际得到: $valueStr")
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

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 不应为空")
            assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析，实际得到: $valueStr")
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

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 不应为空")
            assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析，实际得到: $valueStr")
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

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val phoneNumber = value.toString()
            assertNotNull(value, "$key 的电话号码不应为 null")
            assertTrue(phoneNumber.isNotEmpty(), "$key 的电话号码不应为空")
            assertFalse(phoneNumber.startsWith("@"), "电话占位符 $key 应该被解析，实际得到: $phoneNumber")
            assertTrue(phoneNumber.any { it.isDigit() }, "电话号码 $key 应该包含数字: $phoneNumber")
            println("$key: $phoneNumber")
        }
    }

    // ==================== 国际化测试 ====================

    @Test
    fun testChineseLocalization() {
        GlobalMockConf.Locale.setLocale(Locale.CHINESE)

        val template = mapOf(
            "name" to "@NAME",
            "city" to "@CITY",
            "company" to "@COMPANY",
            "province" to "@PROVINCE",
            "profession" to "@PROFESSION",
            "word" to "@WORD",
            "sentence" to "@SENTENCE"
        )

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 不应为空")
            assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析，实际得到: $valueStr")
            println("Chinese $key: $valueStr")
        }
    }

    @Test
    fun testJapaneseLocalization() {
        GlobalMockConf.Locale.setLocale(Locale.JAPANESE)

        val template = mapOf(
            "name" to "@NAME",
            "city" to "@CITY",
            "company" to "@COMPANY",
            "word" to "@WORD"
        )

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 不应为空")
            assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析，实际得到: $valueStr")
            println("Japanese $key: $valueStr")
        }
    }

    @Test
    fun testKoreanLocalization() {
        GlobalMockConf.Locale.setLocale(Locale.KOREAN)

        val template = mapOf(
            "name" to "@NAME",
            "city" to "@CITY",
            "company" to "@COMPANY",
            "word" to "@WORD"
        )

        val result = mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            val valueStr = value.toString()
            assertNotNull(value, "$key 不应为 null")
            assertTrue(valueStr.isNotEmpty(), "$key 不应为空")
            assertFalse(valueStr.startsWith("@"), "$key 占位符应该被解析，实际得到: $valueStr")
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
            GlobalMockConf.Locale.setLocale(locale)
            assertEquals(locale, GlobalMockConf.Locale.getCurrentLocale())

            val result = mock(template) as Map<String, Any>

            result.forEach { (key, value) ->
                val valueStr = value.toString()
                assertNotNull(value, "$localeName $key 不应为 null")
                assertTrue(valueStr.isNotEmpty(), "$localeName $key 不应为空")
                assertFalse(valueStr.startsWith("@"), "$localeName $key 占位符应该被解析")
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

        val result = mock(template) as Map<String, Any>
        val users = result["users"] as List<Map<String, Any>>

        assertTrue(users.size in 3..5, "用户数量应该在3-5范围内")

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

            // 验证preferences对象
            val preferences = user["preferences"] as Map<String, Any>
            assertTrue(preferences.size in 2..4, "偏好设置大小应该在2-4之间，实际得到 ${preferences.size}")

            val tags = user["tags"] as List<*>
            assertTrue(tags.isNotEmpty(), "标签不应为空")

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

        val result = mock(template) as Map<String, Any>
        val products = result["products"] as List<Map<String, Any>>

        assertTrue(products.size in 5..10, "产品数量应该在5-10范围内")

        products.forEachIndexed { index, product ->
            assertEquals(1 + index, product["id"])

            val name = product["name"] as String
            assertTrue(name.startsWith("Product "), "产品名称应该以 'Product ' 开头")

            val price = product["price"]
            assertTrue(price is Number, "价格应该是数字")

            val tags = product["tags"] as List<*>
            assertTrue(tags.isNotEmpty(), "标签不应为空")

            val manufacturer = product["manufacturer"] as Map<String, Any>
            assertNotNull(manufacturer["name"])
            assertNotNull(manufacturer["location"])
            assertNotNull(manufacturer["contact"])

            val reviews = product["reviews"] as List<*>
            assertTrue(reviews.size in 0..3, "评论数量应该在0-3范围内")
        }

        println("Mixed rules and placeholders result: $result")
    }

    @Test
    fun testErrorHandlingAndEdgeCases() {
        // 测试边界情况
        val template = mapOf(
            "singleItem|1" to listOf("only"),
            "largeRange|1000-9999" to 5000
        )

        val result = mock(template) as Map<String, Any>

        // 验证边界情况处理
        val singleItem = result["singleItem"] as String
        assertEquals("only", singleItem, "单个项目应该是 only")

        val largeRange = result["largeRange"] as Number
        assertTrue(largeRange.toInt() in 1000..9999, "大范围应该在1000-9999之间")

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
        val result = mock(template) as Map<String, Any>
        val endTime = System.currentTimeMillis()

        val largeDataset = result["largeDataset"] as List<*>
        assertEquals(100, largeDataset.size, "大数据集应该有100个项目")

        val executionTime = endTime - startTime
        println("性能测试: 在 ${executionTime}ms 内生成了100个项目")
        assertTrue(executionTime < 5000, "性能测试应该在5秒内完成")
    }
}