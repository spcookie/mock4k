package io.github.spcookie

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Test to verify increment counter isolation between different mock calls
 */
class IncrementIsolationTest {

    @Test
    @DisplayName("Different property names should start from initial value each time")
    fun testDifferentPropertyCounters() {
        val template1 = mapOf("id1|+1" to 100)
        val template2 = mapOf("id2|+1" to 200)

        // Generate from both templates multiple times
        val results1 = mutableListOf<Int>()
        val results2 = mutableListOf<Int>()

        repeat(3) {
            val result1 = Mock.mock(template1) as Map<String, Any>
            val result2 = Mock.mock(template2) as Map<String, Any>
            results1.add(result1["id1"] as Int)
            results2.add(result2["id2"] as Int)
        }

        // Each call should start from initial value since counters are cleared after each mock call
        assertEquals(listOf(100, 100, 100), results1)
        assertEquals(listOf(200, 200, 200), results2)
    }

    @Test
    @DisplayName("Same property name should start from initial value each time")
    fun testSamePropertyNameIndependentCounter() {
        val template1 = mapOf("id|+1" to 100)
        val template2 = mapOf("id|+1" to 200)  // Same property name, different initial value

        // Generate from both templates alternately
        val results1 = mutableListOf<Int>()
        val results2 = mutableListOf<Int>()

        repeat(3) {
            val result1 = Mock.mock(template1) as Map<String, Any>
            val result2 = Mock.mock(template2) as Map<String, Any>
            results1.add(result1["id"] as Int)
            results2.add(result2["id"] as Int)
        }

        // Each call should start from initial value since counters are cleared after each mock call
        assertEquals(listOf(100, 100, 100), results1)
        assertEquals(listOf(200, 200, 200), results2)
    }

    @Test
    @DisplayName("Multiple properties in same template should increment within single call")
    fun testMultiplePropertiesInSameTemplate() {
        val template = listOf(
            mapOf("id|+1" to 100),
            mapOf("id|+1" to 100),
            mapOf("id|+1" to 100)
        )

        // Generate once and check that same property increments within the same call
        val result = Mock.mock(template) as List<Map<String, Any>>

        // Within a single template generation, the same property should increment across array elements
        assertEquals(100, result[0]["id"])
        assertEquals(101, result[1]["id"])
        assertEquals(102, result[2]["id"])
    }
}