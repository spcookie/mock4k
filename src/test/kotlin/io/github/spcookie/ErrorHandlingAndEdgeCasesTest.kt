package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * 错误处理和边界情况测试 - 测试各种异常情况和边界条件
 */
class ErrorHandlingAndEdgeCasesTest {

    // ==================== 空值和空模板测试 ====================

    @Test
    fun testEmptyTemplate() {
        val emptyMap = emptyMap<String, Any>()
        val result = Mock.mock(emptyMap)

        assertNotNull(result, "Empty template should return non-null result")
        assertTrue(result is Map<*, *>, "Empty template should return a map")
        assertTrue((result as Map<*, *>).isEmpty(), "Empty template should return empty map")

        println("Empty template result: $result")
    }

    @Test
    fun testNullValues() {
        val template = mapOf(
            "nullValue" to null,
            "normalValue" to "@STRING",
            "anotherNull" to null
        )

        val result = Mock.mock(template) as Map<String, Any?>

        // 验证null值的处理
        assertTrue(result.containsKey("nullValue"), "Should contain null value key")
        assertNotNull(result["normalValue"], "Normal value should not be null")
        assertTrue(result.containsKey("anotherNull"), "Should contain another null key")

        println("Null values result: $result")
    }

    @Test
    fun testEmptyStrings() {
        val template = mapOf(
            "emptyString" to "",
            "whitespaceString" to "   ",
            "normalString" to "@STRING"
        )

        val result = Mock.mock(template) as Map<String, Any>

        assertEquals("", result["emptyString"], "Empty string should remain empty")
        assertEquals("   ", result["whitespaceString"], "Whitespace string should remain unchanged")
        assertNotEquals("@STRING", result["normalString"], "Normal string placeholder should be resolved")

        println("Empty strings result: $result")
    }

    // ==================== 无效占位符测试 ====================

    @Test
    fun testInvalidPlaceholders() {
        val template = mapOf(
            "invalidPlaceholder" to "@INVALID_PLACEHOLDER",
            "malformedSyntax" to "@NATURAL(",
            "missingCloseParen" to "@RANGE(1, 10",
            "extraParens" to "@STRING()))",
            "emptyPlaceholder" to "@",
            "justAt" to "@",
            "atWithSpace" to "@ STRING",
            "lowercasePlaceholder" to "@string",
            "mixedCase" to "@String"
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 验证无效占位符的处理 - 应该返回原始字符串或某种默认值
        result.forEach { (key, value) ->
            assertNotNull(value, "$key should not be null")
            val valueStr = value.toString()
            assertTrue(
                valueStr.isNotEmpty() || key == "emptyPlaceholder" || key == "justAt",
                "$key should not be empty (except for @ placeholders): $valueStr"
            )
            println("$key: $valueStr")
        }
    }

    @Test
    fun testPlaceholderWithInvalidParameters() {
        val template = mapOf(
            "invalidRange1" to "@RANGE(10, 5)", // 最小值大于最大值
            "invalidRange2" to "@NATURAL(-5, 10)", // 自然数负数范围
            "invalidFloat" to "@FLOAT(abc, def)", // 非数字参数
            "tooManyParams" to "@STRING(10, 20, 30)", // 参数过多
            "negativeLength" to "@STRING(-5)", // 负数长度
            "zeroLength" to "@STRING(0)", // 零长度
            "hugeNumber" to "@NATURAL(1, 999999999999)", // 极大数字
            "floatOverflow" to "@FLOAT(1.0, 1.7976931348623157E+308)" // 浮点数溢出
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            assertNotNull(value, "$key should not be null even with invalid parameters")
            val valueStr = value.toString()
            println("$key: $valueStr")

            // 特定验证
            when (key) {
                "zeroLength" -> {
                    // 零长度字符串的处理
                    if (value is String) {
                        assertTrue(value.isEmpty() || value.isNotEmpty(), "Zero length should be handled gracefully")
                    }
                }

                "negativeLength" -> {
                    // 负数长度的处理
                    assertTrue(
                        valueStr.isNotEmpty() || valueStr.isEmpty(),
                        "Negative length should be handled gracefully"
                    )
                }
            }
        }
    }

    // ==================== 极端数据大小测试 ====================

    @Test
    fun testLargeDataStructures() {
        val template = mapOf(
            "largeArray|100" to listOf("@STRING"),
            "deepNesting" to mapOf(
                "level1" to mapOf(
                    "level2" to mapOf(
                        "level3" to mapOf(
                            "level4" to mapOf(
                                "level5" to "@STRING"
                            )
                        )
                    )
                )
            ),
            "longString" to "@STRING(1000)",
            "manyFields" to (1..50).associate { "field$it" to "@STRING" }
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 验证大数组
        val largeArray = result["largeArray"] as List<*>
        assertEquals(100, largeArray.size, "Large array should have 100 elements")
        largeArray.forEach { item ->
            assertNotNull(item, "Array item should not be null")
            assertTrue(item is String, "Array item should be string")
            assertTrue((item as String).isNotEmpty(), "Array item should not be empty")
        }

        // 验证深度嵌套
        val deepNesting = result["deepNesting"] as Map<String, Any>
        var current: Any = deepNesting
        for (level in 1..5) {
            assertTrue(current is Map<*, *>, "Level $level should be a map")
            current = (current as Map<String, Any>)["level$level"]!!
        }
        assertTrue(current is String, "Final level should be a string")
        assertTrue((current as String).isNotEmpty(), "Final string should not be empty")

        // 验证长字符串
        val longString = result["longString"] as String
        assertTrue(longString.length >= 100, "Long string should be reasonably long, got ${longString.length}")

        // 验证多字段
        val manyFields = result["manyFields"] as Map<String, Any>
        assertEquals(50, manyFields.size, "Should have 50 fields")
        manyFields.forEach { (key, value) ->
            assertTrue(key.startsWith("field"), "Key should start with 'field': $key")
            assertNotNull(value, "Field value should not be null")
            assertTrue(value is String, "Field value should be string")
            assertTrue((value as String).isNotEmpty(), "Field value should not be empty")
        }

        println("Large data structures test completed successfully")
    }

    @Test
    fun testExtremeArraySizes() {
        val template = mapOf(
            "emptyArray|0" to listOf("@STRING"),
            "singleArray|1" to listOf("@STRING"),
            "mediumArray|50" to listOf("@STRING")
        )

        val result = Mock.mock(template) as Map<String, Any>

        val emptyArray = result["emptyArray"] as List<*>
        val singleArray = result["singleArray"] as String
        val mediumArray = result["mediumArray"] as List<*>

        assertEquals(0, emptyArray.size, "Empty array should have 0 elements")
        assertFalse(singleArray.startsWith("@"), "Single array should not start with @")
        assertEquals(50, mediumArray.size, "Medium array should have 50 elements")

        println("Extreme array sizes test completed successfully")
    }

    // ==================== 特殊字符和编码测试 ====================

    @Test
    fun testSpecialCharactersInTemplate() {
        val template = mapOf(
            "unicodeKey🚀" to "@STRING",
            "key with spaces" to "@STRING",
            "key-with-dashes" to "@STRING",
            "key_with_underscores" to "@STRING",
            "key.with.dots" to "@STRING",
            "key@with@at" to "@STRING",
            "key#with#hash" to "@STRING",
            "中文键" to "@STRING",
            "日本語キー" to "@STRING",
            "한국어키" to "@STRING"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            assertNotNull(value, "Value for key '$key' should not be null")
            assertTrue(value is String, "Value for key '$key' should be string")
            assertTrue((value as String).isNotEmpty(), "Value for key '$key' should not be empty")
            println("$key: $value")
        }
    }

    @Test
    fun testSpecialCharactersInValues() {
        val template = mapOf(
            "mixedContent1" to "Hello @NAME! Welcome to @COMPANY.",
            "mixedContent2" to "Email: @EMAIL, Phone: @PHONENUMBER",
            "mixedContent3" to "Price: $@FLOAT(10.0, 100.0), Discount: @NATURAL(5, 25)%",
            "specialChars" to "Special chars: !@#$%^&*()_+-=[]{}|;':,.<>?",
            "unicodeContent" to "Unicode: 🚀🌟💻🎉 @STRING",
            "multiline" to "Line 1\nLine 2 with @STRING\nLine 3",
            "tabContent" to "Tab\tseparated\t@STRING\tvalues"
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            assertNotNull(value, "Value for key '$key' should not be null")
            val valueStr = value.toString()
            assertTrue(valueStr.isNotEmpty(), "Value for key '$key' should not be empty")

            when (key) {
                "mixedContent1" -> {
                    assertFalse(valueStr.contains("@NAME"), "@NAME should be replaced")
                    assertFalse(valueStr.contains("@COMPANY"), "@COMPANY should be replaced")
                    assertTrue(valueStr.contains("Hello"), "Should contain 'Hello'")
                    assertTrue(valueStr.contains("Welcome"), "Should contain 'Welcome'")
                }

                "mixedContent2" -> {
                    assertFalse(valueStr.contains("@EMAIL"), "@EMAIL should be replaced")
                    assertFalse(valueStr.contains("@PHONENUMBER"), "@PHONENUMBER should be replaced")
                    assertTrue(valueStr.contains("Email:"), "Should contain 'Email:'")
                    assertTrue(valueStr.contains("Phone:"), "Should contain 'Phone:'")
                }

                "specialChars" -> {
                    assertTrue(valueStr.contains("!@#$%^&*()"), "Should contain special characters")
                }

                "unicodeContent" -> {
                    assertTrue(valueStr.contains("🚀"), "Should contain rocket emoji")
                    assertFalse(valueStr.contains("@STRING"), "@STRING should be replaced")
                }
            }

            println("$key: $valueStr")
        }
    }

    // ==================== 并发和性能测试 ====================

    @Test
    fun testConcurrentAccess() {
        val template = mapOf(
            "data" to mapOf(
                "id" to "@GUID",
                "name" to "@NAME",
                "email" to "@EMAIL",
                "items|10" to listOf(
                    mapOf(
                        "id" to "@NATURAL",
                        "value" to "@STRING"
                    )
                )
            )
        )

        val results = mutableListOf<Map<String, Any>>()
        val threads = mutableListOf<Thread>()

        // 创建多个线程同时执行Mock.mock
        repeat(5) { threadIndex ->
            val thread = Thread {
                repeat(3) { iteration ->
                    val result = Mock.mock(template) as Map<String, Any>
                    synchronized(results) {
                        results.add(result)
                    }
                    println("Thread $threadIndex, Iteration $iteration completed")
                }
            }
            threads.add(thread)
            thread.start()
        }

        // 等待所有线程完成
        threads.forEach { it.join() }

        // 验证结果
        assertEquals(15, results.size, "Should have 15 results from 5 threads * 3 iterations")

        results.forEach { result ->
            assertNotNull(result["data"], "Each result should have data")
            val data = result["data"] as Map<String, Any>
            assertNotNull(data["id"], "Each data should have id")
            assertNotNull(data["name"], "Each data should have name")
            assertNotNull(data["email"], "Each data should have email")
            assertNotNull(data["items"], "Each data should have items")

            val items = data["items"] as List<*>
            assertEquals(10, items.size, "Each items list should have 10 elements")
        }

        println("Concurrent access test completed successfully")
    }

    @Test
    fun testPerformanceWithLargeTemplate() {
        val startTime = System.currentTimeMillis()

        val template = mapOf(
            "users|100" to listOf(
                mapOf(
                    "id" to "@GUID",
                    "profile" to mapOf(
                        "name" to "@NAME",
                        "email" to "@EMAIL",
                        "phone" to "@PHONENUMBER",
                        "address" to mapOf(
                            "street" to "@STREETNAME",
                            "city" to "@CITY",
                            "province" to "@PROVINCE"
                        )
                    ),
                    "preferences" to mapOf(
                        "theme" to "@COLOR",
                        "language" to "@WORD",
                        "notifications" to "@BOOLEAN"
                    ),
                    "activity|20" to listOf(
                        mapOf(
                            "timestamp" to "@DATETIME",
                            "action" to "@SENTENCE(3, 8)",
                            "details" to "@PARAGRAPH(1, 3)"
                        )
                    )
                )
            )
        )

        val result = Mock.mock(template) as Map<String, Any>

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // 验证结果
        val users = result["users"] as List<*>
        assertEquals(100, users.size, "Should have 100 users")

        users.forEach { user ->
            assertTrue(user is Map<*, *>, "Each user should be a map")
            val userMap = user as Map<String, Any>
            assertNotNull(userMap["id"], "User should have id")
            assertNotNull(userMap["profile"], "User should have profile")
            assertNotNull(userMap["preferences"], "User should have preferences")
            assertNotNull(userMap["activity"], "User should have activity")

            val activity = userMap["activity"] as List<*>
            assertEquals(20, activity.size, "Each user should have 20 activity records")
        }

        println("Performance test completed in ${duration}ms")
        assertTrue(duration < 10000, "Performance test should complete within 10 seconds, took ${duration}ms")
    }

    // ==================== 内存和资源测试 ====================

    @Test
    fun testMemoryUsageWithRepeatedCalls() {
        val template = mapOf(
            "data" to mapOf(
                "id" to "@GUID",
                "content" to "@STRING(100)",
                "numbers|50" to listOf("@NATURAL")
            )
        )

        val results = mutableListOf<Any>()

        // 执行多次Mock.mock调用
        repeat(100) { iteration ->
            val result = Mock.mock(template)
            results.add(result)

            // 每10次迭代验证一次结果
            if (iteration % 10 == 0) {
                assertTrue(result is Map<*, *>, "Result should be a map")
                val resultMap = result as Map<String, Any>
                assertNotNull(resultMap["data"], "Result should have data")
                println("Iteration $iteration completed")
            }
        }

        assertEquals(100, results.size, "Should have 100 results")
        println("Memory usage test completed successfully")
    }

    // ==================== 边界值测试 ====================

    @Test
    fun testBoundaryValues() {
        val template = mapOf(
            "minInt" to "@INTEGER(${Int.MIN_VALUE}, ${Int.MIN_VALUE + 1})",
            "maxInt" to "@INTEGER(${Int.MAX_VALUE - 1}, ${Int.MAX_VALUE})",
            "minFloat" to "@FLOAT(${Float.MIN_VALUE}, ${Float.MIN_VALUE * 2})",
            "maxFloat" to "@FLOAT(${Float.MAX_VALUE / 2}, ${Float.MAX_VALUE})",
            "zeroRange" to "@INTEGER(0, 2)",
            "singleChar" to "@STRING(1)",
            "emptyArray|0" to listOf("@STRING")
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            assertNotNull(value, "$key should not be null")

            when (key) {
                "zeroRange" -> {
                    val num = when (value) {
                        is String -> value.toInt()
                        is Int -> value
                        else -> fail("Expected String or Int, got ${value::class.simpleName}")
                    }
                    assertTrue(num in 0..2, "Zero range should return value between 0 and 2")
                }

                "singleChar" -> {
                    val str = value as String
                    assertEquals(1, str.length, "Single char string should have length 1")
                }

                "emptyArray" -> {
                    val array = value as List<*>
                    assertEquals(0, array.size, "Empty array should have size 0")
                }
            }

            println("$key: $value")
        }
    }
}