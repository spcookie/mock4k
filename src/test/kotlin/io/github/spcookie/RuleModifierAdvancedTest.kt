package io.github.spcookie

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * 高级规则修饰符测试 - 详细测试所有规则修饰符的组合和边界情况
 */
class RuleModifierAdvancedTest {


    // ==================== 字符串规则修饰符 ====================

    @Test
    fun testStringCountRule() {
        val template = mapOf(
            "repeat3" to "Hello|3",
            "repeat1" to "World|1",
            "repeat10" to "A|10"
        )

        val result = Mock.mock(template) as Map<String, Any>

        assertEquals("HelloHelloHello", result["repeat3"])
        assertEquals("World", result["repeat1"])
        assertEquals("AAAAAAAAAA", result["repeat10"])

        println("String count rule result: $result")
    }

    @Test
    fun testStringRangeRule() {
        val template = mapOf(
            "range2to5" to "Hi|2-5",
            "range1to3" to "Test|1-3",
            "range0to2" to "Empty|0-2"
        )

        val result = Mock.mock(template) as Map<String, Any>

        val range2to5 = result["range2to5"] as String
        val range1to3 = result["range1to3"] as String
        val range0to2 = result["range0to2"] as String

        assertTrue(range2to5.length in 4..15, "range2to5 length should be 4-15 (2-5 * 3), got ${range2to5.length}")
        assertTrue(range1to3.length in 4..12, "range1to3 length should be 4-12 (1-3 * 4), got ${range1to3.length}")
        assertTrue(range0to2.length in 0..10, "range0to2 length should be 0-10 (0-2 * 5), got ${range0to2.length}")

        println("String range rule result: $result")
    }

    // ==================== 数字规则修饰符 ====================

    @Test
    fun testIntegerRangeRule() {
        val template = mapOf(
            "age|18-65" to 25,
            "score|0-100" to 50,
            "negative|-50--10" to -20,
            "large|1000-9999" to 5000
        )

        val result = Mock.mock(template) as Map<String, Any>

        val age = result["age"] as Int
        val score = result["score"] as Int
        val negative = result["negative"] as Int
        val large = result["large"] as Int

        assertTrue(age in 18..65, "Age should be 18-65, got $age")
        assertTrue(score in 0..100, "Score should be 0-100, got $score")
        assertTrue(negative in -50..-10, "Negative should be -50 to -10, got $negative")
        assertTrue(large in 1000..9999, "Large should be 1000-9999, got $large")

        println("Integer range rule result: $result")
    }

    @Test
    fun testFloatRangeRule() {
        val template = mapOf(
            "price|10-100.2" to 50.99,
            "weight|0-10.1-3" to 5.5,
            "percentage|0-100.2-4" to 75.25,
            "precise|1-5.3" to 2.123
        )

        val result = Mock.mock(template) as Map<String, Any>

        result.forEach { (key, value) ->
            when (value) {
                is Double -> {
                    assertTrue(value >= 0, "$key should be non-negative, got $value")
                    println("$key (Double): $value")
                }

                is Float -> {
                    assertTrue(value >= 0, "$key should be non-negative, got $value")
                    println("$key (Float): $value")
                }

                else -> {
                    println("$key (${value::class.simpleName}): $value")
                }
            }
        }
    }

    // ==================== 递增规则修饰符 ====================

    @Test
    fun testIncrementRuleBasic() {
        val template = mapOf(
            "items|3" to listOf(
                mapOf(
                    "id|+1" to 100,
                    "sequence|+10" to 1000,
                    "negative|+-5" to 0
                )
            )
        )

        val result = Mock.mock(template) as Map<String, Any>
        val items = result["items"] as List<Map<String, Any>>

        assertEquals(3, items.size)

        // 验证递增规则
        assertEquals(100, items[0]["id"])
        assertEquals(101, items[1]["id"])
        assertEquals(102, items[2]["id"])

        assertEquals(1000, items[0]["sequence"])
        assertEquals(1010, items[1]["sequence"])
        assertEquals(1020, items[2]["sequence"])

        assertEquals(0, items[0]["negative"])
        assertEquals(-5, items[1]["negative"])
        assertEquals(-10, items[2]["negative"])

        println("Increment rule basic result: $result")
    }

    @Test
    fun testIncrementRuleWithDifferentSteps() {
        val template = mapOf(
            "data|5" to listOf(
                mapOf(
                    "step1|+1" to 0,
                    "step2|+2" to 10,
                    "step5|+5" to 100,
                    "step10|+10" to 1000
                )
            )
        )

        val result = Mock.mock(template) as Map<String, Any>
        val data = result["data"] as List<Map<String, Any>>

        assertEquals(5, data.size)

        // 验证不同步长的递增
        for (i in 0 until 5) {
            assertEquals(0 + i * 1, data[i]["step1"])
            assertEquals(10 + i * 2, data[i]["step2"])
            assertEquals(100 + i * 5, data[i]["step5"])
            assertEquals(1000 + i * 10, data[i]["step10"])
        }

        println("Increment rule with different steps result: $result")
    }

    // ==================== 数组规则修饰符 ====================

    @Test
    fun testArrayCountRule() {
        val template = mapOf(
            "tags|3" to listOf("tag1", "tag2", "tag3", "tag4", "tag5"),
            "numbers|5" to listOf(1, 2, 3),
            "booleans|2" to listOf(true, false)
        )

        val result = Mock.mock(template) as Map<String, Any>

        val tags = result["tags"] as List<*>
        val numbers = result["numbers"] as List<*>
        val booleans = result["booleans"] as List<*>

        assertEquals(3, tags.size)
        assertEquals(5, numbers.size)
        assertEquals(2, booleans.size)

        println("Array count rule result: $result")
    }

    @Test
    fun testArrayRangeRule() {
        val template = mapOf(
            "items|2-5" to listOf("a", "b", "c", "d", "e", "f"),
            "values|1-3" to listOf(10, 20, 30, 40),
            "flags|0-2" to listOf(true, false, true)
        )

        val result = Mock.mock(template) as Map<String, Any>

        val items = result["items"] as List<*>
        val values = result["values"] as List<*>
        val flags = result["flags"] as List<*>

        assertTrue(items.size in 2..5, "Items size should be 2-5, got ${items.size}")
        assertTrue(values.size in 1..3, "Values size should be 1-3, got ${values.size}")
        assertTrue(flags.size in 0..2, "Flags size should be 0-2, got ${flags.size}")

        println("Array range rule result: $result")
    }

    // ==================== 布尔规则修饰符 ====================

    @Test
    fun testBooleanRules() {
        val template = mapOf(
            "flag|1" to true,
            "status|3" to false,
            "options|2-4" to true
        )

        val result = Mock.mock(template) as Map<String, Any>

        val flag = result["flag"]
        val status = result["status"] as List<*>
        val options = result["options"] as List<*>

        assertTrue(flag is Boolean, "Flag should be boolean")
        assertEquals(3, status.size)
        assertTrue(options.size in 2..4, "Options size should be 2-4, got ${options.size}")

        // 验证所有元素都是布尔值
        status.forEach { assertTrue(it is Boolean, "Status item should be boolean") }
        options.forEach { assertTrue(it is Boolean, "Options item should be boolean") }

        println("Boolean rules result: $result")
    }

    // ==================== 对象规则修饰符 ====================

    @Test
    fun testObjectRules() {
        val template = mapOf(
            "config|2" to mapOf(
                "debug" to true,
                "timeout" to 5000,
                "retries" to 3
            ),
            "settings|1-3" to mapOf(
                "theme" to "dark",
                "language" to "en",
                "notifications" to false
            )
        )

        val result = Mock.mock(template) as Map<String, Any>

        val config = result["config"] as List<*>
        val settings = result["settings"] as List<*>

        assertEquals(2, config.size)
        assertTrue(settings.size in 1..3, "Settings size should be 1-3, got ${settings.size}")

        // 验证对象结构
        config.forEach { item ->
            assertTrue(item is Map<*, *>, "Config item should be a map")
            val configMap = item as Map<String, Any>
            assertTrue(configMap.containsKey("debug"))
            assertTrue(configMap.containsKey("timeout"))
            assertTrue(configMap.containsKey("retries"))
        }

        println("Object rules result: $result")
    }

    // ==================== 复杂规则组合 ====================

    @Test
    fun testComplexRuleCombinations() {
        val template = mapOf(
            "complexData|2-3" to listOf(
                mapOf(
                    "id|+1" to 1000,
                    "name" to "Item|3",
                    "scores|3-5" to listOf(
                        mapOf(
                            "value|+10" to 50,
                            "weight|1-10.2" to 5.5
                        )
                    ),
                    "tags|2-4" to listOf("tag|2", "label|3"),
                    "metadata" to mapOf(
                        "created|+1" to 1000000000,
                        "version" to "v|1-3",
                        "flags|1-2" to true
                    )
                )
            )
        )

        val result = Mock.mock(template) as Map<String, Any>
        val complexData = result["complexData"] as List<Map<String, Any>>

        assertTrue(complexData.size in 2..3, "Complex data size should be 2-3")

        complexData.forEachIndexed { index, item ->
            // 验证递增ID
            assertEquals(1000 + index, item["id"])

            // 验证字符串重复
            val name = item["name"] as String
            assertTrue(name.length >= 9, "Name should be at least 9 characters (Item * 3)")

            // 验证嵌套数组和递增
            val scores = item["scores"] as List<Map<String, Any>>
            assertTrue(scores.size in 3..5, "Scores size should be 3-5")

            scores.forEachIndexed { scoreIndex, score ->
                assertEquals(50 + scoreIndex * 10, score["value"])
            }

            // 验证标签数组
            val tags = item["tags"] as List<*>
            assertTrue(tags.size in 2..4, "Tags size should be 2-4")

            // 验证元数据
            val metadata = item["metadata"] as Map<String, Any>
            assertEquals(1000000000 + index, metadata["created"])
        }

        println("Complex rule combinations result: $result")
    }

    // ==================== 边界情况测试 ====================

    @Test
    fun testEdgeCases() {
        val template = mapOf(
            "zero|0" to "text",
            "emptyArray|0" to listOf("item"),
            "sameRange|5-5" to 10,
            "negativeStep|+-1" to 5,
            "largeStep|+100" to 1000,
            "singleChar|1" to "X",
            "longRepeat|50" to "A"
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 验证边界情况
        val zero = result["zero"] as String
        assertEquals("", zero, "Zero repetition should result in empty string")

        val emptyArray = result["emptyArray"] as List<*>
        assertEquals(0, emptyArray.size, "Empty array should have 0 items")

        val sameRange = result["sameRange"] as Int
        assertEquals(5, sameRange, "Same range should return the exact value")

        val longRepeat = result["longRepeat"] as String
        assertEquals(50, longRepeat.length, "Long repeat should have exact length")
        assertEquals("A".repeat(50), longRepeat, "Long repeat should be correct")

        println("Edge cases result: $result")
    }

    @Test
    fun testRuleParsingEdgeCases() {
        val template = mapOf(
            "noRule" to "plain text",
            "justPipe" to "text|",
            "multiPipe" to "text|1|2",
            "spaceInRule" to "text| 3 ",
            "complexRule" to "base|1-10.2-5"
        )

        val result = Mock.mock(template) as Map<String, Any>

        // 验证解析边界情况
        assertNotNull(result["noRule"])
        assertNotNull(result["justPipe"])
        assertNotNull(result["multiPipe"])
        assertNotNull(result["spaceInRule"])
        assertNotNull(result["complexRule"])

        println("Rule parsing edge cases result: $result")
    }
}