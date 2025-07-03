package io.github.spcookie

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Test to verify that increment counters are properly isolated between different mock instances
 */
class CounterIsolationTest {

    @Test
    @DisplayName("Different mock calls should have isolated counters")
    fun testCounterIsolation() {
        val template1 = mapOf("id|+1" to 100)
        val template2 = mapOf("id|+1" to 200)

        // First call to template1
        val result1a = Mock.mock(template1) as Map<String, Any>
        assertEquals(100, result1a["id"])

        // First call to template2
        val result2a = Mock.mock(template2) as Map<String, Any>
        assertEquals(200, result2a["id"])

        // Second call to template1 - should start fresh
        val result1b = Mock.mock(template1) as Map<String, Any>
        assertEquals(100, result1b["id"])

        // Second call to template2 - should start fresh
        val result2b = Mock.mock(template2) as Map<String, Any>
        assertEquals(200, result2b["id"])
    }

    @Test
    @DisplayName("Within single template, counters should work normally")
    fun testWithinTemplateCounters() {
        val template = mapOf(
            "user" to mapOf(
                "id|+1" to 1000,
                "score|+5" to 0
            ),
            "admin" to mapOf(
                "id|+1" to 2000,
                "score|+10" to 100
            )
        )

        val result = Mock.mock(template) as Map<String, Any>
        val user = result["user"] as Map<String, Any>
        val admin = result["admin"] as Map<String, Any>

        // Within the same template generation, counters should work
        assertEquals(1000, user["id"])
        assertEquals(0, user["score"])
        assertEquals(2000, admin["id"])
        assertEquals(100, admin["score"])
    }

    @Test
    @DisplayName("Array elements should have independent counters within same call")
    fun testArrayCounters() {
        val template = listOf(
            mapOf("id|+1" to 100),
            mapOf("id|+1" to 100),
            mapOf("id|+1" to 100)
        )

        val result = Mock.mock(template) as List<Map<String, Any>>

        // Each array element should have independent counter within the same call
        assertEquals(100, result[0]["id"])
        assertEquals(100, result[1]["id"])
        assertEquals(100, result[2]["id"])
    }
}