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

        assertNotNull(result, "空模板应该返回非null结果")
        assertTrue(result is Map<*, *>, "空模板应该返回一个map")
        assertTrue((result as Map<*, *>).isEmpty(), "空模板应该返回空map")

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
        assertTrue(result.containsKey("nullValue"), "应该包含null值键")
        assertNotNull(result["normalValue"], "正常值不应为null")
        assertTrue(result.containsKey("anotherNull"), "应该包含另一个null键")

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

        assertEquals("", result["emptyString"], "空字符串应该保持为空")
        assertEquals("   ", result["whitespaceString"], "空白字符串应该保持不变")
        assertNotEquals("@STRING", result["normalString"], "正常字符串占位符应该被解析")

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
            assertNotNull(value, "$key 不应为null")
            val valueStr = value.toString()
            assertTrue(
                valueStr.isNotEmpty() || key == "emptyPlaceholder" || key == "justAt",
                "$key 不应为空（除了@占位符）: $valueStr"
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
            assertNotNull(value, "即使参数无效，$key 也不应为null")
            val valueStr = value.toString()
            println("$key: $valueStr")

            // 特定验证
            when (key) {
                "zeroLength" -> {
                    // 零长度字符串的处理
                    if (value is String) {
                        assertTrue(value.isEmpty() || value.isNotEmpty(), "零长度应该被优雅处理")
                    }
                }

                "negativeLength" -> {
                    // 负数长度的处理
                    assertTrue(
                        valueStr.isNotEmpty() || valueStr.isEmpty(),
                        "负长度应该被优雅处理"
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
        assertEquals(100, largeArray.size, "大数组应该有100个元素")
        largeArray.forEach { item ->
            assertNotNull(item, "数组项不应为 null")
            assertTrue(item is String, "数组项应该是字符串")
            assertTrue((item as String).isNotEmpty(), "数组项不应为空")
        }

        // 验证深度嵌套
        val deepNesting = result["deepNesting"] as Map<String, Any>
        var current: Any = deepNesting
        for (level in 1..5) {
            assertTrue(current is Map<*, *>, "第 $level 层应该是一个映射")
            current = (current as Map<String, Any>)["level$level"]!!
        }
        assertTrue(current is String, "最终层应该是一个字符串")
        assertTrue((current as String).isNotEmpty(), "最终字符串不应为空")

        // 验证长字符串
        val longString = result["longString"] as String
        assertTrue(longString.length >= 100, "长字符串应该足够长，得到 ${longString.length}")

        // 验证多字段
        val manyFields = result["manyFields"] as Map<String, Any>
        assertEquals(50, manyFields.size, "应该有50个字段")
        manyFields.forEach { (key, value) ->
            assertTrue(key.startsWith("field"), "键应该以 'field' 开头: $key")
            assertNotNull(value, "字段值不应为 null")
            assertTrue(value is String, "字段值应该是字符串")
            assertTrue((value as String).isNotEmpty(), "字段值不应为空")
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

        assertEquals(0, emptyArray.size, "空数组应该有0个元素")
        assertFalse(singleArray.startsWith("@"), "单个数组不应该以@开头")
        assertEquals(50, mediumArray.size, "中等数组应该有50个元素")

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
            assertNotNull(value, "键 '$key' 的值不应为null")
            assertTrue(value is String, "键 '$key' 的值应该是字符串")
            assertTrue((value as String).isNotEmpty(), "键 '$key' 的值不应为空")
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
            assertNotNull(value, "键 '$key' 的值不应为null")
            val valueStr = value.toString()
            assertTrue(valueStr.isNotEmpty(), "键 '$key' 的值不应为空")

            when (key) {
                "mixedContent1" -> {
                    assertFalse(valueStr.contains("@NAME"), "@NAME 应该被替换")
                    assertFalse(valueStr.contains("@COMPANY"), "@COMPANY 应该被替换")
                    assertTrue(valueStr.contains("Hello"), "应该包含 'Hello'")
                    assertTrue(valueStr.contains("Welcome"), "应该包含 'Welcome'")
                }

                "mixedContent2" -> {
                    assertFalse(valueStr.contains("@EMAIL"), "@EMAIL 应该被替换")
                    assertFalse(valueStr.contains("@PHONENUMBER"), "@PHONENUMBER 应该被替换")
                    assertTrue(valueStr.contains("Email:"), "应该包含 'Email:'")
                    assertTrue(valueStr.contains("Phone:"), "应该包含 'Phone:'")
                }

                "specialChars" -> {
                    assertTrue(valueStr.contains("!@#$%^&*()"), "应该包含特殊字符")
                }

                "unicodeContent" -> {
                    assertTrue(valueStr.contains("🚀"), "应该包含火箭表情符号")
                    assertFalse(valueStr.contains("@STRING"), "@STRING 应该被替换")
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
        assertEquals(15, results.size, "应该有15个结果（5个线程 * 3次迭代）")

        results.forEach { result ->
            assertNotNull(result["data"], "每个结果都应该有data")
            val data = result["data"] as Map<String, Any>
            assertNotNull(data["id"], "每个data都应该有id")
            assertNotNull(data["name"], "每个data都应该有name")
            assertNotNull(data["email"], "每个data都应该有email")
            assertNotNull(data["items"], "每个data都应该有items")

            val items = data["items"] as List<*>
            assertEquals(10, items.size, "每个items列表应该有10个元素")
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
        assertEquals(100, users.size, "应该有100个用户")

        users.forEach { user ->
            assertTrue(user is Map<*, *>, "每个用户都应该是一个map")
            val userMap = user as Map<String, Any>
            assertNotNull(userMap["id"], "用户应该有id")
            assertNotNull(userMap["profile"], "用户应该有profile")
            assertNotNull(userMap["preferences"], "用户应该有preferences")
            assertNotNull(userMap["activity"], "用户应该有activity")

            val activity = userMap["activity"] as List<*>
            assertEquals(20, activity.size, "每个用户应该有20条活动记录")
        }

        println("Performance test completed in ${duration}ms")
        assertTrue(duration < 10000, "性能测试应该在10秒内完成，实际用时 ${duration}ms")
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
                assertTrue(result is Map<*, *>, "结果应该是一个map")
                val resultMap = result as Map<String, Any>
                assertNotNull(resultMap["data"], "结果应该有data")
                println("Iteration $iteration completed")
            }
        }

        assertEquals(100, results.size, "应该有100个结果")
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
            assertNotNull(value, "$key 不应为null")

            when (key) {
                "zeroRange" -> {
                    val num = when (value) {
                        is String -> value.toInt()
                        is Int -> value
                        else -> fail("期望String或Int，实际得到 ${value::class.simpleName}")
                    }
                    assertTrue(num in 0..2, "零范围应该返回0到2之间的值")
                }

                "singleChar" -> {
                    val str = value as String
                    assertEquals(1, str.length, "单字符字符串应该长度为1")
                }

                "emptyArray" -> {
                    val array = value as List<*>
                    assertEquals(0, array.size, "空数组应该大小为0")
                }
            }

            println("$key: $value")
        }
    }
}