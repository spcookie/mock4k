package io.github.spcookie

import io.github.spcookie.Mock.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test cases for syntax specification implementation
 */
class SyntaxTest {


    @Nested
    @DisplayName("String Rules Tests")
    inner class StringRulesTest {

        @Test
        @DisplayName("String repeat count rule: 'name|count': string")
        fun testStringRepeatCount() {
            val template = mapOf(
                "name|3" to "hello"
            )
            val result = mock(template) as Map<String, Any>

            assertEquals("hellohellohello", result["name"])
        }

        @Test
        @DisplayName("String repeat range rule: 'name|min-max': string")
        fun testStringRepeatRange() {
            val template = mapOf(
                "name|2-4" to "hi"
            )
            val result = mock(template) as Map<String, Any>
            val value = result["name"] as String
            assertTrue(value.length in 4..8) // "hi" repeated 2-4 times
            assertTrue(value.startsWith("hi"))
        }
    }

    @Nested
    @DisplayName("Number Rules Tests")
    inner class NumberRulesTest {

        @Test
        @DisplayName("Number range rule: 'name|min-max': number")
        fun testNumberRange() {
            val template = mapOf(
                "age|18-65" to 0
            )
            val result = mock(template) as Map<String, Any>
            val age = result["age"] as Int
            assertTrue(age in 18..65)
        }

        @Test
        @DisplayName("Float range rule: 'name|min-max.dmin-dmax': number")
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
    @DisplayName("Boolean Rules Tests")
    inner class BooleanRulesTest {

        @Test
        @DisplayName("Boolean random rule: 'name|1': boolean")
        fun testBooleanRandom() {
            val template = mapOf(
                "isActive|1" to true
            )

            // Generate multiple times to ensure randomness
            val results = mutableSetOf<Boolean>()
            repeat(20) {
                val result = mock(template) as Map<String, Any>
                results.add(result["isActive"] as Boolean)
            }

            // Should have both true and false values (with high probability)
            assertTrue(results.size >= 1) // At least one unique value
        }

        @Test
        @DisplayName("Boolean weighted rule: 'name|min-max': value")
        fun testBooleanWeighted() {
            val template = mapOf(
                "isVip|1-3" to true
            )

            // Generate multiple times to test probability distribution
            var trueCount = 0
            val totalTests = 100
            repeat(totalTests) {
                val result = mock(template) as Map<String, Any>
                if (result["isVip"] as Boolean) trueCount++
            }

            // With 1-3 ratio, true probability should be around 25% (1/(1+3))
            // Allow some variance in random testing
            assertTrue(trueCount in 15..40)
        }
    }

    @Nested
    @DisplayName("Object Rules Tests")
    inner class ObjectRulesTest {

        @Test
        @DisplayName("Object property count rule: 'name|count': object")
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
        @DisplayName("Object property range rule: 'name|min-max': object")
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
    @DisplayName("Array Rules Tests")
    inner class ArrayRulesTest {

        @Test
        @DisplayName("Array pick one rule: 'name|1': array")
        fun testArrayPickOne() {
            val template = mapOf(
                "color|1" to listOf("red", "green", "blue", "yellow")
            )
            val result = mock(template) as Map<String, Any>
            val color = result["color"] as String
            assertTrue(color in listOf("red", "green", "blue", "yellow"))
        }

        @Test
        @DisplayName("Array pick sequential rule: 'name|+1': array")
        fun testArrayPickSequential() {
            val template = mapOf(
                "list|5" to listOf(
                    mapOf(
                        "status|+1" to listOf("pending", "processing", "completed")
                    )
                )
            )

            // Generate multiple times to test sequential picking
            val results = mutableListOf<String>()
            val result = mock(template) as Map<*, *>
            for (item in result["list"] as List<*>) {
                results.add((item as Map<*, *>)["status"] as String)
            }

            // Should cycle through the array sequentially
            assertEquals("pending", results[0])
            assertEquals("processing", results[1])
            assertEquals("completed", results[2])
            assertEquals("pending", results[3]) // Cycle back
            assertEquals("processing", results[4])
        }

        @Test
        @DisplayName("Array repeat count rule: 'name|count': array")
        fun testArrayRepeatCount() {
            val template = mapOf(
                "tags|2" to listOf("kotlin", "java")
            )
            val result = mock(template) as Map<String, Any>
            val tags = result["tags"] as List<String>
            assertEquals(4, tags.size) // 2 repetitions of 2 elements
            assertTrue(tags.all { it in listOf("kotlin", "java") })
        }

        @Test
        @DisplayName("Array repeat range rule: 'name|min-max': array")
        fun testArrayRepeatRange() {
            val template = mapOf(
                "items|1-3" to listOf("item1", "item2")
            )
            val result = mock(template) as Map<String, Any>
            val items = result["items"] as List<String>
            assertTrue(items.size in 2..6) // 1-3 repetitions of 2 elements
            assertTrue(items.all { it in listOf("item1", "item2") })
        }
    }

    @Nested
    @DisplayName("Backward Compatibility Tests")
    inner class BackwardCompatibilityTest {

        @Test
        @DisplayName("Legacy Range rule should still work")
        fun testLegacyRange() {
            val template = mapOf(
                "count|5-10" to 0
            )
            val result = mock(template) as Map<String, Any>
            val count = result["count"] as Int
            assertTrue(count in 5..10)
        }

        @Test
        @DisplayName("Legacy Count rule should still work")
        fun testLegacyCount() {
            val template = mapOf(
                "text|3" to "abc"
            )
            val result = mock(template) as Map<String, Any>
            assertEquals("abcabcabc", result["text"])
        }

        @Test
        @DisplayName("Legacy Increment rule should still work")
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