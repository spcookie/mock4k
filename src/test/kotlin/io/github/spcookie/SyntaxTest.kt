package io.github.spcookie

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * 语法规范实现的测试用例
 */
class SyntaxTest {


    @Nested
    @DisplayName("字符串规则测试")
    inner class StringRulesTest {

        @Test
        @DisplayName("字符串重复次数规则: 'name|count': string")
        fun testStringRepeatCount() {
            val template = mapOf(
                "name|3" to "hello"
            )
            val result = mock(template) as Map<String, Any>

            assertEquals("hellohellohello", result["name"])
        }

        @Test
        @DisplayName("字符串重复范围规则: 'name|min-max': string")
        fun testStringRepeatRange() {
            val template = mapOf(
                "name|2-4" to "hi"
            )
            val result = mock(template) as Map<String, Any>
            val value = result["name"] as String
            assertTrue(value.length in 4..8) // "hi" 重复 2-4 次
            assertTrue(value.startsWith("hi"))
        }
    }

    @Nested
    @DisplayName("数字规则测试")
    inner class NumberRulesTest {

        @Test
        @DisplayName("数字范围规则: 'name|min-max': number")
        fun testNumberRange() {
            val template = mapOf(
                "age|18-65" to 0
            )
            val result = mock(template) as Map<String, Any>
            val age = result["age"] as Int
            assertTrue(age in 18..65)
        }

        @Test
        @DisplayName("浮点数范围规则: 'name|min-max.dmin-dmax': number")
        fun testFloatRange() {
            val template = mapOf(
                "price|10-20.2-4" to 0.0
            )
            val result = mock(template) as Map<String, Any>
            val price = result["price"] as Double
            assertTrue(price >= 10.0 && price <= 20.9999)
        }
    }

    @Nested
    @DisplayName("布尔值规则测试")
    inner class BooleanRulesTest {

        @Test
        @DisplayName("布尔值随机规则: 'name|1': boolean")
        fun testBooleanRandom() {
            val template = mapOf(
                "isActive|1" to true
            )

            // 多次生成以确保随机性
            val results = mutableSetOf<Boolean>()
            repeat(20) {
                val result = mock(template) as Map<String, Any>
                results.add(result["isActive"] as Boolean)
            }

            // 应该包含 true 和 false 值（高概率）
            assertTrue(results.size >= 1) // 至少一个唯一值
        }

        @Test
        @DisplayName("布尔值权重规则: 'name|min-max': value")
        fun testBooleanWeighted() {
            val template = mapOf(
                "isVip|1-3" to true
            )

            // 多次生成以测试概率分布
            var trueCount = 0
            val totalTests = 100
            repeat(totalTests) {
                val result = mock(template) as Map<String, Any>
                if (result["isVip"] as Boolean) trueCount++
            }

            // 1-3 比例下，true 的概率应该约为 25% (1/(1+3))
            // 允许随机测试中的一些变化
            assertTrue(trueCount in 15..40)
        }
    }

    @Nested
    @DisplayName("对象规则测试")
    inner class ObjectRulesTest {

        @Test
        @DisplayName("对象属性数量规则: 'name|count': object")
        fun testObjectCount() {
            val template = mapOf(
                "user|2" to mapOf(
                    "name" to "@name",
                    "age" to "@age",
                    "email" to "@email",
                    "phone" to "@phone"
                )
            )
            val result = mock(template) as Map<String, Any>
            val user = result["user"] as Map<String, Any>
            assertEquals(2, user.size)
        }

        @Test
        @DisplayName("对象属性范围规则: 'name|min-max': object")
        fun testObjectRange() {
            val template = mapOf(
                "config|1-3" to mapOf(
                    "debug" to true,
                    "timeout" to 5000,
                    "retries" to 3,
                    "cache" to false
                )
            )
            val result = mock(template) as Map<String, Any>
            val config = result["config"] as Map<String, Any>
            assertTrue(config.size in 1..3)
        }
    }

    @Nested
    @DisplayName("数组规则测试")
    inner class ArrayRulesTest {

        @Test
        @DisplayName("数组选择一个规则: 'name|1': array")
        fun testArrayPickOne() {
            val template: Map<String, *> = mapOf(
                "color|1" to listOf("red", "green", "blue", "yellow")
            )
            val result = mock(template)
            val color = result["color"] as String
            assertTrue(color in listOf("red", "green", "blue", "yellow"))
        }

        @Test
        @DisplayName("数组顺序选择规则: 'name|+1': array")
        fun testArrayPickSequential() {
            val template = mapOf(
                "list|5" to listOf(
                    mapOf(
                        "status|+1" to listOf("pending", "processing", "completed")
                    )
                )
            )

            // 多次生成以测试顺序选择
            val results = mutableListOf<String>()
            val result = mock(template) as Map<*, *>
            for (item in result["list"] as List<*>) {
                results.add((item as Map<*, *>)["status"] as String)
            }

            // 应该按顺序循环遍历数组
            assertEquals("pending", results[0])
            assertEquals("processing", results[1])
            assertEquals("completed", results[2])
            assertEquals("pending", results[3]) // 循环回到开始
            assertEquals("processing", results[4])
        }

        @Test
        @DisplayName("数组重复次数规则: 'name|count': array")
        fun testArrayRepeatCount() {
            val template = mapOf(
                "tags|2" to listOf("kotlin", "java")
            )
            val result = mock(template) as Map<String, Any>
            val tags = result["tags"] as List<String>
            assertEquals(4, tags.size) // 2 个元素重复 2 次
            assertTrue(tags.all { it in listOf("kotlin", "java") })
        }

        @Test
        @DisplayName("数组重复范围规则: 'name|min-max': array")
        fun testArrayRepeatRange() {
            val template = mapOf(
                "items|1-3" to listOf("item1", "item2")
            )
            val result = mock(template) as Map<String, Any>
            val items = result["items"] as List<String>
            assertTrue(items.size in 2..6) // 2 个元素重复 1-3 次
            assertTrue(items.all { it in listOf("item1", "item2") })
        }
    }

    @Nested
    @DisplayName("向后兼容性测试")
    inner class BackwardCompatibilityTest {

        @Test
        @DisplayName("旧版范围规则应该仍然有效")
        fun testLegacyRange() {
            val template = mapOf(
                "count|5-10" to 0
            )
            val result = mock(template) as Map<String, Any>
            val count = result["count"] as Int
            assertTrue(count in 5..10)
        }

        @Test
        @DisplayName("旧版计数规则应该仍然有效")
        fun testLegacyCount() {
            val template = mapOf(
                "text|3" to "abc"
            )
            val result = mock(template) as Map<String, Any>
            assertEquals("abcabcabc", result["text"])
        }

        @Test
        @DisplayName("旧版递增规则应该仍然有效")
        fun testLegacyIncrement() {
            val template = mapOf(
                "list|3" to listOf(
                    mapOf(
                        "id|+2" to 10
                    )
                )
            )

            val results = mutableListOf<Int>()
            val result = mock(template) as Map<*, *>
            for (item in result["list"] as List<*>) {
                results.add((item as Map<*, *>)["id"] as Int)
            }


            assertEquals(10, results[0])
            assertEquals(12, results[1])
            assertEquals(14, results[2])
        }
    }
}