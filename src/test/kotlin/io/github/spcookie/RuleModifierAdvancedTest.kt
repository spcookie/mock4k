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
            "text|3" to "Hello",
            "word|1" to "World",
            "char|10" to "A"
        )

        val result = Mock.mock(template) as Map<String, Any>

        assertEquals("HelloHelloHello", result["text"])
        assertEquals("World", result["word"])
        assertEquals("AAAAAAAAAA", result["char"])

        println("String count rule result: $result")
    }

    @Test
    fun testStringRangeRule() {
        val template = mapOf(
            "text|2-5" to "Hi",
            "word|1-3" to "Test",
            "empty|0-2" to "Empty"
        )

        val result = Mock.mock(template) as Map<String, Any>

        val text = result["text"] as String
        val word = result["word"] as String
        val empty = result["empty"] as String

        assertTrue(text.length in 4..10, "text length should be 4-10 (2-5 * 2), got ${text.length}")
        assertTrue(word.length in 4..12, "word length should be 4-12 (1-3 * 4), got ${word.length}")
        assertTrue(empty.length in 0..10, "empty length should be 0-10 (0-2 * 5), got ${empty.length}")

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

        assertEquals(15, tags.size)
        assertEquals(15, numbers.size)
        assertEquals(4, booleans.size)

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

        assertTrue(items.size in 12..30, "Items size should be 12-30, got ${items.size}")
        assertTrue(values.size in 4..12, "Values size should be 4-12, got ${values.size}")
        assertTrue(flags.size in 0..6, "Flags size should be 0-6, got ${flags.size}")

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
        val status = result["status"]
        val options = result["options"]

        assertTrue(flag is Boolean, "Flag should be boolean")
        assertTrue(status is Boolean, "Status should be boolean")
        assertTrue(options is Boolean, "Options should be boolean")

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

        val config = result["config"] as Map<String, Any>
        val settings = result["settings"] as Map<String, Any>

        // 验证 config 对象包含 2 个属性
        assertEquals(2, config.size, "Config should have exactly 2 properties")

        // 验证 settings 对象包含 1-3 个属性
        assertTrue(settings.size in 1..3, "Settings size should be 1-3, got ${settings.size}")

        // 验证 config 的属性都来自原始对象
        val originalConfigKeys = setOf("debug", "timeout", "retries")
        config.keys.forEach { key ->
            assertTrue(originalConfigKeys.contains(key), "Config key '$key' should be from original object")
        }

        // 验证 settings 的属性都来自原始对象
        val originalSettingsKeys = setOf("theme", "language", "notifications")
        settings.keys.forEach { key ->
            assertTrue(originalSettingsKeys.contains(key), "Settings key '$key' should be from original object")
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
                    "name|3" to "Item",
                    "scores|3-5" to listOf(
                        mapOf(
                            "value|+10" to 50,
                            "weight|1-10.2" to 5.5
                        )
                    ),
                    "tags|2-4" to listOf("tag", "label"),
                    "metadata" to mapOf(
                        "created|+1" to 1000000000,
                        "version|1-3" to "v",
                        "flags|1-2" to true
                    )
                )
            )
        )

        val result = Mock.mock(template) as Map<String, Any>
        val complexData = result["complexData"] as List<Map<String, Any>>

        assertTrue(complexData.size in 2..3, "Complex data size should be 2-3")

        var scoreCount = 50
        complexData.forEachIndexed { index, item ->
            // 验证递增ID (每个重复的对象都有独立的计数器，都返回初始值)
            assertEquals(1000 + index, item["id"])

            // 验证字符串重复
            val name = item["name"] as String
            assertEquals("ItemItemItem", name, "Name should be 'ItemItemItem' (Item * 3)")

            // 验证嵌套数组和递增
            val scores = item["scores"] as List<Map<String, Any>>
            assertTrue(scores.size in 3..5, "Scores size should be 3-5")

            // 验证标签数组 (tags|2-4 表示重复数组元素2-4次)
            val tags = item["tags"] as List<*>
            assertTrue(tags.size in 4..8, "Tags size should be 4-8, but got ${tags.size}")

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